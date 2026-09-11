package com.hoscat.mtj.dev

import com.softcat.mystictarot.SpreadDrawMode
import com.softcat.mystictarot.TarotCard
import com.softcat.mystictarot.TarotDeck
import com.softcat.mystictarot.selectableSpreadOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TarotUiFlowContractTest {
    private val deck = TarotDeck("standard", "Test deck", (0 until 78).map {
        TarotCard(it, "Card $it", "카드 $it", "Major Arcana", "Meaning $it")
    })

    // 질문은 선택 사항이다(2026-09-12 변경) — 빈 질문으로도 리딩을 시작할 수 있다.
    @Test fun emptyQuestionCanBecomeAQuickDefaultReading() {
        val blank = validateTarotQuestion("")
        val whitespace = validateTarotQuestion("   ")

        assertTrue(blank.canStart)
        assertTrue(whitespace.canStart)
        assertNull(blank.error)
        assertNull(whitespace.error)
    }

    @Test fun oneCardSpreadUsesTheSameExplicitSelectionFlow() {
        val spread = selectableSpreadOptions.single {
            it.layoutId == "one_card" && it.drawMode == SpreadDrawMode.Normal
        }

        val started = TarotRecreationState.start(deck, spread, "오늘 내가 볼 신호", reversed = false, seed = 11L)
        val materialized = started.materialize(deck)

        assertTrue(started.started)
        assertEquals(spread.key, started.spreadKey)
        assertEquals("오늘 내가 볼 신호", started.questionAtStart)
        assertEquals(78, materialized.bridge!!.shuffledCards.size)
        assertTrue(started.selectedIds.isEmpty())
        assertNull(materialized.result)

        val selectedId = materialized.bridge.shuffledCards.first().id
        val finished = started.copy(selectedIds = listOf(selectedId)).finish(deck).materialize(deck).result!!

        assertEquals(listOf(selectedId), finished.reading.cards.map { it.cardId })
        assertEquals(listOf("오늘의 메시지"), finished.reading.cards.map { it.positionLabel })
    }

    @Test fun spreadPositionIdentityFollowsSelectedIdOrder() {
        val spread = selectableSpreadOptions.single {
            it.key == "three_cards:past_present_future"
        }
        val started = TarotRecreationState.start(deck, spread, "지금의 선택을 이어가도 될까요?", reversed = false, seed = 22L)
        val selected = started.materialize(deck).bridge!!.shuffledCards.take(3).map { it.id }.reversed()

        val result = started.copy(selectedIds = selected).finish(deck).materialize(deck).result!!

        assertEquals(selected, result.reading.cards.map { it.cardId })
        assertEquals(listOf("과거", "현재", "미래"), result.reading.cards.map { it.positionLabel })
    }
}
