package com.hoscat.mtj.dev

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticSaveTest {
    @Test fun `transient failure is retried with the same operation`() = runBlocking {
        var attempts = 0
        val saved = runAutomaticSave(initialDelayMillis = 0) {
            attempts++
            if (attempts < 3) error("temporary")
        }

        assertTrue(saved)
        assertEquals(3, attempts)
    }

    @Test fun `permanent failure stops at bounded attempt count`() = runBlocking {
        var attempts = 0
        val saved = runAutomaticSave(maxAttempts = 2, initialDelayMillis = 0) {
            attempts++
            error("still unavailable")
        }

        assertFalse(saved)
        assertEquals(2, attempts)
    }
}
