package com.mtj.tarot

import com.softcat.mystictarot.SavedReading
import com.softcat.mystictarot.SpreadDrawMode
import com.softcat.mystictarot.SpreadOption
import com.softcat.mystictarot.SpreadSlot
import com.softcat.mystictarot.spreadSlots
import java.util.Collections

internal enum class TarotQuestionOrigin { DIRECT, SAJU_REFERENCE, RECENT_REUSE }

internal enum class TarotAudienceKind { SELF, OTHER }

internal enum class TarotReaderMode { BEGINNER, EXPERIENCED }

internal enum class SpreadReadingLoad { SIMPLE, STEADY, DEEP }

internal data class SajuQuestionSeed(
    val chartReferenceId: String,
    val topic: String,
    val questionDraft: String = "",
    val birthPayloadIncluded: Boolean = false
)

internal data class TarotAudience(
    val kind: TarotAudienceKind,
    val alias: String,
    val birthPayloadIncluded: Boolean = false
)

internal data class TarotQuestionEntry(
    val question: String,
    val origin: TarotQuestionOrigin,
    val topic: String?,
    val chartReferenceId: String?,
    val audience: TarotAudience,
    val readerMode: TarotReaderMode,
    val positionHelpExpanded: Boolean
)

internal data class RepeatedQuestionAdvice(
    val isRepeated: Boolean,
    val previousReadingIds: List<Long>,
    val action: RepeatedQuestionAction
)

internal enum class RepeatedQuestionAction {
    CONTINUE,
    OFFER_HISTORY_COMPARISON
}

internal data class SpreadDiscoveryItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val cardCount: Int,
    val drawMode: SpreadDrawMode,
    val previewSlots: List<SpreadSlot>,
    val positionLabels: List<String>,
    val purposeDescription: String,
    val readingLoad: SpreadReadingLoad,
    val readingLoadLabel: String,
    val hasSpatialPreview: Boolean
)

internal data class SpreadCountGroup(
    val cardCount: Int,
    val label: String,
    val spreads: List<SpreadDiscoveryItem>
)

internal fun tarotAudience(kind: TarotAudienceKind, alias: String? = null): TarotAudience {
    val normalized = alias?.trim().orEmpty()
    return TarotAudience(
        kind = kind,
        alias = when {
            normalized.isNotEmpty() -> normalized
            kind == TarotAudienceKind.SELF -> "나"
            else -> "상대"
        }
    )
}

internal fun tarotEntryFromSaju(
    seed: SajuQuestionSeed,
    audience: TarotAudience = tarotAudience(TarotAudienceKind.SELF),
    readerMode: TarotReaderMode = TarotReaderMode.BEGINNER,
    positionHelpExpanded: Boolean = readerMode == TarotReaderMode.BEGINNER
): TarotQuestionEntry {
    require(seed.chartReferenceId.trim().isNotEmpty()) { "chartReferenceId is required" }
    require(seed.topic.trim().isNotEmpty()) { "topic is required" }
    require(!seed.birthPayloadIncluded) { "Birth payload must be gated outside tarot entry handoff" }
    require(!audience.birthPayloadIncluded) { "Audience birth payload must not be required for tarot entry" }
    return TarotQuestionEntry(
        question = seed.questionDraft.trim(),
        origin = TarotQuestionOrigin.SAJU_REFERENCE,
        topic = seed.topic.trim(),
        chartReferenceId = seed.chartReferenceId.trim(),
        audience = audience,
        readerMode = readerMode,
        positionHelpExpanded = positionHelpExpanded
    )
}

internal fun tarotEntryDirect(
    question: String,
    audience: TarotAudience = tarotAudience(TarotAudienceKind.SELF),
    readerMode: TarotReaderMode = TarotReaderMode.BEGINNER,
    positionHelpExpanded: Boolean = readerMode == TarotReaderMode.BEGINNER
): TarotQuestionEntry {
    require(!audience.birthPayloadIncluded) { "Audience birth payload must not be required for tarot entry" }
    return TarotQuestionEntry(
        question = question.trim(),
        origin = TarotQuestionOrigin.DIRECT,
        topic = null,
        chartReferenceId = null,
        audience = audience,
        readerMode = readerMode,
        positionHelpExpanded = positionHelpExpanded
    )
}

