package com.softcat.mystictarot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class SpreadGeometryTest {
    private val gaps = SpreadGeometryGaps(
        horizontal = 8f,
        vertical = 10f,
        cardToLabel = 6f,
        labelToLabel = 4f,
    )

    @Test fun everyNormalSpreadIsSafeAtPhoneAndTabletWidths() {
        val normalSpreads = spreadOptions.filter { it.drawMode == SpreadDrawMode.Normal }
        assertTrue(normalSpreads.isNotEmpty())

        for (spread in normalSpreads) {
            for (width in listOf(320f, 840f)) {
                val heights = spread.positionLabels.mapIndexed { index, label ->
                    28f + label.length * 3f + index
                }
                val geometry = readyGeometry(spread, width, heights)
                assertGeometrySafe(spread, width, heights, geometry)
            }
        }
    }

    @Test fun longMeasuredLabelsGrowCanvasWithoutIntersectingOrTruncatingBounds() {
        val spread = spreadOptions.first { it.layoutId == "relationship_clearing" }
        val heights = List(spread.cardCount) { 240f + it * 37f }
        val geometry = readyGeometry(spread, 320f, heights)

        assertGeometrySafe(spread, 320f, heights, geometry)
        assertEquals(heights.maxOrNull()!!, geometry.placements.maxOf { it.labelBounds.height }, 0.001f)
        assertTrue(geometry.canvasExtent.height > heights.maxOrNull()!!)
    }

    @Test fun cardWidthAndHorizontalSilhouetteAreInvariantAcrossTwoPassMeasurement() {
        for (spread in spreadOptions.filter { it.drawMode == SpreadDrawMode.Normal }) {
            val firstPass = readyGeometry(spread, 360f, List(spread.cardCount) { 0f })
            val measuredHeights = spread.positionLabels.mapIndexed { index, label ->
                36f + label.length * 5f + index * 7f
            }
            val secondPass = readyGeometry(spread, 360f, measuredHeights)

            assertEquals(firstPass.cardWidth, secondPass.cardWidth, 0f)
            assertEquals(firstPass.cardHeight, secondPass.cardHeight, 0f)
            firstPass.placements.zip(secondPass.placements).forEach { (first, second) ->
                assertEquals(first.index, second.index)
                assertEquals(first.rotationDegrees, second.rotationDegrees, 0f)
                assertEquals(first.cardBounds.left, second.cardBounds.left, 0f)
                assertEquals(first.cardBounds.right, second.cardBounds.right, 0f)
                assertEquals(first.cardBounds.height, second.cardBounds.height, 0.001f)
                assertEquals(first.transformedCardBounds.left, second.transformedCardBounds.left, 0f)
                assertEquals(first.transformedCardBounds.right, second.transformedCardBounds.right, 0f)
                assertEquals(first.transformedCardBounds.height, second.transformedCardBounds.height, 0.001f)
                if (spread.layoutId == "mini_celtic" || spread.layoutId == "celtic_cross") {
                    assertTrue(second.cardBounds.top >= first.cardBounds.top)
                } else {
                    assertEquals(first.cardBounds, second.cardBounds)
                    assertEquals(first.transformedCardBounds, second.transformedCardBounds)
                }
            }
            assertGeometrySafe(spread, 360f, measuredHeights, secondPass)
        }
    }

    @Test fun miniAndFullCelticReserveVariedLabelBandsAndKeepLabelsAttached() {
        for (layoutId in listOf("mini_celtic", "celtic_cross")) {
            val spread = spreadOptions.first { it.layoutId == layoutId }
            val heights = List(spread.cardCount) { index -> 34f + index * 19f }
            val geometry = readyGeometry(spread, 375f, heights)
            val centerPairBottom = max(
                geometry.placements[0].transformedCardBounds.bottom,
                geometry.placements[1].transformedCardBounds.bottom,
            )
            val centerPairTop = min(
                geometry.placements[0].transformedCardBounds.top,
                geometry.placements[1].transformedCardBounds.top,
            )

            assertGeometrySafe(spread, 375f, heights, geometry)
            geometry.placements.forEachIndexed { index, placement ->
                val separation = when (index) {
                    0 -> placement.labelBounds.top - centerPairBottom
                    1 -> centerPairTop - placement.labelBounds.bottom
                    else -> placement.labelBounds.top - placement.transformedCardBounds.bottom
                }
                assertEquals(gaps.cardToLabel, separation, 0.001f)
                assertTrue(intersectionWidth(placement.cardBounds, placement.labelBounds) > 0f)
            }
            assertEquals(0f, intersectionArea(
                geometry.placements[0].labelBounds,
                geometry.placements[1].labelBounds,
            ), 0f)
        }
    }

    @Test fun miniAndFullCelticKeepOnlyTheIntentionalRotatedCoincidentPair() {
        for (layoutId in listOf("mini_celtic", "celtic_cross")) {
            val spread = spreadOptions.first { it.layoutId == layoutId }
            val heights = List(spread.cardCount) { 44f }
            val geometry = readyGeometry(spread, 360f, heights)

            assertGeometrySafe(spread, 360f, heights, geometry)
            assertEquals(0f, geometry.placements[0].rotationDegrees, 0f)
            assertEquals(90f, geometry.placements[1].rotationDegrees, 0f)
            assertEquals(
                geometry.placements[1].cardBounds.height,
                geometry.placements[1].transformedCardBounds.width,
                0.001f,
            )
            assertEquals(
                geometry.placements[1].cardBounds.width,
                geometry.placements[1].transformedCardBounds.height,
                0.001f,
            )
            assertTrue(intersectionArea(
                geometry.placements[0].transformedCardBounds,
                geometry.placements[1].transformedCardBounds,
            ) > 0f)
        }
    }

    @Test fun invalidParametersFailClosed() {
        val spread = spreadOptions.first { it.cardCount == 3 }
        val heights = List(spread.cardCount) { 32f }
        val invalidCalls = listOf(
            { calculateSpreadGeometry(spread, Float.NaN, 0.62f, heights, gaps) },
            { calculateSpreadGeometry(spread, Float.POSITIVE_INFINITY, 0.62f, heights, gaps) },
            { calculateSpreadGeometry(spread, 0f, 0.62f, heights, gaps) },
            { calculateSpreadGeometry(spread, 320f, 0f, heights, gaps) },
            { calculateSpreadGeometry(spread, 320f, Float.NaN, heights, gaps) },
            { calculateSpreadGeometry(spread, 320f, 0.62f, heights.dropLast(1), gaps) },
            { calculateSpreadGeometry(spread, 320f, 0.62f, heights.toMutableList().also { it[0] = -1f }, gaps) },
            { calculateSpreadGeometry(spread, 320f, 0.62f, heights, gaps.copy(horizontal = -1f)) },
            { calculateSpreadGeometry(spread, 320f, 0.62f, heights, gaps.copy(cardToLabel = Float.NaN)) },
        )

        invalidCalls.forEach { call ->
            assertEquals(
                SpreadGeometryStatus.InvalidParameters,
                (call() as SpreadGeometryResult.Unsupported).status,
            )
        }
    }

    @Test fun malformedDefinitionAndImpossibleWidthReturnExplicitUnsupportedStatuses() {
        val celtic = spreadOptions.first { it.layoutId == "celtic_cross" }
        val malformed = celtic.copy(
            cardCount = 11,
            positionLabels = celtic.positionLabels + "extra",
        )
        assertEquals(
            SpreadGeometryStatus.DefinitionMismatch,
            (calculateSpreadGeometry(malformed, 360f, 0.62f, List(11) { 30f }, gaps)
                as SpreadGeometryResult.Unsupported).status,
        )
        val malformedMini = spreadOptions.first { it.layoutId == "mini_celtic" }.copy(
            cardCount = 1,
            positionLabels = listOf("only"),
        )
        assertEquals(
            SpreadGeometryStatus.DefinitionMismatch,
            (calculateSpreadGeometry(malformedMini, 360f, 0.62f, listOf(30f), gaps)
                as SpreadGeometryResult.Unsupported).status,
        )

        val threeCards = spreadOptions.first { it.cardCount == 3 }
        assertEquals(
            SpreadGeometryStatus.NoCollisionFreeLayout,
            (calculateSpreadGeometry(threeCards, 1f, 0.62f, List(3) { 30f }, gaps)
                as SpreadGeometryResult.Unsupported).status,
        )
    }

    private fun readyGeometry(
        spread: SpreadOption,
        width: Float,
        labelHeights: List<Float>,
    ): SpreadGeometry {
        val result = calculateSpreadGeometry(spread, width, 0.62f, labelHeights, gaps)
        if (result !is SpreadGeometryResult.Success) {
            fail("${spread.key} at $width returned ${result.status}")
        }
        return (result as SpreadGeometryResult.Success).geometry
    }

    private fun assertGeometrySafe(
        spread: SpreadOption,
        availableWidth: Float,
        labelHeights: List<Float>,
        geometry: SpreadGeometry,
    ) {
        assertEquals(spread.cardCount, geometry.placements.size)
        assertEquals(0.62f, geometry.cardWidth / geometry.cardHeight, 0.0001f)
        assertTrue(geometry.cardWidth.isFinite())
        assertTrue(geometry.cardHeight.isFinite())
        assertTrue(geometry.canvasExtent.width.isFinite())
        assertTrue(geometry.canvasExtent.height.isFinite())
        assertTrue(geometry.canvasExtent.width <= availableWidth)
        assertTrue(geometry.canvasExtent.width > 0f)
        assertTrue(geometry.canvasExtent.height > 0f)

        val slots = spreadSlots(spread, spread.cardCount)
        geometry.placements.forEachIndexed { index, placement ->
            assertEquals(index, placement.index)
            assertEquals(spread.positionLabels[index], placement.positionLabel)
            assertEquals(slots[index].rotation, placement.rotationDegrees, 0f)
            assertEquals(labelHeights[index], placement.labelBounds.height, 0.001f)
            for (rect in listOf(placement.cardBounds, placement.transformedCardBounds, placement.labelBounds)) {
                assertTrue(rect.left.isFinite())
                assertTrue(rect.top.isFinite())
                assertTrue(rect.right.isFinite())
                assertTrue(rect.bottom.isFinite())
                assertTrue(rect.left >= 0f)
                assertTrue(rect.top >= 0f)
                assertTrue(rect.right <= geometry.canvasExtent.width)
                assertTrue(rect.bottom <= geometry.canvasExtent.height)
            }
        }

        for (label in geometry.placements) {
            for (card in geometry.placements) {
                assertEquals(0f, intersectionArea(label.labelBounds, card.transformedCardBounds), 0f)
            }
        }
        for (first in geometry.placements.indices) {
            for (second in first + 1 until geometry.placements.size) {
                assertEquals(
                    0f,
                    intersectionArea(
                        geometry.placements[first].labelBounds,
                        geometry.placements[second].labelBounds,
                    ),
                    0f,
                )
                val cardIntersection = intersectionArea(
                    geometry.placements[first].transformedCardBounds,
                    geometry.placements[second].transformedCardBounds,
                )
                val intentional =
                    spread.layoutId in setOf("mini_celtic", "celtic_cross") && first == 0 && second == 1
                if (intentional) assertTrue(cardIntersection > 0f)
                else assertEquals(0f, cardIntersection, 0f)
            }
        }
    }

    private fun intersectionWidth(first: SpreadGeometryRect, second: SpreadGeometryRect): Float =
        max(0f, min(first.right, second.right) - max(first.left, second.left))

    private fun intersectionArea(first: SpreadGeometryRect, second: SpreadGeometryRect): Float =
        intersectionWidth(first, second) *
            max(0f, min(first.bottom, second.bottom) - max(first.top, second.top))
}
