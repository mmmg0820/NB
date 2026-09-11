package com.hoscat.mtj.dev

internal data class TarotSpreadOverviewLabel(
    val positionLine: String,
    val cardNameLine: String,
    val directionLine: String,
) {
    val text: String = "$positionLine\n$cardNameLine\n$directionLine"
    val readableWidthCandidates: List<String> = listOf(cardNameLine, directionLine)
        .flatMap { line -> line.split(TAROT_LABEL_WHITESPACE).filter(String::isNotEmpty) }
}

internal fun formatTarotSpreadOverviewLabel(
    order: Int,
    positionLabel: String,
    cardName: String,
    directionLabel: String,
): TarotSpreadOverviewLabel = TarotSpreadOverviewLabel(
    positionLine = "$order. $positionLabel",
    cardNameLine = cardName,
    directionLine = directionLabel,
)

private val TAROT_LABEL_WHITESPACE = Regex("\\s+")
