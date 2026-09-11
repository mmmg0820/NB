package com.hoscat.mtj.dev

import com.hoscat.core.model.CalendarType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BirthDateValidationTest {
    private val today = LocalDate.of(2026, 9, 10)

    @Test fun compactSolarDateParsesYyyyMmDd() {
        val validation = validateBirthDateText("19900123", latestDate = today, calendarType = CalendarType.Solar)

        assertFalse(validation.hasError)
        assertEquals("1990", validation.year)
        assertEquals("1", validation.month)
        assertEquals("23", validation.day)
    }

    @Test fun compactDateRejectsNonEightDigitInput() {
        assertEquals(BIRTH_DATE_REQUIRED_ERROR, validateBirthDateText("", today, CalendarType.Solar).error)
        assertEquals(BIRTH_DATE_DIGITS_ERROR, validateBirthDateText("1990-01-23", today, CalendarType.Solar).error)
        assertEquals(BIRTH_DATE_DIGITS_ERROR, validateBirthDateText("1990012", today, CalendarType.Solar).error)
    }

    @Test fun compactDateErrorsUseUserFacingDisplayCopy() {
        assertEquals("생년월일을 확인해 주세요.", birthDateDisplayError(BIRTH_DATE_DIGITS_ERROR))
        assertEquals(null, birthDateDisplayError(null))
    }

    @Test fun compactSolarDateRejectsImpossibleAndFutureDates() {
        assertEquals(BIRTH_DATE_INVALID_ERROR, validateBirthDateText("20250230", today, CalendarType.Solar).error)
        assertEquals(BIRTH_DATE_RANGE_ERROR, validateBirthDateText("20260911", today, CalendarType.Solar).error)
    }

    @Test fun compactLunarDateUsesLunarDayRangeWithoutSolarMonthShape() {
        val validLunar = validateBirthDateText("20250230", today, CalendarType.Lunar)

        assertFalse(validLunar.hasError)
        assertTrue(validateBirthDateText("20250231", today, CalendarType.Lunar).hasError)
    }
}
