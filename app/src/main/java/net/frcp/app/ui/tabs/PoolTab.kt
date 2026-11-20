package net.frcp.app.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import net.frcp.app.device.Assets
import net.frcp.app.logic.Civ
import net.frcp.app.logic.Edition
import net.frcp.app.logic.Logic
import net.frcp.app.logic.PickerState
import net.frcp.app.picker.PickerViewModel
import net.frcp.app.ui.Label
import net.frcp.app.ui.LocalImages
import net.frcp.app.ui.drawCell
import net.frcp.app.ui.drawPart
import net.frcp.app.ui.widgets.CheckRow
import net.frcp.app.ui.widgets.ParchmentButton
import net.frcp.app.ui.widgets.ShareButton
import net.frcp.app.ui.widgets.ToggleIcon

internal val HIDDEN_DLC_TOGGLES = setOf("west", "dukes", "india")

private val CIV_ROW = 24.dp

internal fun shownDlcToggles(edition: Edition) = edition.dlcToggles.filter { it !in HIDDEN_DLC_TOGGLES }

internal fun civGrid(logic: Logic, ed: String): List<List<Civ?>> =
    logic.displayOrder.map { if (logic.inEdition(it, ed)) it else null }.chunked(3)

@Composable
fun PoolTab(vm: PickerViewModel, pageTop: Dp) {
    val view = vm.view
    val settings = vm.settings
    val info = vm.logic.editionInfo.getValue(view.state.edition)
    Column(
        Modifier.fillMaxSize().padding(top = 58.dp + pageTop, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        EditionPicker(vm, info)
        Row(verticalAlignment = Alignment.CenterVertically) {
            CheckRow("Auto-toggle older civs", settings.autoToggle, vm::toggleAutoToggle, Modifier.weight(1f))
            Label("Enabled: ${view.pool.size}/${view.editionTotal}", bold = true)
        }
        DlcToggles(vm, info)
        CivRows(vm)
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

private fun PickerState.isOn(id: Int) = id !in off

@Composable
private fun EditionPicker(vm: PickerViewModel, info: Edition) {
    val logic = vm.logic
    val ed = vm.view.state.edition
    Row {
        Column(Modifier.weight(1f).selectableGroup(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            logic.editions.forEachIndexed { i, e ->
                val other = logic.editionInfo.getValue(e)
                EditionRow(i, "${other.short} (${other.year})", e == ed) { vm.setEdition(e) }
            }
        }
        val banners = LocalImages.current[Assets.BANNER_ATLAS]
        val index = logic.editions.indexOf(ed)
        val storePage = "${info.series}: ${info.name} on Steam"
        Box(
            Modifier
                .size(128.dp, 93.dp)
                .drawBehind {
                    val h = banners.height / logic.editions.size
                    drawPart(banners, 0, index * h, banners.width, h)
                }
                .clickable(null, null, role = Role.Button, onClick = vm::openEditionPage)
                .semantics { contentDescription = if (info.url != null) storePage else "About ${info.name}" },
        )
    }
}

@Composable
private fun DlcToggles(vm: PickerViewModel, info: Edition) {
    val logic = vm.logic
    val state = vm.view.state
    val dlcAtlas = LocalImages.current[Assets.DLC_ATLAS]
    val dlcIcons = remember(logic) { logic.dlcs.values.filter { it.hasIcon }.map { it.key } }
    val rowHeight = maxOf(info.toggleSize, 22).dp
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (pair in shownDlcToggles(info).chunked(2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (dlc in pair) {
                    val on = logic.civs.any { it.dlc == dlc && logic.inEdition(it, state.edition) && state.isOn(it.id) }
                    val name = logic.dlcs.getValue(dlc).name
                    val cell = 3 * logic.editions.size + dlcIcons.indexOf(dlc)
                    Toggle(name, on, { vm.toggleDlc(dlc) }, Modifier.weight(1f).height(rowHeight), info.toggleGap.dp) {
                        ToggleIcon(dlcAtlas, on, info.toggleSize.dp, cell, Assets.DLC_ATLAS_COLS)
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColumnScope.CivRows(vm: PickerViewModel) {
    val logic = vm.logic
    val state = vm.view.state
    val images = LocalImages.current
    val civRows = remember(logic, state.edition) { civGrid(logic, state.edition) }
    BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
        val scale = minOf(1f, maxHeight / (CIV_ROW * civRows.size))
        Column {
            for (row in civRows) {
                Row(Modifier.height(CIV_ROW * scale), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in row) {
                        if (c != null) {
                            val icon = images[Assets.civIcon(c.file)]
                            val cell = Modifier.weight(1f).fillMaxHeight()
                            CivToggle(c, state.isOn(c.id), icon, scale, { vm.toggleCiv(c.id) }, cell)
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

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
        val cell = 3 * index + if (pressed || selected) 2 else 0
        Box(Modifier.size(28.dp).drawBehind { drawCell(atlas, cell, Assets.DLC_ATLAS_COLS) })
        Label(text, bold = selected, oneLine = true)
    }
}

@Composable
private fun CivToggle(c: Civ, on: Boolean, icon: ImageBitmap, scale: Float, onToggle: () -> Unit, modifier: Modifier) =
    Toggle(c.name, on, onToggle, modifier, 2.dp, 13.sp * scale) { ToggleIcon(icon, on, 18.dp * scale) }

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
