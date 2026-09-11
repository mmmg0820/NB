package com.hoscat.core.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

const val SAJU_INPUT_CONTRACT_VERSION = 1

data class BirthInputDraft(
    val name: String = "",
    val calendarType: CalendarType = CalendarType.Solar,
    val isLeapMonth: Boolean = false,
    val gender: Gender = Gender.Unknown,
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val hour: String = "",
    val minute: String = "",
    val birthRegionId: String = KoreanBirthRegions.defaultRegionId,
    val useBirthRegionSolarCorrection: Boolean = false,
)

data class BirthInputParseResult(
    val input: BirthProfileInput?,
    val errorMessage: String?,
)

typealias LunarSolarDateResolver = (
    lunarYear: Int,
    lunarMonth: Int,
    lunarDay: Int,
    isLeapMonth: Boolean,
) -> LocalDate?

fun parseBirthProfileInput(
    draft: BirthInputDraft,
    currentKstDateTime: LocalDateTime = LocalDateTime.now(ZoneId.of(KOREA_STANDARD_TIME_ZONE_ID)),
    resolveLunarSolarDate: LunarSolarDateResolver? = null,
): BirthInputParseResult {
    val year = draft.year.toIntOrNull()
    val month = draft.month.toIntOrNull()
    val day = draft.day.toIntOrNull()
    if (draft.name.isBlank()) return BirthInputParseResult(null, "이름을 입력해주세요.")
    if (year == null || month == null || day == null) {
        return BirthInputParseResult(null, "생년월일을 모두 입력해주세요.")
    }
    validateBirthDate(year, month, day, draft.calendarType)?.let { message ->
        return BirthInputParseResult(null, message)
    }

    val hourBlank = draft.hour.isBlank()
    val minuteBlank = draft.minute.isBlank()
    if (hourBlank.xor(minuteBlank)) {
        return BirthInputParseResult(null, "출생 시각은 시와 분을 함께 입력하거나 둘 다 비워주세요.")
    }
    val hour = if (hourBlank) null else draft.hour.toIntOrNull()
    val minute = if (minuteBlank) 0 else draft.minute.toIntOrNull()
    if (!hourBlank && (hour == null || hour !in 0..23)) {
        return BirthInputParseResult(null, "출생 시간은 0시부터 23시 사이로 입력해주세요.")
    }
    if (!minuteBlank && (minute == null || minute !in 0..59)) {
        return BirthInputParseResult(null, "출생 분은 0분부터 59분 사이로 입력해주세요.")
    }

    val birthDate = when (draft.calendarType) {
        CalendarType.Solar -> LocalDate.of(year, month, day)
        CalendarType.Lunar -> resolveLunarSolarDate?.invoke(year, month, day, draft.isLeapMonth)
            ?: if (resolveLunarSolarDate != null) {
                return BirthInputParseResult(null, "존재하지 않는 음력 날짜입니다.")
            } else {
                null
            }
    }
    validateNotFuture(birthDate, hour, minute ?: 0, currentKstDateTime)?.let { message ->
        return BirthInputParseResult(null, message)
    }

    val region = KoreanBirthRegions.byId(draft.birthRegionId)
    if (draft.useBirthRegionSolarCorrection && region == null) {
        return BirthInputParseResult(null, "출생지역 시간 보정을 켜려면 출생지역을 선택해주세요.")
    }
    val resolvedRegion = region ?: KoreanBirthRegions.defaultRegion()
    val correctionMinutes = if (draft.useBirthRegionSolarCorrection) {
        KoreanBirthRegions.correctionMinutes(resolvedRegion.longitudeEast)
    } else {
        0.0
    }
    return BirthInputParseResult(
        input = BirthProfileInput(
            name = draft.name.trim(),
            birthDateTime = BirthDateTime(year, month, day, hour, minute ?: 0),
            calendarType = draft.calendarType,
            isLeapMonth = draft.calendarType == CalendarType.Lunar && draft.isLeapMonth,
            gender = draft.gender,
            timezoneId = resolvedRegion.timeZoneId,
            placeName = resolvedRegion.displayName,
            birthCountryCode = resolvedRegion.countryCode,
            birthRegionId = resolvedRegion.id,
            birthTimeZoneId = resolvedRegion.timeZoneId,
            useBirthRegionSolarCorrection = draft.useBirthRegionSolarCorrection,
            solarCorrectionMinutes = correctionMinutes,
            calculationBasisLabel = KOREA_STANDARD_TIME_LABEL,
        ),
        errorMessage = null,
    )
}

private fun validateNotFuture(
    birthDate: LocalDate?,
    hour: Int?,
    minute: Int,
    currentKstDateTime: LocalDateTime,
): String? {
    if (birthDate == null) return null
    if (birthDate > currentKstDateTime.toLocalDate()) {
        return "미래의 출생일은 입력할 수 없습니다."
    }
    if (
        birthDate == currentKstDateTime.toLocalDate() &&
        hour != null &&
        LocalTime.of(hour, minute) > currentKstDateTime.toLocalTime()
    ) {
        return "현재 시각 이후의 출생 시간은 입력할 수 없습니다."
    }
    return null
}

private fun validateBirthDate(
    year: Int,
    month: Int,
    day: Int,
    calendarType: CalendarType,
): String? {
    if (year !in 1900..2100) return "지원 범위는 1900년부터 2100년까지입니다."
    if (month !in 1..12) return "월은 1월부터 12월 사이로 입력해주세요."
    if (day !in 1..31) return "일은 1일부터 31일 사이로 입력해주세요."
    return when (calendarType) {
        CalendarType.Solar -> runCatching { LocalDate.of(year, month, day) }.fold(
            onSuccess = { null },
            onFailure = { "존재하지 않는 양력 날짜입니다." },
        )
        CalendarType.Lunar -> if (day > 30) "음력 날짜는 1일부터 30일 사이로 입력해주세요." else null
    }
}
