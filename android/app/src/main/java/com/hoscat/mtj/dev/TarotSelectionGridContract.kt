package com.hoscat.mtj.dev

internal const val TAROT_CARD_ASPECT_RATIO = 0.62f

internal data class TarotOverviewGridSpec(val columns: Int, val rows: Int)

internal fun tarotOverviewGridSpec(
    availableWidthDp: Float,
    availableHeightDp: Float,
    cardCount: Int = 78,
    gapDp: Float = 2f,
): TarotOverviewGridSpec {
    require(availableWidthDp > 0f)
    require(availableHeightDp > 0f)
    require(cardCount > 0)
    if (cardCount == 78) return TarotOverviewGridSpec(columns = 8, rows = 10)
    return (5..12).map { columns ->
        val rows = (cardCount + columns - 1) / columns
        val cellWidth = (availableWidthDp - gapDp * (columns - 1)).coerceAtLeast(1f) / columns
        val cellHeight = (availableHeightDp - gapDp * (rows - 1)).coerceAtLeast(1f) / rows
        val visibleCardWidth = minOf(cellWidth, cellHeight * TAROT_CARD_ASPECT_RATIO)
        TarotOverviewGridSpec(columns, rows) to visibleCardWidth
    }.maxBy { it.second }.first
}

internal fun tarotOverviewRowSizes(cardCount: Int, columns: Int): List<Int> {
    require(cardCount >= 0)
    require(columns > 0)
    if (cardCount == 0) return emptyList()
    return List(cardCount / columns) { columns } +
        (cardCount % columns).takeIf { it > 0 }?.let(::listOf).orEmpty()
}
