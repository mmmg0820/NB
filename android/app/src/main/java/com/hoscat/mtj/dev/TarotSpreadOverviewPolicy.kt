package com.hoscat.mtj.dev

import com.softcat.mystictarot.SpreadGeometryRect
import com.softcat.mystictarot.SpreadGeometryResult
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

internal enum class TarotSpreadOverviewMode {
    Spatial,
    OrderedFallback,
}

internal enum class TarotSpreadOverviewOverlapAllowance {
    None,
    CelticCenterPair,
}

internal data class TarotSpreadOverviewLimits(
    val minimumCardWidth: Float,
    val maximumCanvasHeight: Float,
    val maximumLabelSeparation: Float,
)

internal fun tarotSpreadOverviewOverlapAllowance(
    layoutId: String,
): TarotSpreadOverviewOverlapAllowance = when (layoutId) {
    "mini_celtic", "celtic_cross" -> TarotSpreadOverviewOverlapAllowance.CelticCenterPair
    else -> TarotSpreadOverviewOverlapAllowance.None
}

internal fun chooseTarotSpreadOverviewMode(
    result: SpreadGeometryResult,
    availableWidth: Float,
    expectedCardCount: Int,
    limits: TarotSpreadOverviewLimits,
    overlapAllowance: TarotSpreadOverviewOverlapAllowance = TarotSpreadOverviewOverlapAllowance.None,
    minimumReadableLabelWidth: Float = 0f,
): TarotSpreadOverviewMode {
    if (
        !availableWidth.isFinite() || availableWidth <= 0f ||
        expectedCardCount <= 0 ||
        !limits.minimumCardWidth.isFinite() || limits.minimumCardWidth <= 0f ||
        !limits.maximumCanvasHeight.isFinite() || limits.maximumCanvasHeight <= 0f ||
        !limits.maximumLabelSeparation.isFinite() || limits.maximumLabelSeparation < 0f ||
        !minimumReadableLabelWidth.isFinite() || minimumReadableLabelWidth < 0f
    ) {
        return TarotSpreadOverviewMode.OrderedFallback
    }

    val geometry = (result as? SpreadGeometryResult.Success)?.geometry
        ?: return TarotSpreadOverviewMode.OrderedFallback
    if (
        geometry.placements.size != expectedCardCount ||
        !geometry.cardWidth.isFinite() || !geometry.cardHeight.isFinite() ||
        !geometry.canvasExtent.width.isFinite() || !geometry.canvasExtent.height.isFinite() ||
        geometry.cardWidth < limits.minimumCardWidth || geometry.cardHeight <= 0f ||
        geometry.canvasExtent.width <= 0f ||
        geometry.canvasExtent.width > availableWidth ||
        geometry.canvasExtent.height <= 0f ||
        geometry.canvasExtent.height > limits.maximumCanvasHeight
    ) {
        return TarotSpreadOverviewMode.OrderedFallback
    }

    val associationsAreClear = geometry.placements.withIndex().all { (index, placement) ->
        val horizontalOverlap = max(
            0f,
            min(placement.cardBounds.right, placement.labelBounds.right) -
                max(placement.cardBounds.left, placement.labelBounds.left),
        )
        val separation = geometry.labelSeparation(index, overlapAllowance)
        placement.index == index &&
            placement.positionLabel.isNotBlank() &&
            horizontalOverlap > 0f &&
            separation >= 0f &&
            separation <= limits.maximumLabelSeparation
    }
    val allBoundsAreFiniteAndContained = geometry.placements.all { placement ->
        listOf(
            placement.cardBounds,
            placement.transformedCardBounds,
            placement.labelBounds,
        ).all { bounds -> bounds.isFiniteAndContainedBy(geometry.canvasExtent.width, geometry.canvasExtent.height) }
    }
    val labelsAreDistinct = geometry.placements.indices.all { first ->
        (first + 1 until geometry.placements.size).all { second ->
            !geometry.placements[first].labelBounds.intersects(
                geometry.placements[second].labelBounds,
            )
        }
    }
    val labelsHaveReadableWidth = geometry.placements.all { placement ->
        placement.labelBounds.width >= minimumReadableLabelWidth
    }
    val labelsClearEveryCard = geometry.placements.all { labelOwner ->
        geometry.placements.all { cardOwner ->
            !labelOwner.labelBounds.intersects(cardOwner.transformedCardBounds)
        }
    }
    val cardOverlapsAreAllowed = geometry.placements.indices.all { first ->
        (first + 1 until geometry.placements.size).all { second ->
            val overlaps = geometry.placements[first].transformedCardBounds.intersects(
                geometry.placements[second].transformedCardBounds,
            )
            !overlaps || overlapAllowance.allows(first, second)
        }
    }
    return if (
        associationsAreClear &&
        allBoundsAreFiniteAndContained &&
        labelsAreDistinct &&
        labelsHaveReadableWidth &&
        labelsClearEveryCard &&
        cardOverlapsAreAllowed
    ) {
        TarotSpreadOverviewMode.Spatial
    } else {
        TarotSpreadOverviewMode.OrderedFallback
    }
}

