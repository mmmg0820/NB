package com.hoscat.mtj.dev

internal const val BIRTH_TIME_PAIR_ERROR =
    "출생 시각은 시와 분을 함께 입력하거나 둘 다 비워주세요."
internal const val BIRTH_TIME_DIGITS_ERROR =
    "태어난 시간은 HHmm 숫자 4자리로 입력해주세요."
internal const val BIRTH_HOUR_ERROR =
    "출생 시간은 0시부터 23시 사이로 입력해주세요."
internal const val BIRTH_MINUTE_ERROR =
    "출생 분은 0분부터 59분 사이로 입력해주세요."

internal enum class BirthTimeField {
    Hour,
    Minute,
}

internal data class BirthTimeValidation(
    val hour: String = "",
    val minute: String = "",
    val hourError: String? = null,
    val minuteError: String? = null,
) {
    val hasErrors: Boolean get() = hourError != null || minuteError != null
    val firstInvalidField: BirthTimeField?
        get() = when {
            hourError != null -> BirthTimeField.Hour
            minuteError != null -> BirthTimeField.Minute
            else -> null
        }
    val primaryError: String? get() = hourError ?: minuteError
}

internal fun validateBirthTimeText(
    birthTime: String,
    isUnknown: Boolean,
): BirthTimeValidation {
    if (isUnknown) return BirthTimeValidation()

    val value = birthTime.trim()
    if (value.isBlank()) return BirthTimeValidation(hourError = BIRTH_TIME_DIGITS_ERROR)
    if (!value.all(Char::isDigit) || value.length != 4) {
        return BirthTimeValidation(hourError = BIRTH_TIME_DIGITS_ERROR)
    }

    val hour = value.substring(0, 2)
    val minute = value.substring(2, 4)
    return validateBirthTime(hour, minute, isUnknown = false)
}

internal fun validateBirthTime(
    hour: String,
    minute: String,
    isUnknown: Boolean,
): BirthTimeValidation {
    if (isUnknown) return BirthTimeValidation()

    val hourBlank = hour.isBlank()
    val minuteBlank = minute.isBlank()
    if (hourBlank && minuteBlank) return BirthTimeValidation()
    if (hourBlank.xor(minuteBlank)) {
        return if (hourBlank) {
            BirthTimeValidation(hourError = BIRTH_TIME_PAIR_ERROR)
        } else {
            BirthTimeValidation(minuteError = BIRTH_TIME_PAIR_ERROR)
        }
    }

    val parsedHour = hour.toIntOrNull()
    val parsedMinute = minute.toIntOrNull()
    val hourError = when {
        parsedHour == null || parsedHour !in 0..23 -> BIRTH_HOUR_ERROR
        else -> null
    }
    val minuteError = when {
        parsedMinute == null || parsedMinute !in 0..59 -> BIRTH_MINUTE_ERROR
        else -> null
    }
    return BirthTimeValidation(
        hour = if (hourError == null) hour.toInt().toString() else "",
        minute = if (minuteError == null) minute.toInt().toString() else "",
        hourError = hourError,
        minuteError = minuteError,
    )
}

internal fun birthTimeDisplayError(error: String?): String? =
    error?.let { "00:00~23:59 사이로 입력해 주세요." }
