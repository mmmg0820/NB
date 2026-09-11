package com.hoscat.mtj.dev

import com.hoscat.core.model.CalculationEvidence
import com.hoscat.core.model.DataTrustLevel
import com.hoscat.core.model.Pillar
import com.hoscat.core.model.SajuChart
import org.junit.Assert.assertEquals
import org.junit.Test

class SajuReadingTitleTest {
    @Test fun `title uses month branch and full day pillar`() {
        assertEquals("인월무신", sajuReadingTitle(chart()))
    }

    private fun chart() = SajuChart(
        name = "테스트",
        yearPillar = Pillar("갑", "자", "연주"),
        monthPillar = Pillar("임", "인", "월주"),
        dayPillar = Pillar("무", "신", "일주"),
        hourPillar = null,
        dayMaster = "무",
        summary = "요약",
        evidence = CalculationEvidence(
            timezoneId = "Asia/Seoul",
            policyCode = "myeongri_kr_v2",
            yearBoundary = "Lichun",
            monthBoundary = "SolarJieTerms",
            hourPolicy = "StandardTwoHourBlocks",
            dataVersion = "test",
            isVerified = false,
            note = "test",
            trustLevel = DataTrustLevel.InternalStructureChecked,
            calculationBasisLabel = "절기 기준",
        ),
        analysis = null,
    )
}
