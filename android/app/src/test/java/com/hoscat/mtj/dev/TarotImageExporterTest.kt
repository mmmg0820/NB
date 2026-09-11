package com.hoscat.mtj.dev

import org.junit.Assert.assertEquals
import org.junit.Test

class TarotImageExporterTest {
    @Test fun `export grid keeps common spreads compact`() {
        assertEquals(TarotExportGrid(1, 1), tarotExportGrid(1))
        assertEquals(TarotExportGrid(2, 1), tarotExportGrid(2))
        assertEquals(TarotExportGrid(3, 1), tarotExportGrid(3))
        assertEquals(TarotExportGrid(2, 2), tarotExportGrid(4))
        assertEquals(TarotExportGrid(3, 4), tarotExportGrid(10))
    }
}
