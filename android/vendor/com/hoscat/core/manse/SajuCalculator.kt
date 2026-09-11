package com.hoscat.core.manse

import com.hoscat.core.model.BirthProfileInput
import com.hoscat.core.model.CalendarType
import com.hoscat.core.model.CalculationEvidence
import com.hoscat.core.model.CalculationPolicy
import com.hoscat.core.model.DataTrustLevel
import com.hoscat.core.model.HourPillarPolicy
import com.hoscat.core.model.KOREA_STANDARD_TIME_ZONE_ID
import com.hoscat.core.model.Pillar
import com.hoscat.core.model.SajuChart
import com.hoscat.core.model.calculationBirthDateTime
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

interface SajuCalculator {
    fun calculate(
        input: BirthProfileInput,
        policy: CalculationPolicy = CalculationPolicy(),
    ): SajuChart
}

class SajuCalculationException(
    val userMessage: String,
    cause: Throwable? = null,
) : IllegalArgumentException(userMessage, cause)

class DefaultSajuCalculator(
    private val solarTermDataSource: SolarTermDataSource = EmptySolarTermDataSource(),
    private val lunarDateDataSource: LunarDateDataSource = EmptyLunarDateDataSource(),
    private val dayPillarEpoch: DayPillarEpoch = DayPillarEpoch.unverifiedPreview,
) : SajuCalculator {
    override fun calculate(input: BirthProfileInput, policy: CalculationPolicy): SajuChart {
        val zoneId = runCatching { ZoneId.of(input.birthTimeZoneId) }
            .getOrElse { runCatching { ZoneId.of(input.timezoneId) }.getOrElse { ZoneId.of(KOREA_STANDARD_TIME_ZONE_ID) } }
        validateBirthTime(input)
        val normalizedBirth = normalizeBirth(input)
        val calculationBirth = input.calculationBirthDateTime(normalizedBirth.solarDate)
        val localDate = calculationBirth.toLocalDate()
        val dayPillarDate = when (policy.dayPillarBoundary) {
            com.hoscat.core.model.DayPillarBoundary.LocalMidnight -> localDate
            com.hoscat.core.model.DayPillarBoundary.ZiHour23 -> {
                if (input.birthDateTime.hour != null && calculationBirth.hour == 23) localDate.plusDays(1) else localDate
            }
        }
        validateDataRange(localDate)
        validateDataRange(dayPillarDate)
        val birthInstant = ZonedDateTime.of(
            localDate.year,
            localDate.monthValue,
            localDate.dayOfMonth,
            calculationBirth.hour,
            calculationBirth.minute,
            0,
            0,
            zoneId,
        ).toInstant()
        val relevantTerms = solarTermDataSource.termsForYear(localDate.year - 1) +
            solarTermDataSource.termsForYear(localDate.year) +
            solarTermDataSource.termsForYear(localDate.year + 1)
        val lichun = relevantTerms
            .firstOrNull { it.gregorianYear == localDate.year && it.termIndex == 0 }
        val previousJie = relevantTerms
            .filter { it.termIndex % 2 == 0 && Instant.parse(it.instantUtcIso) <= birthInstant }
            .maxByOrNull { Instant.parse(it.instantUtcIso) }
        if (solarTermDataSource !is EmptySolarTermDataSource && previousJie == null) {
            throw SajuCalculationException("이 날짜는 직전 절기 정보가 부족해 정확한 월주 계산을 할 수 없습니다.")
        }

        val yearForPillar = if (lichun != null && birthInstant < Instant.parse(lichun.instantUtcIso)) {
            localDate.year - 1
        } else {
            localDate.year
        }
        val yearPillar = Sexagenary.yearPillar(yearForPillar)
        val monthBranchIndex = previousJie?.let { it.termIndex / 2 } ?: (localDate.monthValue - 2).floorMod(12)
        val monthPillar = Sexagenary.monthPillar(yearPillar.stem, monthBranchIndex)
        val lunarDate = if (dayPillarDate == normalizedBirth.solarDate) {
            normalizedBirth.lunarDate ?: lunarDateDataSource.dateBySolarDate(localDate.toString())
        } else {
            lunarDateDataSource.dateBySolarDate(dayPillarDate.toString())
        }
        val dayPillar = lunarDate?.ganjiDay?.toPillar("일주")
            ?: fallbackDayPillar(dayPillarDate)
        val hourPillar = input.birthDateTime.hour?.let {
            Sexagenary.hourPillar(dayPillar.stem, hourBranchIndex(calculationBirth.hour, policy.hourPillarPolicy))
        }
        val verified = solarTermDataSource.version.verified && lunarDateDataSource.version.verified
        val pillars = listOfNotNull(yearPillar, monthPillar, dayPillar, hourPillar)

        return SajuChart(
            name = input.name,
            yearPillar = yearPillar,
            monthPillar = monthPillar,
            dayPillar = dayPillar,
            hourPillar = hourPillar,
            dayMaster = "${dayPillar.stem}일간",
            summary = "${dayPillar.stem}일간 · ${dayPillar.stem}${dayPillar.branch}일주를 기준으로 원국과 운의 흐름을 해석합니다.",
            evidence = CalculationEvidence(
                timezoneId = input.birthTimeZoneId,
                policyCode = policy.code,
                yearBoundary = policy.yearPillarBoundary.name,
                monthBoundary = policy.monthPillarBoundary.name,
                hourPolicy = policy.hourPillarPolicy.name,
                dataVersion = "${solarTermDataSource.version.code} / ${lunarDateDataSource.version.code}",
                isVerified = verified,
                note = if (verified) {
                    "검증된 만세력 데이터를 사용했습니다."
                } else {
                    "현재 내장 데이터는 생성 데이터 기준입니다. 공신력 있는 외부 자료와 교차검증 전에는 참고용으로 표시합니다."
                },
                previousSolarTermName = previousJie?.nameKo,
                previousSolarTermAtKst = previousJie?.instantKstIso,
                trustLevel = if (verified) {
                    DataTrustLevel.ExternalAuthorityVerified
                } else {
                    DataTrustLevel.GeneratedUnverified
                },
                externalVerificationSource = if (verified) {
                    solarTermDataSource.version.sourceNote
                } else {
                    null
                },
                normalizedSolarDate = localDate.toString(),
                inputCalendarType = input.calendarType.name,
                calculationBasisLabel = input.calculationBasisLabel,
                birthRegionLabel = input.placeName,
                birthRegionSolarCorrectionMinutes = input.solarCorrectionMinutes,
                birthRegionSolarCorrectionApplied = input.useBirthRegionSolarCorrection,
                birthClockDateTime = input.birthDateTime.toEvidenceString(),
                calculationDateTime = calculationBirth.toString(),
                dayPillarDate = dayPillarDate.toString(),
                solarCorrectionMethod = if (input.useBirthRegionSolarCorrection) {
                    "LONGITUDE_ONLY_NO_EQUATION_OF_TIME"
                } else {
                    "NONE"
                },
            ),
            analysis = SajuAnalysis.analyze(pillars = pillars, dayStem = dayPillar.stem),
        )
    }

    private fun hourBranchIndex(hour: Int, policy: HourPillarPolicy): Int {
        val normalizedHour = hour.coerceIn(0, 23)
        return when (policy) {
            HourPillarPolicy.StandardTwoHourBlocks -> ((normalizedHour + 1) / 2).floorMod(12)
            HourPillarPolicy.LateZiHour -> ((normalizedHour + 1) / 2).floorMod(12)
            HourPillarPolicy.EarlyLateZiSplit -> if (normalizedHour == 23) 0 else ((normalizedHour + 1) / 2).floorMod(12)
        }
    }

    private fun validateBirthTime(input: BirthProfileInput) {
        val birth = input.birthDateTime
        if (birth.hour != null && birth.hour !in 0..23) {
            throw SajuCalculationException("출생 시간은 0시부터 23시 사이여야 합니다.")
        }
        if (birth.minute !in 0..59) {
            throw SajuCalculationException("출생 분은 0분부터 59분 사이여야 합니다.")
        }
    }

    private fun normalizeBirth(input: BirthProfileInput): NormalizedBirth {
        val birth = input.birthDateTime
        return when (input.calendarType) {
            CalendarType.Solar -> {
                val solarDate = runCatching {
                    LocalDate.of(birth.year, birth.month, birth.day)
                }.getOrElse { error ->
                    throw SajuCalculationException("존재하지 않는 생년월일입니다.", error)
                }
                NormalizedBirth(solarDate = solarDate, lunarDate = lunarDateDataSource.dateBySolarDate(solarDate.toString()))
            }

            CalendarType.Lunar -> {
                val lunarDate = lunarDateDataSource.dateByLunarDate(
                    lunarYear = birth.year,
                    lunarMonth = birth.month,
                    lunarDay = birth.day,
                    isLeapMonth = input.isLeapMonth,
                ) ?: throw SajuCalculationException("내장 음력 데이터에서 해당 날짜를 찾을 수 없습니다. 윤달 여부와 날짜를 확인해주세요.")
                NormalizedBirth(solarDate = LocalDate.parse(lunarDate.solarDate), lunarDate = lunarDate)
            }
        }
    }

    private fun validateDataRange(localDate: LocalDate) {
        val lunarRange = lunarDateDataSource.version.lunarDateRange
        val solarRange = solarTermDataSource.version.solarTermRange
        if (localDate.year !in lunarRange) {
            throw SajuCalculationException("내장 음력 데이터 범위(${lunarRange.first}~${lunarRange.last}년)를 벗어난 날짜입니다.")
        }
        if (localDate.year !in solarRange) {
            throw SajuCalculationException("내장 절기 데이터 범위(${solarRange.first}~${solarRange.last}년)를 벗어난 날짜입니다.")
        }
    }

    private fun fallbackDayPillar(localDate: LocalDate): Pillar {
        if (lunarDateDataSource !is EmptyLunarDateDataSource) {
            throw SajuCalculationException("내장 만세력 데이터에서 해당 날짜의 일주를 찾을 수 없습니다.")
        }
        return dayPillarEpoch.pillarFor(localDate.year, localDate.monthValue, localDate.dayOfMonth)
    }

    private fun Int.floorMod(modulus: Int): Int = ((this % modulus) + modulus) % modulus
}

