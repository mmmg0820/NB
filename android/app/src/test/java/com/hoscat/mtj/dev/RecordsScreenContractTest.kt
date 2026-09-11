package com.hoscat.mtj.dev

import mtj.records.Envelope
import mtj.records.Kind
import mtj.records.Origin
import mtj.records.Value
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RecordsScreenContractTest {
    private val profile = record("profile", Kind.PROFILE)
    private val saju = record("saju", Kind.SAJU)
    private val tarot = record("tarot", Kind.TAROT)
    private val importedSaju = record("imported-saju", Kind.SAJU, "imported")
    private val importedTarot = record("imported-tarot", Kind.TAROT, "imported")
    private val compatibility = record("compatibility", Kind.COMPATIBILITY)
    private val archive = listOf(profile, saju, importedTarot, compatibility, tarot, importedSaju)

    @Test fun filtersKeepApprovedOrder() {
        assertEquals(listOf("전체", "사주", "타로", "기록"), RecordFilter.entries.map { it.label })
    }

    @Test fun allRetainsForeignAndFutureKindsInOriginalOrder() {
        assertEquals(listOf(saju, importedTarot, compatibility, tarot, importedSaju), archive.filter(RecordFilter.ALL::accepts))
    }

    @Test fun kindFiltersIncludeForeignSourceRecords() {
        assertEquals(listOf(saju, importedSaju), archive.filter(RecordFilter.SAJU::accepts))
        assertEquals(listOf(importedTarot, tarot), archive.filter(RecordFilter.TAROT::accepts))
    }

    @Test fun savedRequiresNativeSourceAndReadingOrChartKind() {
        assertEquals(listOf(saju, tarot), archive.filter(RecordFilter.SAVED::accepts))
        assertFalse(RecordFilter.SAVED.accepts(record("similar-source", Kind.SAJU, "mtj-native-imported")))
    }

    @Test fun profileNeverAppearsInAnyFilter() {
        RecordFilter.entries.forEach { assertFalse(it.accepts(profile)) }
    }

    @Test fun emptyStatesLinkToRelevantDestination() {
        assertEquals(1, RecordFilter.ALL.emptyState().targetTab)
        assertEquals(1, RecordFilter.SAJU.emptyState().targetTab)
        assertEquals(2, RecordFilter.TAROT.emptyState().targetTab)
        assertEquals("타로 보기", RecordFilter.TAROT.emptyState().actionLabel)
        assertEquals(1, RecordFilter.SAVED.emptyState().targetTab)
    }

    @Test fun futureKindsAreNotMislabeledAsTarot() {
        assertEquals("사주", recordsKindLabel(Kind.SAJU))
        assertEquals("타로", recordsKindLabel(Kind.TAROT))
        assertEquals("기록", recordsKindLabel(Kind.COMPATIBILITY))
    }

    private fun record(id: String, kind: Kind, source: String = "mtj-native") = Envelope(
        origin = Origin(source, id),
        kind = kind,
        payload = Value.Obj(mapOf("title" to Value.Str(id), "summary" to Value.Str("summary-$id"))),
        profileRefs = when (kind) {
            Kind.SAJU -> listOf(Origin("mtj-native", "profile"))
            Kind.COMPATIBILITY -> listOf(Origin("mtj-native", "profile"), Origin("mtj-native", "profile-b"))
            else -> emptyList()
        },
    )
}
