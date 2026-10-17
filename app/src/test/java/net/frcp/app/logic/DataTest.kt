package net.frcp.app.logic

import net.frcp.app.Assets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

// data.json and the asset files it names: what the app needs to find for every edition, DLC and civ. These fail when
// the web version adds a civ, edition or DLC and the app's assets weren't synced (./gradlew syncWebAssets).
class DataTest {
    private fun exists(rel: String) = File(ASSETS, rel).isFile
    private val yearOf = { ed: String -> logic.editionInfo.getValue(ed).year }

    @Test
    fun `civ IDs run from 0 with no gaps or repeats, and names are unique`() {
        val data = GameData.parse(DATA_JSON)
        assertEquals(data.civs.indices.toList(), data.civs.map { it.id }.sorted())
        assertEquals(data.civs.size, data.civs.map { it.name }.toSet().size)
    }

    @Test
    fun `every civ's DLC and every DLC's edition exist`() {
        val data = GameData.parse(DATA_JSON)
        for (c in data.civs) assertTrue("${c.name}'s DLC ${c.dlc} isn't in data.json", c.dlc in logic.dlcs)
        for (d in logic.dlcs.values) assertTrue("DLC ${d.key} has unknown edition ${d.edition}", d.edition in EDITIONS)
    }

    @Test
    fun `editions have unique IDs and years, as which civs an edition has goes by year`() {
        assertEquals(EDITIONS.size, EDITIONS.toSet().size)
        assertEquals(EDITIONS.size, EDITIONS.map(yearOf).toSet().size)
    }

    @Test
    fun `each edition's DLC toggles and older DLCs are DLCs it has`() {
        for (ed in EDITIONS) {
            val info = logic.editionInfo.getValue(ed)
            for (dlc in info.dlcToggles + info.olderDlcs) {
                assertTrue("$ed names unknown DLC $dlc", dlc in logic.dlcs)
                assertTrue("$ed names $dlc, a DLC of a newer edition", yearOf(logic.dlcs.getValue(dlc).edition) <= yearOf(ed))
            }
        }
    }

    @Test
    fun `every civ has an icon and a jingle, and a legacy jingle where its edition has them`() {
        for (c in CIVS) {
            assertTrue("no icon for ${c.name}", exists(Assets.civIcon(c.file)))
            assertTrue("no jingle for ${c.name}", exists(Assets.jingle(c.file, false)))
            if (c.legacy) assertTrue("no legacy jingle for ${c.name}", exists(Assets.jingle(c.file, true)))
        }
        for (f in listOf(Assets.civIcon(Assets.RANDOM), Assets.jingle(Assets.RANDOM, false), Assets.jingle(Assets.RANDOM, true))) {
            assertTrue("no $f", exists(f))
        }
    }

    @Test
    fun `the civ icon atlas has a cell for every civ and random`() {
        val rows = (CIVS.size + 1 + Assets.CIV_ATLAS_COLS - 1) / Assets.CIV_ATLAS_COLS
        assertEquals(Assets.CIV_ATLAS_COLS * 30 to rows * 30, webpSize(Assets.CIV_ATLAS))
    }

    @Test
    fun `the DLC icon atlas has cells for every edition's three looks and every DLC icon`() {
        val cells = 3 * EDITIONS.size + logic.dlcs.values.count { it.hasIcon }
        val rows = (cells + Assets.DLC_ATLAS_COLS - 1) / Assets.DLC_ATLAS_COLS
        assertEquals(Assets.DLC_ATLAS_COLS * 128 to rows * 128, webpSize(Assets.DLC_ATLAS))
    }

    @Test
    fun `the banner atlas stacks every edition's banner`() {
        val (w, h) = webpSize(Assets.BANNER_ATLAS)
        assertEquals(128, w)
        assertEquals(EDITIONS.size * 93, h)
    }
}