private fun com.hoscat.core.model.BirthDateTime.toEvidenceString(): String {
    val date = "%04d-%02d-%02d".format(year, month, day)
    val time = hour?.let { "%02d:%02d".format(it, minute) } ?: "시간 미상"
    return "$date $time"
}

private data class NormalizedBirth(
    val solarDate: LocalDate,
    val lunarDate: LunarDate?,
)

private fun String.toPillar(label: String): Pillar =
    Pillar(stem = take(1), branch = drop(1).take(1), label = label)

data class DayPillarEpoch(
    val baseYear: Int,
    val baseMonth: Int,
    val baseDay: Int,
    val baseIndex: Int,
    val verified: Boolean,
) {
    fun pillarFor(year: Int, month: Int, day: Int) =
        Sexagenary.pillarAt(baseIndex + daysSinceBase(year, month, day), "일주")

    private fun daysSinceBase(year: Int, month: Int, day: Int): Int =
        ChronoUnit.DAYS.between(
            LocalDate.of(baseYear, baseMonth, baseDay),
            LocalDate.of(year, month, day),
        ).toInt()

    companion object {
        val unverifiedPreview = DayPillarEpoch(
            baseYear = 2000,
            baseMonth = 1,
            baseDay = 1,
            baseIndex = 16,
            verified = false,
        )
    }
}
