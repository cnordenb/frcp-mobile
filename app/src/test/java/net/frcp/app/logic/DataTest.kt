package net.frcp.app.logic

import net.frcp.app.device.Assets
import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.io.File

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DataTest {
    private val data = GameData.parse(DATA_JSON)
    private val none = emptyList<String>()

    private fun exists(rel: String) = File(ASSETS, rel).isFile
    private fun yearOf(ed: String) = logic.editionInfo.getValue(ed).year
    private fun namedDlcs(ed: String) = logic.editionInfo.getValue(ed).let { it.dlcToggles + it.olderDlcs }

    @Test
    fun t011_civIdsRunFromZeroWithoutGapsOrRepeats() {
        assertEquals(data.civs.indices.toList(), data.civs.map { it.id }.sorted())
    }

    @Test
    fun t012_civNamesAreUnique() {
        assertEquals(data.civs.size, data.civs.map { it.name }.toSet().size)
    }

    @Test
    fun t013_everyCivsDlcExists() {
        assertEquals(none, data.civs.filter { it.dlc !in logic.dlcs }.map { "${it.name}: ${it.dlc}" })
    }

    @Test
    fun t014_everyDlcsEditionExists() {
        assertEquals(none, logic.dlcs.values.filter { it.edition !in EDITIONS }.map { "${it.key}: ${it.edition}" })
    }

    @Test
    fun t015_editionIdsAreUnique() {
        assertEquals(EDITIONS.size, EDITIONS.toSet().size)
    }

    @Test
    fun t016_editionYearsAreUnique() {
        assertEquals(EDITIONS.size, EDITIONS.map(::yearOf).toSet().size)
    }

    @Test
    fun t017_editionsNameOnlyKnownDlcs() {
        val unknown = EDITIONS.flatMap { ed -> namedDlcs(ed).filter { it !in logic.dlcs }.map { "$ed: $it" } }
        assertEquals(none, unknown)
    }

    @Test
    fun t018_editionsNameNoDlcOfANewerEdition() {
        val newer = EDITIONS.flatMap { ed ->
            namedDlcs(ed).filter { dlc -> logic.dlcs[dlc]?.let { yearOf(it.edition) > yearOf(ed) } == true }
                .map { "$ed: $it" }
        }
        assertEquals(none, newer)
    }

    @Test
    fun t019_everyCivHasAnIcon() {
        assertEquals(none, CIVS.filter { !exists(Assets.civIcon(it.file)) }.map { it.name })
    }

    @Test
    fun t020_everyCivHasAJingle() {
        assertEquals(none, CIVS.filter { !exists(Assets.jingle(it.file, false)) }.map { it.name })
    }

    @Test
    fun t021_everyLegacyCivHasALegacyJingle() {
        assertEquals(none, CIVS.filter { it.legacy && !exists(Assets.jingle(it.file, true)) }.map { it.name })
    }

    @Test
    fun t022_randomHasItsIconAndJingles() {
        val files = listOf(Assets.RANDOM_ICON, Assets.jingle(Assets.RANDOM, false), Assets.jingle(Assets.RANDOM, true))
        assertEquals(none, files.filter { !exists(it) })
    }

    @Test
    fun t023_civIconsAreOfRandomsSize() {
        val random = webpSize(Assets.RANDOM_ICON)
        assertEquals(none, CIVS.filter { pngSize(Assets.civIcon(it.file)) != random }.map { it.name })
    }

    @Test
    fun t024_dlcAtlasHasACellForEveryEditionLookAndDlcIcon() {
        val cells = 3 * EDITIONS.size + logic.dlcs.values.count { it.hasIcon }
        val rows = (cells + Assets.DLC_ATLAS_COLS - 1) / Assets.DLC_ATLAS_COLS
        assertEquals(Assets.DLC_ATLAS_COLS * 128 to rows * 128, webpSize(Assets.DLC_ATLAS))
    }

    @Test
    fun t025_bannerAtlasStacksEveryEditionsBanner() {
        assertEquals(128 to EDITIONS.size * 93, webpSize(Assets.BANNER_ATLAS))
    }
}
