package com.hoscat.mtj.dev

import com.mtj.tarot.MtjTarotBridge
import com.softcat.mystictarot.SpreadDrawMode
import com.softcat.mystictarot.TarotCard
import com.softcat.mystictarot.TarotDeck
import com.softcat.mystictarot.selectableSpreadOptions
import mtj.records.Envelope
import mtj.records.Kind
import mtj.records.Origin
import mtj.records.RecordsJsonCodec
import mtj.records.Value
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TarotResultSummaryTest {
    @Test fun liveAndPersistedSummariesMatchForEveryNormalSpreadWithoutChangingSnapshotQuestion() {
        val deck = TarotDeck("standard", "Test deck", (0..77).map {
            TarotCard(it, "Card $it", "Card $it", "Major Arcana", "Meaning $it")
        })
        val question = "  긴 질문을 그대로 보존합니다.\n두 번째 줄과 | 구분 기호도 유지합니다.  "
        val codec = RecordsJsonCodec()
        for (spread in selectableSpreadOptions.filter { it.drawMode == SpreadDrawMode.Normal }) {
            val bridge = MtjTarotBridge(deck, spread, question, true)
            bridge.lockSelection((0 until spread.cardCount).toList())
            val snapshot = bridge.snapshot()
            val record = RecordSnapshots.tarot(snapshot)
            val saved = codec.decode(codec.encode(record)).tarotResultSummary()

            assertEquals(snapshot.resultSummary(), saved)
            assertEquals("스프레드: ${spread.cardCount}장 | ${spread.title} | ${spread.subtitle}", saved!!.spreadRow)
            assertEquals("질문: ${snapshot.reading.question}", saved.questionRow)
        }
    }

    @Test fun incompleteLegacyRecordsKeepTheirExistingDetailFallback() {
        val record = Envelope(Origin("test", "legacy"), Kind.TAROT, Value.Obj(emptyMap()))
        assertNull(record.tarotResultSummary())
        assertNull(record.copy(kind = Kind.SAJU).tarotResultSummary())
    }
}
