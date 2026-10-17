package net.frcp.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.frcp.app.PickerViewModel
import net.frcp.app.Settings

/**
 * The Options dialog: display, sound, continuous freshness and what happens when the app opens. The web version's
 * tooltip and hotkey settings have no use on a phone.
 */
@Composable
fun OptionsDialog(vm: PickerViewModel) {
    val s = vm.settings
    val n = vm.view.pool.size
    ParchmentDialog("Options", vm::closeDialog, { DialogButton("OK", vm::closeDialog) }) {
        Fieldset("Display") {
            Option(vm, "Show civ name", s.showName) { copy(showName = !showName) }
            Option(vm, "Show civ icon", s.showIcon) { copy(showIcon = !showIcon) }
            Option(vm, "Show round counter", s.showCounter) { copy(showCounter = !showCounter) }
        }
        Fieldset("Sound") {
            OptionRow("Enable civ jingles", s.jingles) { vm.setJingles(!s.jingles) }
            Row(Modifier.heightIn(min = 40.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Label("Jingle type", size = 14.sp)
                ParchmentDropdown(listOf("Definitive Edition", "Legacy"), if (s.legacyJingles) 1 else 0, s.jingles, { vm.setLegacyJingles(it == 1) })
            }
            OptionRow("Enable UI sounds", s.uiSounds) { vm.setUiSounds(!s.uiSounds) }
        }
        Fieldset("Continuous freshness") {
            Option(vm, "Enable continuous freshness", s.fresh) { copy(fresh = !fresh) }
            // The slider runs from 1 to half the pool. At its right end it means "the most", which follows the pool.
            val max = vm.logic.freshMax(n)
            val value = vm.logic.freshLimit(n, s.freshN)
            Label(
                if (n < 2) "Needs at least 2 civs in the pool"
                else "Exclude the last $value civ${if (value == 1) "" else "s"}${if (value == max) " (the most)" else ""}",
                size = 14.sp,
            )
            ParchmentSlider(value, 1, max, s.fresh && n >= 2, { vm.setFreshN(it, max) }, vm::freshNSettled)
            Label("When a round ends, these civs sit out the first draws of the next round.", color = Ink.Brown)
        }
        Fieldset("When the app opens") {
            Option(vm, "Draw a civ", s.drawOnLoad) { copy(drawOnLoad = !drawOnLoad) }
            Option(vm, "Start a new round", s.newRoundOnVisit) { copy(newRoundOnVisit = !newRoundOnVisit) }
            Label("Civ pool and draw log are always kept.", color = Ink.Brown)
        }
    }
}

@Composable
private fun OptionRow(text: String, checked: Boolean, onToggle: () -> Unit) =
    CheckRow(text, checked, onToggle, Modifier.fillMaxWidth().heightIn(min = 36.dp), size = 14.sp, gap = 8.dp)

/** A checkbox for a setting that only needs saving. */
@Composable
private fun Option(vm: PickerViewModel, text: String, checked: Boolean, change: Settings.() -> Settings) {
    OptionRow(text, checked) { vm.toggleSetting(change) }
}

/** A group of settings in a frame, with its legend set into the frame's top edge. */
@Composable
private fun Fieldset(legend: String, content: @Composable ColumnScope.() -> Unit) {
    var legendBox by remember { mutableStateOf(Rect.Zero) }
    Box(
        Modifier.fillMaxWidth().drawBehind {
            val y = legendBox.center.y
            val w = 1.dp.toPx()
            val gap = 3.dp.toPx()
            val right = size.width - w / 2
            val bottom = size.height - w / 2
            val left = w / 2
            drawLine(Ink.Group, Offset(left, y), Offset(legendBox.left - gap, y), w)
            drawLine(Ink.Group, Offset(legendBox.right + gap, y), Offset(right, y), w)
            drawLine(Ink.Group, Offset(left, y), Offset(left, bottom), w)
            drawLine(Ink.Group, Offset(right, y), Offset(right, bottom), w)
            drawLine(Ink.Group, Offset(left, bottom), Offset(right, bottom), w)
        },
    ) {
        Column(Modifier.padding(start = 10.dp, end = 10.dp, top = 22.dp, bottom = 10.dp), content = content)
        Label(legend, Modifier.padding(start = 10.dp).onGloballyPositioned { legendBox = it.boundsInParent() }, size = 14.sp, bold = true)
    }
}
