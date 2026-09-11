package com.hoscat.mtj.dev

import org.junit.Assert.assertEquals
import org.junit.Test

class TarotSpreadLabelFormatterTest {
    @Test fun keepsTheFullCardNameAndForwardDirectionOnSeparateLines() {
        val label = formatTarotSpreadOverviewLabel(
            order = 10,
            positionLabel = "현재의 상황에서 가장 중요하게 살펴볼 아주 긴 위치 이름",
            cardName = "펜타클 페이지",
            directionLabel = "정방향",
        )

        assertEquals(
            "10. 현재의 상황에서 가장 중요하게 살펴볼 아주 긴 위치 이름\n펜타클 페이지\n정방향",
            label.text,
        )
        assertEquals("10. 현재의 상황에서 가장 중요하게 살펴볼 아주 긴 위치 이름", label.positionLine)
        assertEquals("펜타클 페이지", label.cardNameLine)
        assertEquals("정방향", label.directionLine)
        assertEquals(listOf("펜타클", "페이지", "정방향"), label.readableWidthCandidates)
    }

    @Test fun keepsTheFullReversedDirectionAsItsOwnLine() {
        val label = formatTarotSpreadOverviewLabel(
            order = 2,
            positionLabel = "도전과 교차하는 영향",
            cardName = "펜타클 페이지",
            directionLabel = "역방향",
        )

        assertEquals("2. 도전과 교차하는 영향\n펜타클 페이지\n역방향", label.text)
        assertEquals(listOf("펜타클", "페이지", "역방향"), label.readableWidthCandidates)
    }
}