private fun com.softcat.mystictarot.SpreadGeometry.labelSeparation(
    index: Int,
    overlapAllowance: TarotSpreadOverviewOverlapAllowance,
): Float {
    if (
        overlapAllowance != TarotSpreadOverviewOverlapAllowance.CelticCenterPair ||
        index !in 0..1 ||
        placements.size < 2
    ) {
        return placements[index].labelBounds.top - placements[index].transformedCardBounds.bottom
    }
    return if (index == 0) {
        placements[index].labelBounds.top -
            max(placements[0].transformedCardBounds.bottom, placements[1].transformedCardBounds.bottom)
    } else {
        min(placements[0].transformedCardBounds.top, placements[1].transformedCardBounds.top) -
            placements[index].labelBounds.bottom
    }
}

private fun TarotSpreadOverviewOverlapAllowance.allows(first: Int, second: Int): Boolean =
    this == TarotSpreadOverviewOverlapAllowance.CelticCenterPair && first == 0 && second == 1

private fun SpreadGeometryRect.intersects(other: SpreadGeometryRect): Boolean =
    min(right, other.right) > max(left, other.left) &&
        min(bottom, other.bottom) > max(top, other.top)

private fun SpreadGeometryRect.isFiniteAndContainedBy(
    canvasWidth: Float,
    canvasHeight: Float,
): Boolean =
    left.isFinite() && top.isFinite() && right.isFinite() && bottom.isFinite() &&
        left >= 0f && top >= 0f && right >= left && bottom >= top &&
        right <= canvasWidth && bottom <= canvasHeight

internal fun combinedTarotCardRotation(slotRotation: Float, directionLabel: String): Float {
    val directionRotation = if (directionLabel == "역방향") 180f else 0f
    if (!slotRotation.isFinite()) return directionRotation
    val combined = (slotRotation + directionRotation) % 360f
    return if (combined < 0f) combined + 360f else combined
}

internal fun minimumSquareReserveForRotatingCard(
    cardWidth: Float,
    cardAspectRatio: Float,
): Float {
    require(cardWidth.isFinite() && cardWidth > 0f)
    require(cardAspectRatio.isFinite() && cardAspectRatio > 0f)
    return hypot(
        cardWidth.toDouble(),
        (cardWidth / cardAspectRatio).toDouble(),
    ).toFloat()
}

internal fun effectiveTarotSpreadGeometryWidth(
    availableWidth: Float,
    cardCount: Int,
    firstCardWidth: Float,
    singleCardMaximumWidth: Float,
): Float {
    if (
        cardCount != 1 ||
        !availableWidth.isFinite() || availableWidth <= 0f ||
        !firstCardWidth.isFinite() || firstCardWidth <= singleCardMaximumWidth ||
        !singleCardMaximumWidth.isFinite() || singleCardMaximumWidth <= 0f
    ) {
        return availableWidth
    }
    return min(availableWidth, singleCardMaximumWidth)
}
