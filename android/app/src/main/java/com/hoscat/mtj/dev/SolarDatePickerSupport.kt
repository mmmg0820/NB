package com.hoscat.mtj.dev

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val FIRST_SUPPORTED_BIRTH_YEAR = 1900
private const val LAST_SUPPORTED_BIRTH_YEAR = 2100

internal fun typedSolarDateToUtcMillis(year: String, month: String, day: String): Long? =
    runCatching {
        LocalDate.of(year.toInt(), month.toInt(), day.toInt()).toUtcMillis()
    }.getOrNull()

internal fun typedSolarDateToSupportedUtcMillis(
    year: String,
    month: String,
    day: String,
    latestDate: LocalDate,
): Long? = typedSolarDateToUtcMillis(year, month, day)?.takeIf { utcMillis ->
    utcMillisToSupportedSolarDate(utcMillis, latestDate) != null
}

internal fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun utcMillisToSupportedSolarDate(
    utcMillis: Long,
    latestDate: LocalDate,
): LocalDate? = runCatching {
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
}.getOrNull()?.takeIf { date ->
    date.year in FIRST_SUPPORTED_BIRTH_YEAR..LAST_SUPPORTED_BIRTH_YEAR && date <= latestDate
}
