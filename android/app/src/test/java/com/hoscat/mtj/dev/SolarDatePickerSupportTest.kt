package com.hoscat.mtj.dev

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone

class SolarDatePickerSupportTest {
    private val latestDate = LocalDate.of(2026, 9, 9)

    @Test fun utcConversionRoundTripsLeapDayOutsideUtcDefaultTimezone() {
        val date = LocalDate.of(2024, 2, 29)
        val originalTimeZone = TimeZone.getDefault()

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"))
            val utcMillis = Instant.parse("2024-02-29T00:00:00Z").toEpochMilli()

            assertEquals(utcMillis, date.toUtcMillis())
            assertEquals(date, utcMillisToSupportedSolarDate(utcMillis, latestDate))
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }

    @Test fun utcConversionRoundTripsPreEpochDate() {
        val date = LocalDate.of(1965, 7, 20)

        val utcMillis = Instant.parse("1965-07-20T00:00:00Z").toEpochMilli()

        assertEquals(utcMillis, date.toUtcMillis())
        assertEquals(date, utcMillisToSupportedSolarDate(utcMillis, latestDate))
    }

    @Test fun invalidTypedAndUnsupportedDatesAreRejected() {
        assertNull(typedSolarDateToUtcMillis("2023", "2", "29"))
        assertNull(utcMillisToSupportedSolarDate(LocalDate.of(1899, 12, 31).toUtcMillis(), latestDate))
        assertNull(utcMillisToSupportedSolarDate(LocalDate.of(2026, 9, 10).toUtcMillis(), latestDate))
    }

    @Test fun typedPickerSeedRejectsOutOfRangeFutureAndHugeYears() {
        assertNull(typedSolarDateToSupportedUtcMillis("1899", "12", "31", latestDate))
        assertNull(typedSolarDateToSupportedUtcMillis("2026", "9", "10", latestDate))
        assertNull(typedSolarDateToSupportedUtcMillis("99999999999999999999", "1", "1", latestDate))
    }

    @Test fun typedPickerSeedPreservesValidHistoricalMonth() {
        val expected = LocalDate.of(1990, 1, 15).toUtcMillis()

        assertEquals(expected, typedSolarDateToSupportedUtcMillis("1990", "1", "15", latestDate))
    }
}
