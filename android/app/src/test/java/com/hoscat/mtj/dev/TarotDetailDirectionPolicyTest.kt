package com.hoscat.mtj.dev

import org.junit.Assert.assertEquals
import org.junit.Test

class TarotDetailDirectionPolicyTest {
    @Test fun mapsOnlyTheReversedSnapshotDirectionToHalfTurn() {
        assertEquals(0f, detailTarotCardRotation("정방향"), 0f)
        assertEquals(180f, detailTarotCardRotation("역방향"), 0f)
        assertEquals(0f, detailTarotCardRotation(""), 0f)
        assertEquals(0f, detailTarotCardRotation("unknown"), 0f)
    }

    @Test fun overviewStillCombinesSlotRotationWhileDetailUsesDirectionOnly() {
        assertEquals(180f, detailTarotCardRotation("역방향"), 0f)
        assertEquals(270f, combinedTarotCardRotation(90f, "역방향"), 0f)
        assertEquals(90f, combinedTarotCardRotation(90f, "정방향"), 0f)
    }
}
