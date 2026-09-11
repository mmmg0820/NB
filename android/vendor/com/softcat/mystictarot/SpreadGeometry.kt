package com.softcat.mystictarot

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

internal data class SpreadGeometryGaps(
    val horizontal: Float,
    val vertical: Float,
    val cardToLabel: Float,
    val labelToLabel: Float,
)

internal data class SpreadGeometryRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

internal data class SpreadGeometryExtent(
    val width: Float,
    val height: Float,
)

internal data class SpreadGeometryPlacement(
    val index: Int,
    val positionLabel: String,
    val rotationDegrees: Float,
    val cardBounds: SpreadGeometryRect,
    val transformedCardBounds: SpreadGeometryRect,
    val labelBounds: SpreadGeometryRect,
)

internal data class SpreadGeometry(
    val cardWidth: Float,
    val cardHeight: Float,
    val placements: List<SpreadGeometryPlacement>,
    val canvasExtent: SpreadGeometryExtent,
)

internal enum class SpreadGeometryStatus {
    Ready,
    InvalidParameters,
    DefinitionMismatch,
    NoCollisionFreeLayout,
    UnrepresentableExtent,
}

internal sealed interface SpreadGeometryResult {
    val status: SpreadGeometryStatus

    data class Success(val geometry: SpreadGeometry) : SpreadGeometryResult {
        override val status: SpreadGeometryStatus = SpreadGeometryStatus.Ready
    }

    data class Unsupported(override val status: SpreadGeometryStatus) : SpreadGeometryResult {
        init {
            require(status != SpreadGeometryStatus.Ready)
        }
    }
}

/**
 * Measures a spread without changing its slot IDs, order, coordinates, or rotations.
 * [cardAspectRatio] is card width divided by card height. Label heights must already
 * be measured for the returned card width; labels are never shortened by this layer.
 * Card width is independent of label heights. Mini/full Celtic card rows may move
 * vertically in the measured pass so each row reserves its complete label bands,
 * including separate below/above lanes for the coincident center pair.
 */
internal fun calculateSpreadGeometry(
    spread: SpreadOption,
    availableWidth: Float,
    cardAspectRatio: Float,
    labelHeights: List<Float>,
    gaps: SpreadGeometryGaps,
): SpreadGeometryResult {
    if (
        !availableWidth.isFinite() || availableWidth <= 0f ||
        !cardAspectRatio.isFinite() || cardAspectRatio <= 0f ||
        spread.cardCount <= 0 ||
        spread.positionLabels.size != spread.cardCount ||
        labelHeights.size != spread.cardCount ||
        labelHeights.any { !it.isFinite() || it < 0f } ||
        !gaps.isValid()
    ) {
        return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.InvalidParameters)
    }

    val slots = spreadSlots(spread, spread.cardCount)
    if (
        slots.size != spread.cardCount ||
        slots.any { !it.x.isFinite() || !it.y.isFinite() || !it.rotation.isFinite() }
    ) {
        return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.DefinitionMismatch)
    }
    if (spread.usesReservedCelticLabels() && !spread.hasExpectedCelticContract(slots)) {
        return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.DefinitionMismatch)
    }

    val widthLimit = min(
        availableWidth.toDouble(),
        Float.MAX_VALUE.toDouble() * cardAspectRatio.toDouble(),
    )
    val widthMargin = max(
        Math.ulp(availableWidth).toDouble() * 8.0,
        availableWidth.toDouble() * 1e-7,
    )
    val fittedWidth = max(0.0, availableWidth.toDouble() - widthMargin)
    val zeroVerticalOffsets = List(slots.size) { 0.0 }

    fun fits(cardWidth: Double): Boolean {
        val candidate = buildCards(
            spread,
            slots,
            cardWidth,
            cardAspectRatio.toDouble(),
            gaps,
            zeroVerticalOffsets,
        ) ?: return false
        return candidate.contentWidth <= fittedWidth
    }

    if (!fits(0.0)) {
        return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.NoCollisionFreeLayout)
    }

    var low = 0.0
    var high = widthLimit
    repeat(80) {
        val middle = (low + high) / 2.0
        if (fits(middle)) low = middle else high = middle
    }
    if (low <= 0.0) {
        return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.NoCollisionFreeLayout)
    }

    val verticalOffsets = celticVerticalOffsets(spread, slots, labelHeights, gaps)
    val cards = buildCards(
        spread,
        slots,
        low,
        cardAspectRatio.toDouble(),
        gaps,
        verticalOffsets,
    ) ?: return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.NoCollisionFreeLayout)
    return cards.withLabels(spread, labelHeights, gaps, availableWidth)
}