internal fun repeatedQuestionAdvice(question: String, previousReadings: List<SavedReading>): RepeatedQuestionAdvice {
    val current = normalizeQuestion(question)
    if (current.isEmpty()) {
        return RepeatedQuestionAdvice(false, emptyList(), RepeatedQuestionAction.CONTINUE)
    }
    val matches = previousReadings
        .filter { normalizeQuestion(it.question) == current }
        .map { it.id }
    return RepeatedQuestionAdvice(
        isRepeated = matches.isNotEmpty(),
        previousReadingIds = immutable(matches),
        action = if (matches.isEmpty()) RepeatedQuestionAction.CONTINUE else RepeatedQuestionAction.OFFER_HISTORY_COMPARISON
    )
}

internal fun spreadDiscoveryByCardCount(spreads: List<SpreadOption>): List<SpreadCountGroup> {
    return immutable(spreads
        .map { spread ->
            val slots = spreadSlots(spread, spread.cardCount)
            SpreadDiscoveryItem(
                key = spread.key,
                title = spread.title,
                subtitle = spread.subtitle,
                cardCount = spread.cardCount,
                drawMode = spread.drawMode,
                previewSlots = immutable(slots),
                positionLabels = immutable(spread.positionLabels),
                purposeDescription = spreadPurposeDescription(spread),
                readingLoad = readingLoadForCardCount(spread.cardCount),
                readingLoadLabel = readingLoadLabel(spread.cardCount),
                hasSpatialPreview = slots.size == spread.cardCount && slots.isNotEmpty()
            )
        }
        .groupBy { it.cardCount }
        .toSortedMap()
        .map { (count, items) ->
            SpreadCountGroup(
                cardCount = count,
                label = "${count}장",
                spreads = immutable(items.sortedWith(compareBy<SpreadDiscoveryItem> { it.drawMode != SpreadDrawMode.Normal }.thenBy { it.key }))
            )
        })
}

internal fun spreadDiscoveryRecentFirst(
    spreads: List<SpreadOption>,
    recentSpreadKey: String?,
    useCounts: Map<String, Int>
): List<SpreadDiscoveryItem> {
    val known = spreadDiscoveryItems(spreads)
    return immutable(known.sortedWith(
        compareByDescending<SpreadDiscoveryItem> { it.key == recentSpreadKey }
            .thenByDescending { useCounts[it.key] ?: 0 }
            .thenBy { it.cardCount }
            .thenBy { it.key }
    ))
}

internal fun spreadDiscoveryMostUsedFirst(
    spreads: List<SpreadOption>,
    useCounts: Map<String, Int>
): List<SpreadDiscoveryItem> {
    val known = spreadDiscoveryItems(spreads)
    return immutable(known.sortedWith(
        compareByDescending<SpreadDiscoveryItem> { useCounts[it.key] ?: 0 }
            .thenBy { it.cardCount }
            .thenBy { it.key }
    ))
}

internal fun favoriteSpreadItems(
    spreads: List<SpreadOption>,
    persistedFavoriteKeys: List<String>
): List<SpreadDiscoveryItem> {
    val byKey = spreadDiscoveryItems(spreads).associateBy { it.key }
    return immutable(persistedFavoriteKeys.distinct().mapNotNull { byKey[it] })
}

private fun spreadDiscoveryItems(spreads: List<SpreadOption>): List<SpreadDiscoveryItem> =
    spreadDiscoveryByCardCount(spreads).flatMap { it.spreads }

private fun spreadPurposeDescription(spread: SpreadOption): String {
    val value = spread.subtitle.trim().trimEnd('.', '。', '!', '?')
    require(value.isNotBlank()) { "Spread purpose must be nonblank" }
    val forbidden = listOf("적중률", "확률", "%", "예언", "보장")
    require(forbidden.none { value.contains(it, ignoreCase = true) }) { "Spread purpose must not claim accuracy" }
    return value
}

private fun readingLoadForCardCount(cardCount: Int): SpreadReadingLoad = when {
    cardCount <= 3 -> SpreadReadingLoad.SIMPLE
    cardCount <= 6 -> SpreadReadingLoad.STEADY
    else -> SpreadReadingLoad.DEEP
}

private fun readingLoadLabel(cardCount: Int): String = when (readingLoadForCardCount(cardCount)) {
    SpreadReadingLoad.SIMPLE -> "간단 · ${cardCount}장"
    SpreadReadingLoad.STEADY -> "보통 · ${cardCount}장"
    SpreadReadingLoad.DEEP -> "깊게 · ${cardCount}장"
}

private fun normalizeQuestion(value: String): String =
    value.trim().lowercase().replace(Regex("\\s+"), " ")

private fun <T> immutable(values: List<T>): List<T> = Collections.unmodifiableList(ArrayList(values))
