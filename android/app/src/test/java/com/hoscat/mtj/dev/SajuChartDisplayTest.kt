package com.hoscat.mtj.dev

import androidx.compose.ui.unit.dp
import com.hoscat.core.model.CalculationEvidence
import com.hoscat.core.model.DataTrustLevel
import com.hoscat.core.model.Pillar
import com.hoscat.core.model.SajuChart
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SajuChartDisplayTest {
    @Test fun chartOrderMatchesYearMonthDayHourContract() {
        val displays = sajuChartPillarDisplays(chart())

        assertEquals(listOf("연주", "월주", "일주", "시주"), displays.map { it.role })
        assertEquals(listOf("갑", "병", "무", "경"), displays.map { it.pillar?.stem })
    }

    @Test fun dayStemIsTheOnlyDayMasterTile() {
        val displays = sajuChartPillarDisplays(chart())

        assertEquals(listOf(false, false, true, false), displays.map { it.isDayMaster })
        assertEquals("戊 무", displays.single { it.isDayMaster }.stemText)
    }

    @Test fun glyphsIncludeHanjaHangulAndFiveElementText() {
        val month = sajuChartPillarDisplays(chart()).single { it.role == "월주" }

        assertEquals("丙 병", month.stemText)
        assertEquals("寅 인", month.branchText)
        assertEquals("천간 화 · 지지 목", month.elementText)
    }

    @Test fun unknownHourIsExplicitAndExcludedFromElementCalculation() {
        val hour = sajuChartPillarDisplays(chart(hourPillar = null)).single { it.role == "시주" }

        assertTrue(hour.isUnknownHour)
        assertEquals("모름", hour.stemText)
        assertEquals("모름", hour.branchText)
        assertEquals("", hour.stemHanjaText)
        assertEquals(null, hour.stemKoreanText)
        assertEquals("", hour.branchHanjaText)
        assertEquals(null, hour.branchKoreanText)
        assertEquals("오행 산출 제외", hour.elementText)
        assertEquals("시간 모름", hour.stateText)
    }

    @Test fun pillarGridNeverUsesThreePlusOneLayout() {
        assertEquals(4, sajuPillarColumnCount(392.dp, 88.dp))
        assertEquals(4, sajuPillarColumnCount(320.dp, 92.dp, fontScale = 1f))
        assertEquals(2, sajuPillarColumnCount(320.dp, 92.dp, fontScale = 2f))
        assertEquals(2, sajuPillarColumnCount(411.dp, 92.dp, fontScale = 1.5f))
        assertEquals(2, sajuPillarColumnCount(220.dp, 140.dp))
    }

    @Test fun pillarGridUsesCompactModeForNarrowOrLargeText() {
        assertEquals(SajuPillarGridMode.Standard, sajuPillarGridMode(411.dp, 1.0f))
        assertEquals(SajuPillarGridMode.Compact, sajuPillarGridMode(359.dp, 1.0f))
        assertEquals(SajuPillarGridMode.Compact, sajuPillarGridMode(411.dp, 1.5f))
    }

    @Test fun generatedUnverifiedAuthorityUsesUserFacingLanguage() {
        val subject = chart()

        assertEquals("검토 필요", sajuChartAuthorityLabel(subject))
        assertTrue(sajuChartBasisLabel(subject).contains("대한민국 표준시"))
        assertTrue(sajuChartBasisLabel(subject).contains("검증 상태 별도 표시"))
        assertFalse(sajuChartBasisLabel(subject).contains("myeongri_kr_v2"))
        assertFalse(sajuChartBasisLabel(subject).contains("manse-seed-2026-09-09"))
    }

    @Test fun verifiedAuthorityLabelsSeparateExternalAndInternalStates() {
        assertEquals(
            "외부 기관 검증됨",
            sajuChartAuthorityLabel(chart(
                isVerified = true,
                trustLevel = DataTrustLevel.ExternalAuthorityVerified,
            )),
        )
        assertEquals(
            "내부 구조 검토됨",
            sajuChartAuthorityLabel(chart(trustLevel = DataTrustLevel.InternalStructureChecked)),
        )
    }

    private fun chart(
        hourPillar: Pillar? = Pillar("경", "신", "시주"),
        isVerified: Boolean = false,
        trustLevel: DataTrustLevel = DataTrustLevel.GeneratedUnverified,
    ) = SajuChart(
        name = "테스트",
        yearPillar = Pillar("갑", "자", "연주"),
        monthPillar = Pillar("병", "인", "월주"),
        dayPillar = Pillar("무", "오", "일주"),
        hourPillar = hourPillar,
        dayMaster = "무",
        summary = "요약",
        evidence = CalculationEvidence(
            timezoneId = "Asia/Seoul",
            policyCode = "myeongri_kr_v2",
            yearBoundary = "Lichun",
            monthBoundary = "SolarJieTerms",
            hourPolicy = "StandardTwoHourBlocks",
            dataVersion = "manse-seed-2026-09-09",
            isVerified = isVerified,
            note = "Generated authority fixture is not externally reviewed.",
            trustLevel = trustLevel,
            calculationBasisLabel = "대한민국 표준시",
        ),
        analysis = null,
    )
}
