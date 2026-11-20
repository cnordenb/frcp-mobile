package net.frcp.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.frcp.app.Assets
import net.frcp.app.PickerViewModel
import net.frcp.app.logic.Civ
import net.frcp.app.logic.Edition
import net.frcp.app.logic.Logic

/** The DLC toggles left out to make room for the civs, which never scroll. Their civs still toggle one by one. */
internal val HIDDEN_DLC_TOGGLES = setOf("west", "dukes", "india")

/** A row of civs at full size, as frcp.net's phone layout has them. */
private val CIV_ROW = 24.dp

/** The DLC toggles the Pool tab shows for an edition, top to bottom. */
internal fun shownDlcToggles(edition: Edition) = edition.dlcToggles.filter { it !in HIDDEN_DLC_TOGGLES }

/**
 * The Pool tab's civs in rows of three: every civ in its place in the alphabetical order of them all, and null in the
 * places of the civs newer than the edition [ed], so no civ moves when the edition changes.
 */
internal fun civGrid(logic: Logic, ed: String): List<List<Civ?>> =
    logic.displayOrder.map { if (logic.inEdition(it, ed)) it else null }.chunked(3)

/**
 * The Pool tab: the editions beside the edition's banner, Auto-toggle beside the count, the edition's DLC toggles in
 * two columns but for [HIDDEN_DLC_TOGGLES], then the civs in three columns, and the buttons at the bottom. Every civ
 * keeps its place in the alphabetical order of them all, so the civs of newer editions leave theirs empty. The civs
 * never scroll: when the screen is too short for their rows at full size, the rows shrink, icons and names with them.
 */
@Composable
fun PoolTab(vm: PickerViewModel, pageTop: Dp) {
    val logic = vm.logic
    val view = vm.view
    val settings = vm.settings
    val ed = view.state.edition
    val info = logic.editionInfo.getValue(ed)
    val images = LocalImages.current
    val dlcAtlas = images[Assets.DLC_ATLAS]
    val dlcIcons = remember(logic) { logic.dlcs.values.filter { it.hasIcon }.map { it.key } }
    val civRows = remember(logic, ed) { civGrid(logic, ed) }
    fun isOn(id: Int) = id !in view.state.off

    Column(
        Modifier.fillMaxSize().padding(top = 58.dp + pageTop, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row {
            Column(Modifier.weight(1f).selectableGroup(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                logic.editions.forEachIndexed { i, e -> EditionRow(i, "${logic.editionInfo.getValue(e).short} (${logic.editionInfo.getValue(e).year})", e == ed) { vm.setEdition(e) } }
            }
            val banners = images[Assets.BANNER_ATLAS]
            val index = logic.editions.indexOf(ed)
            Box(
                Modifier
                    .size(128.dp, 93.dp)
                    .drawBehind {
                        val h = banners.height / logic.editions.size
                        drawPart(banners, 0, index * h, banners.width, h)
                    }
                    .clickable(null, null, role = Role.Button, onClick = vm::openEditionPage)
                    .semantics { contentDescription = if (info.url != null) "${info.series}: ${info.name} on Steam" else "About ${info.name}" },
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            CheckRow("Auto-toggle older civs", settings.autoToggle, vm::toggleAutoToggle, Modifier.weight(1f))
            Label("Enabled: ${view.pool.size}/${view.editionTotal}", bold = true)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            for (pair in shownDlcToggles(info).chunked(2)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (dlc in pair) {
                        val on = logic.civs.any { it.dlc == dlc && logic.inEdition(it, ed) && isOn(it.id) }
                        Toggle(logic.dlcs.getValue(dlc).name, on, { vm.toggleDlc(dlc) }, Modifier.weight(1f).height(maxOf(info.toggleSize, 22).dp), info.toggleGap.dp) {
                            ToggleIcon(dlcAtlas, on, info.toggleSize.dp, 3 * logic.editions.size + dlcIcons.indexOf(dlc), Assets.DLC_ATLAS_COLS)
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val scale = minOf(1f, maxHeight / (CIV_ROW * civRows.size))
            Column {
                for (row in civRows) {
                    Row(Modifier.height(CIV_ROW * scale), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (c in row) {
                            if (c != null) {
                                CivToggle(c, isOn(c.id), images[Assets.civIcon(c.file)], scale, { vm.toggleCiv(c.id) }, Modifier.weight(1f).fillMaxHeight())
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ParchmentButton("Enable All", vm::enableAll, size = DpSize(90.dp, 30.dp))
                    ParchmentButton("Disable All", vm::disableAll, size = DpSize(90.dp, 30.dp))
                }
                CheckRow("Auto-reset upon change", settings.autoReset, vm::toggleAutoReset)
            }
            ShareButton("Share a link to this civ pool", { vm.shareLink(false) })
        }
    }
}

/**
 * An edition's row, with its icon where the desktop app has a radio circle. The icon shows its pressed look, the
 * third cell of the edition's three in the DLC atlas, while pressed or selected.
 */
@Composable
private fun EditionRow(index: Int, text: String, selected: Boolean, onClick: () -> Unit) {
    val atlas = LocalImages.current[Assets.DLC_ATLAS]
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Row(
        Modifier.height(28.dp).selectable(selected, source, null, role = Role.RadioButton, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).drawBehind { drawCell(atlas, 3 * index + if (pressed || selected) 2 else 0, Assets.DLC_ATLAS_COLS) })
        Label(text, bold = selected, oneLine = true)
    }
}

/** A civ's toggle, its icon and name at [scale] times their full size. */
@Composable
private fun CivToggle(c: Civ, on: Boolean, icon: ImageBitmap, scale: Float, onToggle: () -> Unit, modifier: Modifier) =
    Toggle(c.name, on, onToggle, modifier, 2.dp, 13.sp * scale) { ToggleIcon(icon, on, 18.dp * scale) }

/** A civ or DLC toggle: its icon stands in for the checkbox, and its name is bold while on and struck through while off. */
@Composable
private fun Toggle(
    name: String,
    on: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier,
    gap: Dp,
    size: TextUnit = 13.sp,
    icon: @Composable () -> Unit,
) {
    Row(
        modifier.toggleable(on, null, null, role = Role.Checkbox) { onToggle() },
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Label(name, size = size, bold = on, italic = !on, strike = !on, oneLine = true)
    }
}
