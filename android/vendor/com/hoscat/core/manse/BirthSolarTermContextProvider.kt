package com.hoscat.core.manse

import com.hoscat.core.model.BirthProfileInput
import com.hoscat.core.model.KOREA_STANDARD_TIME_ZONE_ID
import com.hoscat.core.model.calculationBirthDateTime
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class BirthSolarTermContext(
    val birthInstant: Instant,
    val previousJie: SolarTerm?,
    val nextJie: SolarTerm?,
    val previousJieDistance: Duration?,
    val nextJieDistance: Duration?,
)

class BirthSolarTermContextProvider(
    private val solarTermDataSource: SolarTermDataSource,
) {
    fun contextFor(input: BirthProfileInput): BirthSolarTermContext {
        val birth = input.calculationBirthDateTime()
        val zoneId = runCatching { ZoneId.of(input.birthTimeZoneId) }
            .getOrElse { runCatching { ZoneId.of(input.timezoneId) }.getOrElse { ZoneId.of(KOREA_STANDARD_TIME_ZONE_ID) } }
        val birthInstant = ZonedDateTime.of(
            birth.year,
            birth.monthValue,
            birth.dayOfMonth,
            birth.hour,
            birth.minute,
            0,
            0,
            zoneId,
        ).toInstant()
        val terms = solarTermDataSource.termsForYear(birth.year - 1) +
            solarTermDataSource.termsForYear(birth.year) +
            solarTermDataSource.termsForYear(birth.year + 1)
        val jieTerms = terms.filter { it.termIndex % 2 == 0 }
        val previous = jieTerms
            .filter { Instant.parse(it.instantUtcIso) <= birthInstant }
            .maxByOrNull { Instant.parse(it.instantUtcIso) }
        val next = jieTerms
            .filter { Instant.parse(it.instantUtcIso) > birthInstant }
            .minByOrNull { Instant.parse(it.instantUtcIso) }

        return BirthSolarTermContext(
            birthInstant = birthInstant,
            previousJie = previous,
            nextJie = next,
            previousJieDistance = previous?.let {
                Duration.between(Instant.parse(it.instantUtcIso), birthInstant)
            },
            nextJieDistance = next?.let {
                Duration.between(birthInstant, Instant.parse(it.instantUtcIso))
            },
        )
    }
}