private fun SpreadGeometryGaps.isValid(): Boolean =
    horizontal.isFinite() && horizontal >= 0f &&
        vertical.isFinite() && vertical >= 0f &&
        cardToLabel.isFinite() && cardToLabel >= 0f &&
        labelToLabel.isFinite() && labelToLabel >= 0f

private data class DoubleRect(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
) {
    fun translated(dx: Double, dy: Double): DoubleRect =
        DoubleRect(left + dx, top + dy, right + dx, bottom + dy)

    fun intersects(other: DoubleRect): Boolean =
        min(right, other.right) > max(left, other.left) &&
            min(bottom, other.bottom) > max(top, other.top)
}

private data class DoubleCard(
    val centerX: Double,
    val centerY: Double,
    val bounds: DoubleRect,
    val transformedBounds: DoubleRect,
)

private data class CardLayout(
    val cardWidth: Double,
    val cardHeight: Double,
    val cards: List<DoubleCard>,
    val contentWidth: Double,
)

private fun buildCards(
    spread: SpreadOption,
    slots: List<SpreadSlot>,
    cardWidth: Double,
    aspectRatio: Double,
    gaps: SpreadGeometryGaps,
    verticalOffsets: List<Double>,
): CardLayout? {
    if (verticalOffsets.size != slots.size || verticalOffsets.any { !it.isFinite() || it < 0.0 }) {
        return null
    }
    val cardHeight = cardWidth / aspectRatio
    if (!cardWidth.isFinite() || !cardHeight.isFinite() || cardHeight > Float.MAX_VALUE) return null

    val transformedSizes = slots.map { slot ->
        val radians = Math.toRadians((slot.rotation % 360f).toDouble())
        val transformedWidth = abs(cardWidth * cos(radians)) + abs(cardHeight * sin(radians))
        val transformedHeight = abs(cardWidth * sin(radians)) + abs(cardHeight * cos(radians))
        transformedWidth to transformedHeight
    }
    val widest = transformedSizes.maxOfOrNull { it.first } ?: return null
    val tallest = transformedSizes.maxOfOrNull { it.second } ?: return null
    if (!widest.isFinite() || !tallest.isFinite()) return null

    // Float output needs a few ULPs of clearance so zero-area contacts do not
    // become overlaps when the parent renderer consumes the measured values.
    val horizontalClearance = Math.ulp(widest.toFloat()).toDouble() * 8.0
    val verticalClearance = Math.ulp(tallest.toFloat()).toDouble() * 8.0
    val xStep = widest + gaps.horizontal.toDouble() + horizontalClearance
    val yStep = tallest + gaps.vertical.toDouble() + verticalClearance
    if (!xStep.isFinite() || !yStep.isFinite()) return null

    val rawCards = slots.mapIndexed { index, slot ->
        val centerX = slot.x.toDouble() * xStep
        val centerY = slot.y.toDouble() * yStep + verticalOffsets[index]
        val (transformedWidth, transformedHeight) = transformedSizes[index]
        DoubleCard(
            centerX = centerX,
            centerY = centerY,
            bounds = centeredRect(centerX, centerY, cardWidth, cardHeight),
            transformedBounds = centeredRect(centerX, centerY, transformedWidth, transformedHeight),
        )
    }
    if (rawCards.any { !it.bounds.isRepresentable() || !it.transformedBounds.isRepresentable() }) return null

    for (first in rawCards.indices) {
        for (second in first + 1 until rawCards.size) {
            if (
                rawCards[first].transformedBounds.intersects(rawCards[second].transformedBounds) &&
                !spread.allowsCoincidentPair(first, second, slots)
            ) {
                return null
            }
        }
    }

    val minX = rawCards.minOf { min(it.bounds.left, it.transformedBounds.left) }
        .let { cardMin -> min(cardMin, rawCards.minOf { it.centerX - cardWidth / 2.0 }) }
    val maxX = rawCards.maxOf { max(it.bounds.right, it.transformedBounds.right) }
        .let { cardMax -> max(cardMax, rawCards.maxOf { it.centerX + cardWidth / 2.0 }) }
    val minY = rawCards.minOf { min(it.bounds.top, it.transformedBounds.top) }
    val translated = rawCards.map { card ->
        card.copy(
            centerX = card.centerX - minX,
            centerY = card.centerY - minY,
            bounds = card.bounds.translated(-minX, -minY),
            transformedBounds = card.transformedBounds.translated(-minX, -minY),
        )
    }
    return CardLayout(cardWidth, cardHeight, translated, maxX - minX)
}

