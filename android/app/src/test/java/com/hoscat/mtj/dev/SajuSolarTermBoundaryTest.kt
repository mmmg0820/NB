package com.hoscat.mtj.dev

import com.hoscat.core.manse.DefaultSajuCalculator
import com.hoscat.core.manse.ManseDataVersion
import com.hoscat.core.manse.SolarTerm
import com.hoscat.core.manse.SolarTermDataSource
import com.hoscat.core.model.BirthDateTime
import com.hoscat.core.model.BirthProfileInput
import org.junit.Assert.assertEquals
import org.junit.Test

class SajuSolarTermBoundaryTest {
    @Test fun `month pillar changes at solar jie term instead of lunar month`() {
        val calculator = DefaultSajuCalculator(solarTermDataSource = Fixed1996Terms)

        assertEquals("묘", calculator.calculate(input(4)).monthPillar.branch)
        assertEquals("진", calculator.calculate(input(6)).monthPillar.branch)
    }

    private fun input(day: Int) = BirthProfileInput(
        name = "테스트",
        birthDateTime = BirthDateTime(1996, 4, day, 12, 0),
    )

    private object Fixed1996Terms : SolarTermDataSource {
        override val version = ManseDataVersion("test", 1995..1997, 1900..2100, "test", false)
        override fun termsForYear(gregorianYear: Int): List<SolarTerm> = if (gregorianYear == 1996) listOf(
            term(0, "입춘", "1996-02-04T13:00:00Z"),
            term(2, "경칩", "1996-03-05T13:00:00Z"),
            term(4, "청명", "1996-04-05T13:00:00Z"),
        ) else emptyList()

        private fun term(index: Int, name: String, utc: String) = SolarTerm(
            gregorianYear = 1996,
            termIndex = index,
            nameKo = name,
            solarLongitudeDeg = index * 15.0,
            instantUtcIso = utc,
            instantKstIso = utc,
            source = "test",
            sourceVersion = "test",
        )
    }
}
