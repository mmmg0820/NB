package com.hoscat.mtj.dev

import com.softcat.mystictarot.SpreadDrawMode
import com.softcat.mystictarot.SpreadGeometry
import com.softcat.mystictarot.SpreadGeometryExtent
import com.softcat.mystictarot.SpreadGeometryGaps
import com.softcat.mystictarot.SpreadGeometryPlacement
import com.softcat.mystictarot.SpreadGeometryRect
import com.softcat.mystictarot.SpreadGeometryResult
import com.softcat.mystictarot.SpreadGeometryStatus
import com.softcat.mystictarot.calculateSpreadGeometry
import com.softcat.mystictarot.spreadOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class TarotSpreadOverviewPolicyTest {
    private val limits = TarotSpreadOverviewLimits(
        minimumCardWidth = 40f,
        maximumCanvasHeight = 600f,
        maximumLabelSeparation = 24f,
    )

    @Test fun acceptsReadableBoundedGeometryWithAttachedNumberedLabels() {
        assertEquals(
            TarotSpreadOverviewMode.Spatial,
            chooseTarotSpreadOverviewMode(readyGeometry(), 320f, 2, limits),
        )
    }

    @Test fun ordinarySizeReadableWidthKeepsMiniAndFullCelticSpatial() {
        val gaps = SpreadGeometryGaps(8f, 10f, 6f, 4f)
        val pixel10ContentWidth = 375f
        val ordinaryFontReadableWidth = 44f
        val celticLimits = limits.copy(maximumCanvasHeight = 1000f)

        for (layoutId in listOf("mini_celtic", "celtic_cross")) {
            val spread = spreadOptions.first {
                it.drawMode == SpreadDrawMode.Normal && it.layoutId == layoutId
            }
            val typicalKoreanLabelHeights = spread.positionLabels.map { label ->
                72f + label.length * 4f
            }
            val result = calculateSpreadGeometry(
                spread,
                pixel10ContentWidth,
                0.62f,
                typicalKoreanLabelHeights,
                gaps,
            )

            assertEquals(
                "$layoutId should keep its spatial overview",
                TarotSpreadOverviewMode.Spatial,
                chooseTarotSpreadOverviewMode(
                    result,
                    pixel10ContentWidth,
                    spread.cardCount,
                    celticLimits,
                    tarotSpreadOverviewOverlapAllowance(spread.layoutId),
                    minimumReadableLabelWidth = ordinaryFontReadableWidth,
                ),
            )
        }
    }

    @Test fun largeFontReadableWidthKeepsMiniSpatialAndFallsBackForClassicAt360() {
        val gaps = SpreadGeometryGaps(8f, 10f, 6f, 4f)
        val compactContentWidth = 360f
        val largeFontReadableWidth = 57.2f
        val celticLimits = limits.copy(maximumCanvasHeight = 1000f)

        for (layoutId in listOf("mini_celtic", "celtic_cross")) {
            val spread = spreadOptions.first {
                it.drawMode == SpreadDrawMode.Normal && it.layoutId == layoutId
            }
            val result = calculateSpreadGeometry(
                spread,
                compactContentWidth,
                0.62f,
                spread.positionLabels.map { label -> 72f + label.length * 4f },
                gaps,
            )
            val expected = if (layoutId == "mini_celtic") {
                TarotSpreadOverviewMode.Spatial
            } else {
                TarotSpreadOverviewMode.OrderedFallback
            }

            assertEquals(
                "$layoutId should be spatial before the readable-width requirement",
                TarotSpreadOverviewMode.Spatial,
                chooseTarotSpreadOverviewMode(
                    result,
                    compactContentWidth,
                    spread.cardCount,
                    celticLimits,
                    tarotSpreadOverviewOverlapAllowance(spread.layoutId),
                ),
            )

            assertEquals(
                "$layoutId should respect its available label width",
                expected,
                chooseTarotSpreadOverviewMode(
                    result,
                    compactContentWidth,
                    spread.cardCount,
                    celticLimits,
                    tarotSpreadOverviewOverlapAllowance(spread.layoutId),
                    minimumReadableLabelWidth = largeFontReadableWidth,
                ),
            )
        }
    }

    @Test fun readableLabelWidthBoundaryIsInclusive() {
        val geometry = readyGeometry(labelWidth = 48f)

        assertEquals(
            TarotSpreadOverviewMode.Spatial,
            chooseTarotSpreadOverviewMode(
                geometry,
                320f,
                2,
                limits,
                minimumReadableLabelWidth = 48f,
            ),
        )
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(
                geometry,
                320f,
                2,
                limits,
                minimumReadableLabelWidth = 48.001f,
            ),
        )
    }

    @Test fun invalidReadableLabelWidthsFallBack() {
        for (minimumWidth in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -1f)) {
            assertEquals(
                TarotSpreadOverviewMode.OrderedFallback,
                chooseTarotSpreadOverviewMode(
                    readyGeometry(),
                    320f,
                    2,
                    limits,
                    minimumReadableLabelWidth = minimumWidth,
                ),
            )
        }
    }

    @Test fun acceptsOnlyTheExplicitCelticCenterPairAllowance() {
        val coincidentCenterPair = readyGeometry(coincidentCenterPair = true)
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(coincidentCenterPair, 320f, 2, limits),
        )
        assertEquals(
            TarotSpreadOverviewMode.Spatial,
            chooseTarotSpreadOverviewMode(
                coincidentCenterPair,
                320f,
                2,
                limits,
                TarotSpreadOverviewOverlapAllowance.CelticCenterPair,
            ),
        )
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(
                geometryWithArbitraryOverlap(),
                320f,
                3,
                limits,
                TarotSpreadOverviewOverlapAllowance.CelticCenterPair,
            ),
        )
        assertEquals(
            TarotSpreadOverviewOverlapAllowance.None,
            tarotSpreadOverviewOverlapAllowance("relationship_clearing"),
        )
    }

    @Test fun fallsBackForUnsupportedTinyOrOversizedGeometry() {
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(
                SpreadGeometryResult.Unsupported(SpreadGeometryStatus.NoCollisionFreeLayout),
                320f,
                2,
                limits,
            ),
        )
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(readyGeometry(cardWidth = 39f), 320f, 2, limits),
        )
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(readyGeometry(canvasHeight = 601f), 320f, 2, limits),
        )
    }

    @Test fun fallsBackWhenALabelIsDetachedOverlappingOrTheContractIsAmbiguous() {
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(readyGeometry(labelTop = 145f), 320f, 2, limits),
        )
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(readyGeometry(overlappingLabels = true), 320f, 2, limits),
        )
        assertEquals(
            TarotSpreadOverviewMode.OrderedFallback,
            chooseTarotSpreadOverviewMode(readyGeometry(), 320f, 3, limits),
        )
    }

    @Test fun combinesSlotAndDirectionRotations() {
        assertEquals(90f, combinedTarotCardRotation(90f, "정방향"), 0f)
        assertEquals(270f, combinedTarotCardRotation(90f, "역방향"), 0f)
        assertEquals(350f, combinedTarotCardRotation(-10f, "정방향"), 0f)
        assertEquals(180f, combinedTarotCardRotation(Float.NaN, "역방향"), 0f)
    }

    @Test fun squareReserveContainsTheCardAtEveryWholeDegree() {
        val width = 40f
        val height = width / 0.62f
        val reserve = minimumSquareReserveForRotatingCard(width, 0.62f)

        for (degrees in 0 until 360) {
            val radians = Math.toRadians(degrees.toDouble())
            val transformedWidth = abs(width.toDouble() * cos(radians)) +
                abs(height.toDouble() * sin(radians))
            val transformedHeight = abs(width.toDouble() * sin(radians)) +
                abs(height.toDouble() * cos(radians))
            assertTrue(transformedWidth <= reserve.toDouble() + 0.001)
            assertTrue(transformedHeight <= reserve.toDouble() + 0.001)
        }
    }

    @Test fun singleCardGeometryWidthIsCappedOnlyWhenOversized() {
        assertEquals(112f, effectiveTarotSpreadGeometryWidth(320f, 1, 319f, 112f, 420f), 0f)
        assertEquals(100f, effectiveTarotSpreadGeometryWidth(100f, 1, 99f, 112f, 420f), 0f)
        assertEquals(320f, effectiveTarotSpreadGeometryWidth(320f, 1, 110f, 112f, 420f), 0f)
    }

    @Test fun twoAndThreeCardResultsFitNarrowScreensAndCapWideScreens() {
        for (cardCount in 2..3) {
            assertEquals(320f, effectiveTarotSpreadGeometryWidth(320f, cardCount, 96f, 112f, 420f), 0f)
            assertEquals(420f, effectiveTarotSpreadGeometryWidth(420f, cardCount, 130f, 112f, 420f), 0f)
            assertEquals(420f, effectiveTarotSpreadGeometryWidth(840f, cardCount, 260f, 112f, 420f), 0f)
        }
    }

    @Test fun fourAndMoreCardResultsKeepFullAvailableWidth() {
        for (cardCount in listOf(4, 5, 7, 10, 12)) {
            assertEquals(320f, effectiveTarotSpreadGeometryWidth(320f, cardCount, 60f, 112f, 420f), 0f)
            assertEquals(840f, effectiveTarotSpreadGeometryWidth(840f, cardCount, 140f, 112f, 420f), 0f)
        }
    }

    @Test fun invalidAdaptiveWidthInputsLeaveAvailableWidthUnchanged() {
        assertEquals(840f, effectiveTarotSpreadGeometryWidth(840f, 0, 140f, 112f, 420f), 0f)
        assertEquals(840f, effectiveTarotSpreadGeometryWidth(840f, 1, Float.NaN, 112f, 420f), 0f)
        assertEquals(840f, effectiveTarotSpreadGeometryWidth(840f, 1, 140f, 0f, 420f), 0f)
        assertEquals(840f, effectiveTarotSpreadGeometryWidth(840f, 3, 140f, 112f, Float.POSITIVE_INFINITY), 0f)
        assertEquals(0f, effectiveTarotSpreadGeometryWidth(0f, 3, 140f, 112f, 420f), 0f)
    }

    private fun readyGeometry(
        cardWidth: Float = 80f,
        canvasHeight: Float = 240f,
        labelTop: Float = 112f,
        coincidentCenterPair: Boolean = false,
        overlappingLabels: Boolean = false,
        labelWidth: Float = 80f,
    ): SpreadGeometryResult {
        val cardTop = if (coincidentCenterPair) 60f else 0f
        val firstCardLeft = if (coincidentCenterPair) 60f else 0f
        val secondCardLeft = if (coincidentCenterPair) 60f else 120f
        val firstLabelLeft = when {
            overlappingLabels -> 40f
            coincidentCenterPair -> 60f
            else -> 0f
        }
        val secondLabelLeft = when {
            overlappingLabels -> 100f
            coincidentCenterPair -> 60f
            else -> 120f
        }
        val firstLabelTop = if (coincidentCenterPair) 172f else labelTop
        val secondLabelTop = if (coincidentCenterPair) 8f else labelTop
        return SpreadGeometryResult.Success(
            SpreadGeometry(
                cardWidth = cardWidth,
                cardHeight = 129f,
                placements = listOf(
                    placement(
                        0,
                        firstCardLeft,
                        cardTop,
                        firstLabelLeft,
                        firstLabelTop,
                        labelWidth = labelWidth,
                    ),
                    placement(
                        1,
                        secondCardLeft,
                        cardTop,
                        secondLabelLeft,
                        secondLabelTop,
                        labelWidth = labelWidth,
                    ),
                ),
                canvasExtent = SpreadGeometryExtent(200f, canvasHeight),
            )
        )
    }

    private fun geometryWithArbitraryOverlap(): SpreadGeometryResult = SpreadGeometryResult.Success(
        SpreadGeometry(
            cardWidth = 80f,
            cardHeight = 129f,
            placements = listOf(
                placement(0, 60f, 60f, 60f, 172f, labelHeight = 10f),
                placement(1, 60f, 60f, 60f, 38f, labelHeight = 10f),
                placement(2, 60f, 60f, 60f, 184f, labelHeight = 10f),
            ),
            canvasExtent = SpreadGeometryExtent(200f, 240f),
        )
    )

    private fun placement(
        index: Int,
        cardLeft: Float,
        cardTop: Float,
        labelLeft: Float,
        labelTop: Float,
        labelHeight: Float = 40f,
        labelWidth: Float = 80f,
    ) = SpreadGeometryPlacement(
        index = index,
        positionLabel = "${index + 1}번째 위치",
        rotationDegrees = 0f,
        cardBounds = SpreadGeometryRect(cardLeft, cardTop, cardLeft + 80f, cardTop + 100f),
        transformedCardBounds = SpreadGeometryRect(cardLeft, cardTop, cardLeft + 80f, cardTop + 100f),
        labelBounds = SpreadGeometryRect(labelLeft, labelTop, labelLeft + labelWidth, labelTop + labelHeight),
    )
}
