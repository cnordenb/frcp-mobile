package net.frcp.app.ui

import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class UiTileTest {
    @Test
    fun t275_roundFitsAWholeNumberOfTilesAlongAnEdge() {
        assertEquals(100f / 3f, tileLength(100f, 38f, Tile.Round), 0.001f)
    }

    @Test
    fun t276_roundKeepsAtLeastOneTileOnAShortEdge() {
        assertEquals(10f, tileLength(10f, 38f, Tile.Round), 0f)
    }

    @Test
    fun t277_repeatKeepsTheTilesAtTheirOwnSize() {
        assertEquals(38f, tileLength(100f, 38f, Tile.Repeat), 0f)
    }
}
