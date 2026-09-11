package com.hoscat.mtj.dev

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TarotQuestionValidationTest {
    @Test fun emptyQuestionCannotStart() {
        val validation = validateTarotQuestion("")
        assertEquals(0, validation.codePointCount)
        assertFalse(validation.canStart)
        assertEquals("질문을 입력해주세요.", validation.error)
    }

    @Test fun whitespaceOnlyQuestionCannotStart() {
        val validation = validateTarotQuestion("  \n\t  ")
        assertEquals(6, validation.codePointCount)
        assertFalse(validation.canStart)
        assertEquals("질문을 입력해주세요.", validation.error)
    }

    @Test fun questionAt239CodePointsCanStart() {
        val validation = validateTarotQuestion("a".repeat(239))
        assertEquals(239, validation.codePointCount)
        assertTrue(validation.canStart)
        assertNull(validation.error)
    }

    @Test fun questionAt240CodePointsCanStart() {
        val validation = validateTarotQuestion("a".repeat(240))
        assertEquals(240, validation.codePointCount)
        assertTrue(validation.canStart)
        assertNull(validation.error)
    }

    @Test fun questionAt241CodePointsCannotStart() {
        val validation = validateTarotQuestion("a".repeat(241))
        assertEquals(241, validation.codePointCount)
        assertFalse(validation.canStart)
        assertEquals("질문은 최대 240자까지 입력할 수 있습니다.", validation.error)
    }

    @Test fun koreanTextCountsByCodePoint() {
        val validation = validateTarotQuestion("오늘의 선택은 무엇일까요?")
        assertEquals("오늘의 선택은 무엇일까요?".codePointCount(0, "오늘의 선택은 무엇일까요?".length), validation.codePointCount)
        assertTrue(validation.canStart)
    }

    @Test fun emojiUsesOneCodePointRatherThanTwoUtf16Units() {
        val question = "a".repeat(239) + "😀"
        val validation = validateTarotQuestion(question)
        assertEquals(240, validation.codePointCount)
        assertEquals(241, question.length)
        assertTrue(validation.canStart)
    }

    @Test fun multilineTextCountsNewlinesAndCanStart() {
        val validation = validateTarotQuestion("첫째 줄\n둘째 줄")
        assertEquals(9, validation.codePointCount)
        assertTrue(validation.canStart)
        assertNull(validation.error)
    }

    @Test fun inputAcceptsUpTo4096CodePointsAndRejectsLargerReplacement() {
        assertTrue(acceptsTarotQuestionInput("a".repeat(4096)))
        assertFalse(acceptsTarotQuestionInput("a".repeat(4097)))
    }
}
