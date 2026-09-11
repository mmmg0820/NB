package com.hoscat.mtj.dev

import com.hoscat.core.model.BirthInputDraft
import mtj.saju.MtjBirthInputAdapter
import mtj.saju.MtjBirthInputResult
import mtj.records.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class ContractTest {
    @Test fun unknownTimeRemainsUnknown() {
        val result = MtjBirthInputAdapter().parse(
            BirthInputDraft(name = "테스트", year = "2000", month = "1", day = "1"),
            LocalDateTime.of(2026, 9, 9, 0, 0),
        ) as MtjBirthInputResult.Accepted
        assertNull(result.input.birthDateTime.hour)
    }
    @Test fun invalidDateIsNotNormalized() {
        val result = MtjBirthInputAdapter().parse(
            BirthInputDraft(name = "테스트", year = "2000", month = "2", day = "31"),
            LocalDateTime.of(2026, 9, 9, 0, 0),
        )
        assertTrue(result is MtjBirthInputResult.Rejected)
    }
    @Test fun commonRecordRoundTrip() {
        val record = Envelope(Origin("mtj-test", "1"), Kind.TAROT,
            Value.Obj(mapOf("question" to Value.Str("오늘의 선택"))))
        val codec = RecordsJsonCodec()
        assertEquals(record, codec.decode(codec.encode(record)))
    }
    @Test fun duplicateJsonKeysAreRejected() {
        try {
            RecordsJsonCodec().decodeValue("{\"x\":1,\"x\":2}".toByteArray())
            fail("Duplicate key accepted")
        } catch (expected: RecordsCodecException) {
            assertEquals(CodecFailure.DUPLICATE_KEY, expected.failure)
        }
    }
}
