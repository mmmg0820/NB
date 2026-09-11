package com.hoscat.mtj.dev

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TarotSelectionGridContractTest {
    @Test fun all78CardsUseEightColumnsAndTenRows() {
        val compact = tarotOverviewGridSpec(360f, 420f)
        val wide = tarotOverviewGridSpec(600f, 420f)

        assertEquals(TarotOverviewGridSpec(columns = 8, rows = 10), compact)
        assertEquals(TarotOverviewGridSpec(columns = 8, rows = 10), wide)
        assertEquals(listOf(8, 8, 8, 8, 8, 8, 8, 8, 8, 6), tarotOverviewRowSizes(78, 8))
    }

    @Test fun firstTapSelectsAndSecondTapCancels() {
        val initial = TarotSelectionState((0 until 78).toList(), emptyList(), requiredCount = 3)
        val selected = toggleTarotSelection(initial, 12)
        val cancelled = toggleTarotSelection(selected, 12)

        assertEquals(listOf(12), selected.selectedIds)
        assertEquals(emptyList<Int>(), cancelled.selectedIds)
    }

    @Test fun cancellingReindexesRemainingSelectionsAndClosesCompletion() {
        val complete = TarotSelectionState((0 until 78).toList(), listOf(12, 7, 31), requiredCount = 3)
        val reopened = toggleTarotSelection(complete, 7)

        assertEquals(listOf(12, 31), reopened.selectedIds)
        assertTrue(reopened.selectedIds.size < reopened.requiredCount)
    }

    @Test fun limitAndUnknownIdsAreIgnored() {
        val complete = TarotSelectionState(listOf(1, 2, 3), listOf(1, 2), requiredCount = 2)

        assertEquals(complete, toggleTarotSelection(complete, 3))
        assertEquals(complete, toggleTarotSelection(complete, 99))
    }
}
