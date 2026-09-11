package com.hoscat.mtj.dev

import mtj.records.*
import org.junit.Assert.*
import org.junit.Test

class RecordAdmissionTest {
    private val profile = Envelope(Origin("mtj-native", "profile"), Kind.PROFILE, Value.Obj(emptyMap()))
    private val chart = Envelope(Origin("mtj-native", "chart"), Kind.SAJU,
        Value.Obj(mapOf("title" to Value.Str("명식"))), listOf(profile.origin))

    @Test fun profileAndChartAdmittedTogether() {
        assertEquals(listOf(profile, chart), admittedRecords(emptyList(), listOf(profile, chart)))
    }
    @Test fun retriesDoNotCreateDuplicates() {
        assertTrue(admittedRecords(listOf(profile, chart), listOf(profile, chart)).isEmpty())
    }
    @Test fun missingProfileRejectsWholeBatch() {
        assertThrows(IllegalArgumentException::class.java) { admittedRecords(emptyList(), listOf(chart)) }
    }
    @Test fun changedRecordDoesNotOverwrite() {
        val changed = chart.copy(payload = Value.Obj(mapOf("title" to Value.Str("다름"))))
        assertThrows(IllegalArgumentException::class.java) { admittedRecords(listOf(profile, chart), listOf(changed)) }
    }
    @Test fun sourceIdentityDoesNotCollide() {
        val other = profile.copy(origin = Origin("another-app", "profile"))
        assertEquals(listOf(other), admittedRecords(listOf(profile), listOf(other)))
    }
    @Test fun repeatedBatchOnlyAdmitsOneCopy() {
        assertEquals(listOf(profile, chart), admittedRecords(emptyList(), listOf(profile, chart, profile, chart)))
    }
}
