package com.mtj.tarot

import com.softcat.mystictarot.*
import java.util.Collections
import kotlin.random.Random

internal enum class MeaningSource { NONE, STANDARD_CARD, PERSONAL_CARD }

internal data class DeckButtonPolicy(
    val showCardDetails: Boolean,
    val showAi: Boolean,
    val meaningSource: MeaningSource
) {
    val secondaryButtonCount: Int get() = (if (showCardDetails) 1 else 0) + (if (showAi) 1 else 0)
    val totalButtonCount: Int get() = secondaryButtonCount + 1
}

internal fun deckButtonPolicy(deck: TarotDeck, drawnCards: List<DrawnCard>): DeckButtonPolicy {
    val hasMeaning = drawnCards.any { hasDisplayableCardMeaning(it.card) }
    return DeckButtonPolicy(
        showCardDetails = hasMeaning,
        showAi = deck.id == "standard" || deck.aiPrompt.isNotBlank(),
        meaningSource = when {
            !hasMeaning -> MeaningSource.NONE
            deck.id == "standard" -> MeaningSource.STANDARD_CARD
            else -> MeaningSource.PERSONAL_CARD
        }
    )
}

private fun <T> frozen(values: List<T>): List<T> = Collections.unmodifiableList(ArrayList(values))

/** Companion data must travel with SavedReading; the legacy JSON writer drops it. */
internal data class MtjResultSnapshot(
    val reading: SavedReading,
    val spread: SpreadOption,
    val slots: List<SpreadSlot>,
    val buttons: DeckButtonPolicy,
    val aiPromptSnapshot: String?,
    val useReversedAtStart: Boolean,
    val envelopeVersion: Int = 1
)

/** Source functions and this adapter must compile in the SAME Kotlin module. */
internal class MtjTarotBridge(
    deck: TarotDeck,
    spread: SpreadOption,
    private val question: String,
    private val useReversed: Boolean,
    private val random: Random = Random.Default,
    shuffledOrderIds: List<Int>? = null,
) {
    private val deck = deck.copy(cards = frozen(deck.cards.map {
        it.copy(uprightKeywords = frozen(it.uprightKeywords), reversedKeywords = frozen(it.reversedKeywords))
    }))
    private val spread = spread.copy(positionLabels = frozen(spread.positionLabels))
    val shuffledCards: List<TarotCard>
    private var locked: List<DrawnCard>? = null
    private var result: MtjResultSnapshot? = null

    init {
        require(this.deck.enabled) { "Deck is disabled" }
        require(this.deck.cards.map { it.id }.distinct().size == this.deck.cards.size) { "Duplicate deck IDs" }
        require(this.spread.cardCount > 0) { "Invalid spread count" }
        val minimumDeckSize = if (this.spread.drawMode == SpreadDrawMode.FinalOneFromTen) 10 else this.spread.cardCount
        require(this.deck.cards.size >= minimumDeckSize) { "Insufficient deck size" }
        val generatedOrder = shuffleDeckForReading(this.deck.cards, random)
        shuffledCards = frozen(
            if (shuffledOrderIds == null) {
                generatedOrder
            } else {
                require(shuffledOrderIds.size == this.deck.cards.size) { "Invalid shuffled order size" }
                require(shuffledOrderIds.toSet() == this.deck.cards.map { it.id }.toSet()) {
                    "Invalid shuffled order IDs"
                }
                shuffledOrderIds.map { id -> this.deck.cards.single { it.id == id } }
            },
        )
    }

    /** Receives final selected IDs only. The source UI owns the ten-to-one interaction. */
    fun lockSelection(selectedIds: List<Int>): List<DrawnCard> {
        val requiredCount = if (spread.drawMode == SpreadDrawMode.FinalOneFromTen) 1 else spread.cardCount
        require(selectedIds.size == requiredCount) { "Incomplete final selection" }
        require(selectedIds.distinct().size == selectedIds.size) { "Duplicate selection" }
        val selected = selectedIds.map { id ->
            deck.cards.firstOrNull { it.id == id } ?: throw IllegalArgumentException("Unknown card ID: $id")
        }
        locked?.let { previous ->
            require(previous.map { it.card.id } == selectedIds) { "Result is locked; start a new bridge to redraw" }
            return lockDrawnCards(selected, previous, useReversed, random)
        }
        return frozen(lockDrawnCards(selected, emptyList(), useReversed, random)).also { locked = it }
    }

    fun snapshot(): MtjResultSnapshot {
        result?.let { return it }
        val cards = checkNotNull(locked) { "Lock final selection before snapshot" }
        val buttons = deckButtonPolicy(deck, cards)
        val saved = buildSavedReading(spread, question, cards, deck)
        return MtjResultSnapshot(
            reading = saved.copy(cards = frozen(saved.cards)),
            spread = spread,
            slots = frozen(spreadSlots(spread, cards.size)),
            buttons = buttons,
            aiPromptSnapshot = if (buttons.showAi) buildSpreadLlmPrompt(spread, question, cards, deck.aiPrompt) else null,
            useReversedAtStart = useReversed
        ).also { result = it }
    }
}
