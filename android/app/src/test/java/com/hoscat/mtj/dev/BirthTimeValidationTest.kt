package com.hoscat.mtj.dev

import com.hoscat.core.model.BirthInputDraft
import com.hoscat.core.model.parseBirthProfileInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class BirthTimeValidationTest {
    @Test fun compactTimeParsesHhmmIntoDraftHourMinute() {
        val validation = validateBirthTimeText("0930", isUnknown = false)

        assertFalse(validation.hasErrors)
        assertEquals("9", validation.hour)
        assertEquals("30", validation.minute)
    }

    @Test fun compactTimeRejectsBadShapeAndRangeInUserLanguage() {
        assertEquals(BIRTH_TIME_DIGITS_ERROR, validateBirthTimeText("9:30", isUnknown = false).primaryError)
        assertEquals(BIRTH_TIME_DIGITS_ERROR, validateBirthTimeText("930", isUnknown = false).primaryError)
        assertEquals(BIRTH_HOUR_ERROR, validateBirthTimeText("2400", isUnknown = false).primaryError)
        assertEquals(BIRTH_MINUTE_ERROR, validateBirthTimeText("2360", isUnknown = false).primaryError)
    }

    @Test fun compactTimeErrorsUseUserFacingDisplayCopy() {
        assertEquals("00:00~23:59 사이로 입력해 주세요.", birthTimeDisplayError(BIRTH_HOUR_ERROR))
        assertEquals(null, birthTimeDisplayError(null))
    }

    @Test fun compactUnknownTimeBypassesRetainedText() {
        val validation = validateBirthTimeText("2460", isUnknown = true)

        assertFalse(validation.hasErrors)
    }

    @Test fun uiValidationMatchesCanonicalParser() {
        listOf(
            Case("", ""),
            Case("", "30"),
            Case("12", ""),
            Case("24", ""),
            Case("", "60"),
            Case(" ", "30"),
            Case("12", "\t"),
            Case("0", "0"),
            Case("23", "59"),
            Case("24", "0"),
            Case("23", "60"),
            Case("-1", "0"),
            Case("0", "-1"),
            Case("hour", "0"),
            Case("0", "minute"),
            Case("99999999999999999999", "0"),
            Case("0", "99999999999999999999"),
        ).forEach { case ->
            val ui = validateBirthTime(case.hour, case.minute, isUnknown = false)
            val canonical = parseBirthProfileInput(
                draft = BirthInputDraft(
                    name = "테스트",
                    year = "2000",
                    month = "1",
                    day = "1",
                    hour = case.hour,
                    minute = case.minute,
                ),
                currentKstDateTime = LocalDateTime.of(2026, 9, 9, 0, 0),
            )

            assertEquals("${case.hour}:${case.minute}", canonical.errorMessage, ui.primaryError)
        }
    }

    @Test fun partialPairMarksAndFocusesTheMissingField() {
        val missingHour = validateBirthTime("", "30", isUnknown = false)
        assertEquals(BIRTH_TIME_PAIR_ERROR, missingHour.hourError)
        assertEquals(BirthTimeField.Hour, missingHour.firstInvalidField)

        val missingMinute = validateBirthTime("12", "", isUnknown = false)
        assertEquals(BIRTH_TIME_PAIR_ERROR, missingMinute.minuteError)
        assertEquals(BirthTimeField.Minute, missingMinute.firstInvalidField)
    }

    @Test fun bothBlankKeepsCanonicalUnknownTimePolicy() {
        val validation = validateBirthTime("", "", isUnknown = false)
        assertFalse(validation.hasErrors)
        assertNull(validation.firstInvalidField)
    }

    @Test fun unknownBypassesRetainedKnownTimeDraft() {
        val hidden = validateBirthTime("24", "60", isUnknown = true)
        assertFalse(hidden.hasErrors)

        val retained = validateBirthTime("24", "60", isUnknown = false)
        assertTrue(retained.hasErrors)
        assertEquals(BIRTH_HOUR_ERROR, retained.hourError)
        assertEquals(BIRTH_MINUTE_ERROR, retained.minuteError)
    }

    private data class Case(val hour: String, val minute: String)
}