private fun celticVerticalOffsets(
    spread: SpreadOption,
    slots: List<SpreadSlot>,
    labelHeights: List<Float>,
    gaps: SpreadGeometryGaps,
): List<Double> {
    if (!spread.usesReservedCelticLabels()) return List(slots.size) { 0.0 }

    val rows = slots.map { it.y }.distinct().sorted()
    val pairRow = slots[0].y
    val offsetByRow = mutableMapOf<Float, Double>()
    var cumulative = 0.0
    rows.forEach { row ->
        if (row == pairRow) {
            cumulative += labelHeights[1].toDouble() + gaps.cardToLabel.toDouble()
        }
        offsetByRow[row] = cumulative
        val labelsBelowRow = slots.indices.filter { slots[it].y == row && it != 1 }
        if (labelsBelowRow.isNotEmpty()) {
            cumulative += labelsBelowRow.maxOf { labelHeights[it].toDouble() } +
                gaps.cardToLabel.toDouble()
        }
    }
    return slots.map { slot -> checkNotNull(offsetByRow[slot.y]) }
}

private fun CardLayout.withLabels(
    spread: SpreadOption,
    labelHeights: List<Float>,
    gaps: SpreadGeometryGaps,
    availableWidth: Float,
): SpreadGeometryResult {
    val labels = if (spread.usesReservedCelticLabels()) {
        buildReservedCelticLabels(labelHeights, gaps)
    } else {
        buildSweptLabels(labelHeights, gaps)
    } ?: return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.UnrepresentableExtent)

    for (label in labels) {
        if (cards.any { label.intersects(it.transformedBounds) }) {
            return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.NoCollisionFreeLayout)
        }
    }
    for (first in labels.indices) {
        for (second in first + 1 until labels.size) {
            if (labels[first].intersects(labels[second])) {
                return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.NoCollisionFreeLayout)
            }
        }
    }

    val allRects = cards.flatMap { listOf(it.bounds, it.transformedBounds) } + labels
    if (allRects.any { !it.isRepresentable() || it.left < 0.0 || it.top < 0.0 }) {
        return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.UnrepresentableExtent)
    }
    val canvasWidth = allRects.maxOf { it.right }.toFloat()
    val canvasHeight = allRects.maxOf { it.bottom }.toFloat()
    if (
        !canvasWidth.isFinite() || !canvasHeight.isFinite() ||
        canvasWidth > availableWidth ||
        cardWidth.toFloat() <= 0f || cardHeight.toFloat() <= 0f
    ) {
        return SpreadGeometryResult.Unsupported(SpreadGeometryStatus.UnrepresentableExtent)
    }

    val slots = spreadSlots(spread, spread.cardCount)
    val placements = cards.indices.map { index ->
        SpreadGeometryPlacement(
            index = index,
            positionLabel = spread.positionLabels[index],
            rotationDegrees = slots[index].rotation,
            cardBounds = cards[index].bounds.toFloatRect(),
            transformedCardBounds = cards[index].transformedBounds.toFloatRect(),
            labelBounds = labels[index].toFloatRect(),
        )
    }
    return SpreadGeometryResult.Success(
        SpreadGeometry(
            cardWidth = cardWidth.toFloat(),
            cardHeight = cardHeight.toFloat(),
            placements = placements,
            canvasExtent = SpreadGeometryExtent(canvasWidth, canvasHeight),
        )
    )
}

