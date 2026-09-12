package com.hoscat.mtj.dev

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

internal suspend fun runAutomaticSave(
    maxAttempts: Int = 3,
    initialDelayMillis: Long = 250,
    insert: suspend () -> Unit,
): Boolean {
    require(maxAttempts > 0)
    repeat(maxAttempts) { attempt ->
        try {
            insert()
            return true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (attempt + 1 < maxAttempts) delay(initialDelayMillis * (attempt + 1))
        }
    }
    return false
}
