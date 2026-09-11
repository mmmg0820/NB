package com.hoscat.mtj.dev

import com.softcat.mystictarot.SpreadDrawMode
import com.softcat.mystictarot.selectableSpreadOptions
import org.junit.Assert.assertEquals
import org.junit.Test

class TarotSpreadCategoryContractTest {
    @Test fun eightCategoriesCoverAll27NormalReadingKeysExactlyOnce() {
        val normal = selectableSpreadOptions.filter { it.drawMode == SpreadDrawMode.Normal }
        val grouped = tarotSpreadCategories.flatMap { tarotOptionsForCategory(it, normal) }

        assertEquals(8, tarotSpreadCategories.size)
        assertEquals(27, grouped.size)
        assertEquals(normal.map { it.key }.toSet(), grouped.map { it.key }.toSet())
        assertEquals(grouped.size, grouped.map { it.key }.distinct().size)
    }
}
