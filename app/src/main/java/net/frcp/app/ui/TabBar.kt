package net.frcp.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.frcp.app.Assets
import net.frcp.app.PickerViewModel

/**
 * A tab's two looks in the phone tabs image: its box there, at [x] when it isn't selected and at [selectedX] when it
 * is. The tabs differ in size, so each look's box is stretched over its tab.
 */
private class TabLook(val label: String, val x: Int, val selectedX: Int, val y: Int, val w: Int, val h: Int)

private val TABS = listOf(
    TabLook("Draw", 363, 24, 5, 333, 178),
    TabLook("Log", 1045, 708, 2, 317, 148),
    TabLook("Pool", 359, 30, 195, 305, 147),
)

/** The tab bar along the bottom edge: the three parchment tabs share the width, and the selected one is bold. */
@Composable
fun TabBar(vm: PickerViewModel) {
    val atlas = LocalImages.current[Assets.ui("mobile_tabs")]
    Row(Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp).selectableGroup()) {
        TABS.forEachIndexed { i, tab ->
            val selected = vm.tab == i
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .drawBehind { drawPart(atlas, if (selected) tab.selectedX else tab.x, tab.y, tab.w, tab.h) }
                    .selectable(selected, null, null, role = Role.Tab) { vm.selectTab(i) }
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center,
            ) { Label(tab.label, size = 16.sp, bold = selected) }
        }
    }
}
