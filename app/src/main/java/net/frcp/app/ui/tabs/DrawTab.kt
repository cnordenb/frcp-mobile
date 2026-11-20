package net.frcp.app.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.frcp.app.device.Assets
import net.frcp.app.picker.PickerViewModel
import net.frcp.app.ui.Ink
import net.frcp.app.ui.Label
import net.frcp.app.ui.LocalImages
import net.frcp.app.ui.widgets.image

private val SWIPE_HINTS =
    listOf("Swipe left to draw", "Swipe right to undraw", "Swipe up to redraw", "Swipe down to reset")

@Composable
fun DrawTab(vm: PickerViewModel, pageTop: Dp, swipeAllowed: () -> Boolean) {
    val view = vm.view
    val settings = vm.settings
    val look = remember { SwipeLook() }
    val scope = rememberCoroutineScope()
    val tracker = remember { SwipeTracker(vm, look, scope, swipeAllowed) }
    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { tracker.panel = it.boundsInRoot() }
            .swipeActions(vm)
            .pointerInput(Unit) { tracker.track(this) },
    ) {
        Column(
            Modifier.fillMaxSize().padding(top = 10.dp + pageTop, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
                val counter = "${view.drawn}/${view.pool.size}"
                if (settings.showCounter) Label(counter, Modifier.offset(5.dp, 5.dp), bold = true)
            }
            Spacer(Modifier.weight(1f))
            if (settings.showIcon) {
                SwipeIcons(vm, look) { tracker.icon = it }
                Spacer(Modifier.height(6.dp))
            }
            val name = view.flash ?: view.current?.name ?: "?"
            if (settings.showName) Label(name, size = 20.sp, bold = true, align = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            for (hint in SWIPE_HINTS) Label(hint, color = Ink.Brown, align = TextAlign.Center, lineHeight = 22.sp)
        }
    }
}

private fun Modifier.swipeActions(vm: PickerViewModel) = semantics {
    customActions = listOf(
        CustomAccessibilityAction("Draw") { vm.draw(); true },
        CustomAccessibilityAction("Undraw") { vm.undo(); true },
        CustomAccessibilityAction("Redraw") { vm.reroll(); true },
        CustomAccessibilityAction("Reset") { vm.reset(); true },
    )
}

@Composable
private fun SwipeIcons(vm: PickerViewModel, look: SwipeLook, onPlaced: (Rect) -> Unit) {
    val current = vm.view.current
    Box(Modifier.size(144.dp).onGloballyPositioned { onPlaced(it.boundsInRoot()) }) {
        CivIcon(
            vm, vm.iconCell(current?.id),
            Modifier
                .graphicsLayer {
                    translationX = look.iconX
                    translationY = look.iconY
                    alpha = look.iconAlpha
                }
                .clickable(null, null, role = Role.Button, onClick = vm::playJingle)
                .semantics { contentDescription = "${current?.name ?: "Random"}: play the jingle" },
        )
        if (look.ghostShown) {
            CivIcon(
                vm, look.ghostCell,
                Modifier.graphicsLayer {
                    translationX = look.ghostX
                    translationY = look.ghostY
                    alpha = look.ghostAlpha
                },
            )
        }
    }
}

@Composable
private fun CivIcon(vm: PickerViewModel, cell: Int, modifier: Modifier) {
    val path = if (cell == vm.randomCell) Assets.RANDOM_ICON else Assets.civIcon(vm.logic.civs[cell].file)
    Box(modifier.size(144.dp).image(LocalImages.current[path]))
}
