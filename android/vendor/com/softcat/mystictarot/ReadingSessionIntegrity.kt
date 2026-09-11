package com.softcat.mystictarot

import kotlin.random.Random

/** Keeps shuffle and result generation stable inside one reading session. */
internal fun shuffleDeckForReading(
    cards: List<TarotCard>,
    random: Random = Random.Default
): List<TarotCard> = cards.shuffled(random)

internal fun lockDrawnCards(
    selectedCards: List<TarotCard>,
    existingDrawnCards: List<DrawnCard>,
    useReversed: Boolean,
    random: Random = Random.Default
): List<DrawnCard> {
    val existingMatchesSelection = existingDrawnCards.size == selectedCards.size &&
        existingDrawnCards.zip(selectedCards).withIndex().all { (index, pair) ->
            val (drawn, selected) = pair
            drawn.order == index + 1 && drawn.card.id == selected.id
        }
    if (existingMatchesSelection) return existingDrawnCards

    return selectedCards.mapIndexed { index, card ->
        DrawnCard(
            card = card,
            direction = if (useReversed && random.nextBoolean()) {
                CardDirection.Reversed
            } else {
                CardDirection.Upright
            },
            order = index + 1
        )
    }
}
