package com.hoscat.mtj.dev

import com.mtj.tarot.MtjTarotBridge
import com.softcat.mystictarot.*
import mtj.records.Action
import mtj.records.MigrationPlanner
import mtj.records.RecordsJsonCodec
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class TarotRecreationStateTest {
    private val deck = TarotDeck("standard", "Test deck", (0..77).map {
        TarotCard(it, "Card $it", "Card $it", "Major Arcana", "Meaning $it")
    })
    private val spreads = selectableSpreadOptions.filter { it.drawMode == SpreadDrawMode.Normal }
    private val spread = spreads.first { it.cardCount == 3 }

    private fun start(reversed: Boolean = true, seed: Long = 7123456L) =
        TarotRecreationState.start(deck, spread, "  My question  ", reversed, seed)

    // Exercise the exact primitive codec used by the Compose Saver, copying mutable arrays.
    private fun recreate(state: TarotRecreationState): TarotRecreationState =
        TarotRecreationState.restore(state.save().map { if (it is IntArray) it.copyOf() else it })

    private fun rejects(block: () -> Unit) {
        try {
            block()
            fail("Inconsistent restoration must not produce a fresh reading")
        } catch (_: IllegalArgumentException) {
        } catch (_: IllegalStateException) {
        }
    }

    @Test fun draftWithoutSpreadAndEverySelectableSpreadRoundTrip() {
        assertEquals(TarotRecreationState(), recreate(TarotRecreationState()))
        assertNull(recreate(TarotRecreationState()).materialize(deck).bridge)
        for (option in spreads) {
            val state = TarotRecreationState(spreadKey = option.key)
            assertEquals(state, recreate(state))
            assertEquals(option, recreate(state).materialize(deck).spread)
        }
    }

    @Test fun partialSelectionOrderAndWholeDeckSurviveRepeatedRecreation() {
        val initial = start().copy(selectedIds = listOf(41, 3))
        var restored = initial
        repeat(5) {
            restored = recreate(restored)
            assertEquals(initial, restored)
            assertEquals(listOf(41, 3), restored.selectedIds)
            assertEquals(initial.shuffledIds, restored.materialize(deck).bridge!!.shuffledCards.map { it.id })
            assertNull(restored.materialize(deck).result)
            assertEquals("My question", restored.questionAtStart)
            assertTrue(restored.useReversedAtStart)
        }
        // Deselect then reselect appends, just as the screen's ordered selection does.
        restored = restored.copy(selectedIds = restored.selectedIds - 41)
        restored = recreate(restored).copy(selectedIds = restored.selectedIds + 41)
        assertEquals(listOf(3, 41), recreate(restored).selectedIds)
    }

    @Test fun recreationBeforeLockDoesNotChangeDirectionsForEitherReversedSetting() {
        for (reversed in listOf(false, true)) {
            val initial = start(reversed).copy(selectedIds = listOf(77, 2, 18))
            val uninterrupted = MtjTarotBridge(deck, spread, initial.questionAtStart, reversed, Random(initial.seed))
            uninterrupted.lockSelection(initial.selectedIds)
            val restored = recreate(initial).finish(deck).materialize(deck).result!!
            assertEquals(uninterrupted.snapshot().reading.cards, restored.reading.cards)
            assertEquals(reversed, restored.useReversedAtStart)
            if (!reversed) assertTrue(restored.reading.cards.all { it.directionLabel == CardDirection.Upright.label })
        }
    }

    @Test fun completedResultAndRecordIdentityRemainByteIdentical() {
        val completed = start().copy(selectedIds = listOf(77, 2, 18)).finish(deck)
        val before = completed.materialize(deck)
        val beforeRecord = checkNotNull(before.record)
        assertEquals(listOf(77, 2, 18), before.result!!.reading.cards.map { it.cardId })
        val codec = RecordsJsonCodec()
        var restored = completed
        repeat(5) {
            restored = recreate(restored)
            val after = restored.materialize(deck)
            val afterRecord = checkNotNull(after.record)
            assertEquals(before.result, after.result)
            assertEquals(before.record, after.record)
            assertEquals(completed, restored.finish(deck))
            assertArrayEquals(codec.encode(beforeRecord), codec.encode(afterRecord))
            val plan = MigrationPlanner().plan(listOf(beforeRecord), listOf(afterRecord))
            assertTrue(plan.ready)
            assertEquals(Action.SKIP, plan.decisions.single().action)
        }
    }

    @Test fun everyNormalSpreadRestoresAFullSelectionAndResult() {
        for (option in spreads) {
            val state = TarotRecreationState.start(deck, option, "Question", true, -78L)
                .copy(selectedIds = (0 until option.cardCount).reversed().toList())
            val result = recreate(state).finish(deck)
            assertEquals(result.materialize(deck).result, recreate(result).materialize(deck).result)
        }
    }

    @Test fun reshuffleCanPreserveSelectionWhileNewReadingStartsEmpty() {
        val initial = start().copy(selectedIds = listOf(1, 2, 3))
        val selectedPositions = initial.selectedIds.associateWith(initial.shuffledIds::indexOf)
        val reshuffled = initial.reshufflePreservingSelection(deck, seed = 99L)
        assertNotEquals(initial.shuffledIds, reshuffled.shuffledIds)
        assertEquals(listOf(1, 2, 3), reshuffled.selectedIds)
        selectedPositions.forEach { (id, index) -> assertEquals(id, reshuffled.shuffledIds[index]) }
        assertFalse(reshuffled.locked)
        assertEquals("", reshuffled.recordId)
        assertNull(recreate(reshuffled).materialize(deck).result)
        val restarted = TarotRecreationState(spreadKey = initial.spreadKey)
        assertNull(recreate(restarted).materialize(deck).bridge)
        assertFalse(recreate(restarted).started)
    }

    @Test fun backFromResultReopensTheSameDeckAndSelection() {
        val completed = start().copy(selectedIds = listOf(1, 2, 3)).finish(deck)
        val reopened = recreate(completed).reopenSelection()

        assertFalse(reopened.locked)
        assertEquals(completed.shuffledIds, reopened.shuffledIds)
        assertEquals(completed.selectedIds, reopened.selectedIds)
        assertNull(reopened.materialize(deck).result)
        assertEquals(completed.questionAtStart, reopened.questionAtStart)
    }

    @Test fun malformedPrimitiveStateFailsClosedAndFailureSurvivesAnotherRecreation() {
        val saved = start().copy(selectedIds = listOf(41, 3)).save()
        val mutations = listOf<Pair<Int, Any>>(
            0 to 999, 1 to "missing:spread", 1 to "x".repeat(129), 2 to false,
            3 to "not a seed", 4 to "", 4 to "x".repeat(241), 5 to "true", 6 to "bad hash",
            7 to IntArray(78) { 0 }, 7 to IntArray(79) { it },
            8 to intArrayOf(41, 41), 8 to intArrayOf(78), 8 to intArrayOf(1, 2, 3, 4),
            8 to IntArray(11) { it }, 8 to listOf(41, 3),
            9 to true, 10 to 1L, 11 to 1L, 12 to "unexpected", 13 to 1L, 14 to "0".repeat(64),
        )
        val invalid = mutations.map { (index, value) -> saved.toMutableList().also { it[index] = value } } +
            listOf(saved.dropLast(1), saved + "extra", saved.toMutableList().also { it[3] = 0 })
        for (value in invalid + listOf<Any>("bad state", emptyList<Any>())) {
            val failed = TarotRecreationState.restore(value)
            assertTrue("Accepted malformed state: $value", failed.failed)
            assertTrue(recreate(failed).failed)
            rejects { failed.materialize(deck) }
        }
        val nullField = saved.mapIndexed { index, value -> if (index == 4) null else value }
        assertTrue(TarotRecreationState.restore(nullField).failed)
    }

    @Test fun validLookingButInconsistentStateDoesNotReroll() {
        val initial = start().copy(selectedIds = listOf(1, 2, 3))
        val completed = initial.finish(deck)
        rejects { recreate(initial.copy(seed = initial.seed + 1)).materialize(deck) }
        rejects { recreate(initial.copy(shuffledIds = initial.shuffledIds.reversed())).materialize(deck) }
        rejects { recreate(initial.copy(catalogHash = "0".repeat(64))).materialize(deck) }
        rejects { recreate(completed.copy(selectedIds = listOf(3, 2, 1))).materialize(deck) }
        rejects { recreate(completed.copy(useReversedAtStart = false)).materialize(deck) }
        rejects { recreate(completed.copy(readingId = completed.readingId + 1)).materialize(deck) }
        rejects { recreate(completed.copy(readingSavedAt = completed.readingSavedAt + 1)).materialize(deck) }
        rejects { recreate(completed.copy(recordSavedAt = completed.recordSavedAt + 1)).materialize(deck) }
        rejects { recreate(completed.copy(recordId = "00000000-0000-0000-0000-000000000000")).materialize(deck) }
        rejects { recreate(completed.copy(resultHash = "0".repeat(64))).materialize(deck) }
        val changed = deck.copy(cards = deck.cards.map { if (it.id == 1) it.copy(basicMeaning = "Changed") else it })
        rejects { recreate(initial).materialize(changed) }
        rejects { recreate(completed).materialize(changed) }
        rejects { recreate(initial).materialize(deck.copy(cards = deck.cards.reversed())) }
    }

    @Test fun incompleteSelectionCannotBecomeAResult() {
        rejects { recreate(start().copy(selectedIds = listOf(1, 2))).finish(deck) }
        rejects { TarotRecreationState(spreadKey = spread.key).finish(deck) }
    }

    @Test fun savedPayloadHasStrictBoundsAndNoCatalogOrResultText() {
        val largest = spreads.maxBy { it.cardCount }
        val state = TarotRecreationState.start(deck, largest, "Q".repeat(240), true, Long.MIN_VALUE)
            .copy(selectedIds = (0 until largest.cardCount).toList()).finish(deck)
        val saved = state.save()
        assertEquals(15, saved.size)
        assertTrue(saved.all { it is String || it is Boolean || it is Long || it is Int || it is IntArray })
        assertEquals(88, saved.filterIsInstance<IntArray>().sumOf { it.size })
        // Conservative raw UTF-16/primitive budget; actual Parcel overhead is parent device QA.
        val rawBytes = saved.sumOf {
            when (it) {
                is String -> 2 * it.length
                is IntArray -> 4 * it.size
                else -> 8
            }
        }
        assertTrue("Raw session payload was $rawBytes bytes", rawBytes < 2048)
        assertFalse(saved.filterIsInstance<String>().any { it.contains("Meaning") })
        assertEquals(state, recreate(state))
    }
}
