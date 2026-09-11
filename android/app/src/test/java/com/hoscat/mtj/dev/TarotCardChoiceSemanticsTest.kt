package com.hoscat.mtj.dev

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.SemanticsModifier
import androidx.compose.ui.semantics.SemanticsProperties
import com.softcat.mystictarot.TarotCard
import com.softcat.mystictarot.TarotDeck
import com.softcat.mystictarot.selectableSpreadOptions
import org.junit.Assert.*
import org.junit.Test

class TarotCardChoiceSemanticsTest {
    private fun config(index: Int, enabled: Boolean = true, action: () -> Unit = {}): SemanticsConfiguration =
        checkNotNull(Modifier.tarotCardChoiceSemantics(76, index, enabled, action)
            .foldIn<SemanticsConfiguration?>(null) { current, element ->
                (element as? SemanticsModifier)?.semanticsConfiguration ?: current
            })

    @Test fun selectedAndUnselectedCardsExposeNameRoleAndActionOnOneClearingNode() {
        for (index in listOf(-1, 0, 2)) {
            var taps = 0
            val semantics = config(index) { taps++ }
            assertTrue(semantics.isClearingSemantics)
            assertEquals(listOf("카드 77"), semantics[SemanticsProperties.ContentDescription])
            assertEquals(Role.Button, semantics[SemanticsProperties.Role])
            assertEquals(index >= 0, semantics[SemanticsProperties.Selected])
            assertEquals(if (index >= 0) "${index + 1}번째 선택" else "선택 안 됨",
                semantics[SemanticsProperties.StateDescription])
            val click = semantics[SemanticsActions.OnClick]
            assertEquals(if (index >= 0) "선택 취소" else "카드 선택", click.label)
            assertEquals(true, click.action!!.invoke())
            assertEquals(1, taps)
        }
    }

    @Test fun disabledUnselectedCardCannotInvokeItsActionAtFullCount() {
        var taps = 0
        val semantics = config(-1, enabled = false) { taps++ }
        assertTrue(semantics.contains(SemanticsProperties.Disabled))
        assertEquals(false, semantics[SemanticsActions.OnClick].action!!.invoke())
        assertEquals(0, taps)
    }

    @Test fun accessibilityRetapAfterResultBackDeselectsLastRowCardAndClosesCompletion() {
        val deck = TarotDeck("standard", "Test deck", (0..77).map {
            TarotCard(it, "Card $it", "Card $it", "Major Arcana", "Meaning $it")
        })
        val spread = selectableSpreadOptions.single { it.key == "three_cards:past_present_future" }
        val started = TarotRecreationState.start(deck, spread, "Question", false, seed = 78L)
        val ids = listOf(started.shuffledIds[0], started.shuffledIds[1], started.shuffledIds[76])
        val shuffled = started.copy(selectedIds = ids).reshufflePreservingSelection(deck, seed = 99L)
        val completed = shuffled.finish(deck)
        var session = TarotRecreationState.restore(completed.save()).reopenSelection()
        val semantics = config(session.selectedIds.indexOf(ids.last())) {
            val next = toggleTarotSelection(
                TarotSelectionState(session.shuffledIds, session.selectedIds, spread.cardCount), ids.last(),
            )
            session = session.copy(selectedIds = next.selectedIds)
        }
        assertEquals(true, semantics[SemanticsActions.OnClick].action!!.invoke())
        assertEquals(ids.take(2), session.selectedIds)
        assertTrue(session.selectedIds.size < spread.cardCount)
        assertEquals(shuffled.shuffledIds, session.shuffledIds)
        assertEquals(shuffled.seed, session.seed)
        assertEquals(shuffled.questionAtStart, session.questionAtStart)
        assertFalse(session.locked)
        assertNull(session.materialize(deck).result)
        assertThrows(IllegalArgumentException::class.java) { session.finish(deck) }
    }
}
