package com.hoscat.mtj.dev

import com.hoscat.core.model.CalendarType
import java.time.LocalDate

internal const val BIRTH_DATE_REQUIRED_ERROR =
    "생년월일 8자리를 입력해주세요."
internal const val BIRTH_DATE_DIGITS_ERROR =
    "생년월일은 숫자 8자리로 입력해주세요."
internal const val BIRTH_DATE_RANGE_ERROR =
    "지원 범위는 1900년부터 오늘까지입니다."
internal const val BIRTH_DATE_INVALID_ERROR =
    "존재하지 않는 날짜입니다."
internal const val BIRTH_DATE_FORMAT_ERROR =
    "생년월일은 숫자 8자리로 입력해주세요."

internal data class BirthDateValidation(
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val error: String? = null,
) {
    val hasError: Boolean get() = error != null
}

internal fun validateBirthDateText(
    birthDate: String,
    latestDate: LocalDate,
    calendarType: CalendarType = CalendarType.Solar,
): BirthDateValidation {
    val value = birthDate.trim()
    if (value.isEmpty()) return BirthDateValidation(error = BIRTH_DATE_REQUIRED_ERROR)
    if (!value.all(Char::isDigit) || value.length != 8) {
        return BirthDateValidation(error = BIRTH_DATE_DIGITS_ERROR)
    }

    val year = value.substring(0, 4).toInt()
    val month = value.substring(4, 6).toInt()
    val day = value.substring(6, 8).toInt()
    if (year !in 1900..latestDate.year) return BirthDateValidation(error = BIRTH_DATE_RANGE_ERROR)
    if (month !in 1..12 || day !in 1..31) return BirthDateValidation(error = BIRTH_DATE_INVALID_ERROR)
    if (calendarType == CalendarType.Lunar && day > 30) return BirthDateValidation(error = BIRTH_DATE_INVALID_ERROR)
    if (calendarType == CalendarType.Solar) {
        val parsed = runCatching { LocalDate.of(year, month, day) }.getOrNull()
            ?: return BirthDateValidation(error = BIRTH_DATE_INVALID_ERROR)
        if (parsed > latestDate) return BirthDateValidation(error = BIRTH_DATE_RANGE_ERROR)
    }
    if (calendarType == CalendarType.Lunar && year == latestDate.year && month > latestDate.monthValue) {
        return BirthDateValidation(error = BIRTH_DATE_RANGE_ERROR)
    }
    return BirthDateValidation(
        year = year.toString(),
        month = month.toString(),
        day = day.toString(),
    )
}

internal fun validateCompactBirthDate(
    birthDate: String,
    calendarType: CalendarType = CalendarType.Solar,
    latestDate: LocalDate,
): BirthDateValidation {
    val validation = validateBirthDateText(birthDate, latestDate, calendarType)
    val error = when (validation.error) {
        BIRTH_DATE_REQUIRED_ERROR, BIRTH_DATE_DIGITS_ERROR -> BIRTH_DATE_FORMAT_ERROR
        BIRTH_DATE_INVALID_ERROR -> if (calendarType == CalendarType.Solar) {
            "존재하지 않는 양력 날짜입니다."
        } else {
            BIRTH_DATE_INVALID_ERROR
        }
        BIRTH_DATE_RANGE_ERROR -> "미래의 출생일은 입력할 수 없습니다."
        else -> validation.error
    }
    return validation.copy(error = error)
}

internal fun birthDateDisplayError(error: String?): String? =
    error?.let { "생년월일을 확인해 주세요." }

internal fun LocalDate.toBirthDateText(): String =
    "%04d%02d%02d".format(year, monthValue, dayOfMonth)