private fun CardLayout.buildReservedCelticLabels(
    labelHeights: List<Float>,
    gaps: SpreadGeometryGaps,
): List<DoubleRect>? {
    if (cards.size < 2) return null
    val pairTop = min(cards[0].transformedBounds.top, cards[1].transformedBounds.top)
    val pairBottom = max(cards[0].transformedBounds.bottom, cards[1].transformedBounds.bottom)
    return cards.indices.map { index ->
        val card = cards[index]
        val height = labelHeights[index].toDouble()
        val left = card.centerX - cardWidth / 2.0
        val top = when (index) {
            0 -> pairBottom + gaps.cardToLabel.toDouble()
            1 -> pairTop - gaps.cardToLabel.toDouble() - height
            else -> card.transformedBounds.bottom + gaps.cardToLabel.toDouble()
        }
        DoubleRect(left, top, left + cardWidth, top + height)
    }
}

private fun CardLayout.buildSweptLabels(
    labelHeights: List<Float>,
    gaps: SpreadGeometryGaps,
): List<DoubleRect>? {
    val labels = mutableListOf<DoubleRect>()
    for (index in cards.indices) {
        val card = cards[index]
        val height = labelHeights[index].toDouble()
        val left = card.centerX - cardWidth / 2.0
        var top = card.transformedBounds.bottom + gaps.cardToLabel.toDouble()
        var attempts = 0
        while (true) {
            val candidate = DoubleRect(left, top, left + cardWidth, top + height)
            var nextTop = top
            cards.forEach { blocker ->
                if (candidate.intersects(blocker.transformedBounds)) {
                    nextTop = max(nextTop, blocker.transformedBounds.bottom + gaps.cardToLabel.toDouble())
                }
            }
            labels.forEach { blocker ->
                if (candidate.intersects(blocker)) {
                    nextTop = max(nextTop, blocker.bottom + gaps.labelToLabel.toDouble())
                }
            }
            if (nextTop == top) {
                labels += candidate
                break
            }
            top = nextTop
            attempts++
            if (attempts > cards.size + labels.size + 1 || !top.isFinite()) return null
        }
    }
    return labels
}

private fun centeredRect(centerX: Double, centerY: Double, width: Double, height: Double): DoubleRect =
    DoubleRect(
        centerX - width / 2.0,
        centerY - height / 2.0,
        centerX + width / 2.0,
        centerY + height / 2.0,
    )

private fun DoubleRect.isRepresentable(): Boolean =
    left.isFinite() && top.isFinite() && right.isFinite() && bottom.isFinite() &&
        abs(left) <= Float.MAX_VALUE && abs(top) <= Float.MAX_VALUE &&
        abs(right) <= Float.MAX_VALUE && abs(bottom) <= Float.MAX_VALUE

private fun DoubleRect.toFloatRect(): SpreadGeometryRect = SpreadGeometryRect(
    left = left.toFloat(),
    top = top.toFloat(),
    right = right.toFloat(),
    bottom = bottom.toFloat(),
)

private fun SpreadOption.usesReservedCelticLabels(): Boolean =
    layoutId == "mini_celtic" || layoutId == "celtic_cross"

private fun SpreadOption.hasExpectedCelticContract(slots: List<SpreadSlot>): Boolean {
    val expectedCount = if (layoutId == "mini_celtic") 6 else 10
    return cardCount == expectedCount &&
        slots.size == expectedCount &&
        slots[0].x == slots[1].x &&
        slots[0].y == slots[1].y &&
        slots[0].rotation == 0f &&
        slots[1].rotation == 90f
}

private fun SpreadOption.allowsCoincidentPair(
    first: Int,
    second: Int,
    slots: List<SpreadSlot>,
): Boolean {
    if (!usesReservedCelticLabels()) return false
    if (first != 0 || second != 1) return false
    return slots[first].x == slots[second].x && slots[first].y == slots[second].y
}
