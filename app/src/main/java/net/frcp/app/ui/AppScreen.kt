package net.frcp.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.frcp.app.Assets
import net.frcp.app.Effect
import net.frcp.app.PickerViewModel
import net.frcp.app.Toast

/** Whether the press now going on closed the menu, which keeps it from also swiping on the Draw tab. */
private class MenuPress {
    var closedMenu = false
}

/**
 * The whole screen, as frcp.net's phone layout: the parchment fills it, the tabs fill the bottom edge and a hamburger
 * menu at the top right stands in for the menu bar. Status messages show just over the tab bar.
 */
@Composable
fun AppScreen(vm: PickerViewModel) {
    CompositionLocalProvider(LocalImages provides vm.images, LocalOverscrollFactory provides null) {
        val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val press = remember { MenuPress() }
        var menuBounds by remember { mutableStateOf(Rect.Zero) }
        BackHandler(enabled = vm.menuOpen, onBack = vm::closeMenu)
        Box(
            Modifier
                .fillMaxSize()
                .background(Ink.Parchment)
                .image(vm.images[Assets.ui("background_mobile")])
                // A press outside the open menu closes it, and still reaches what it lands on.
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        press.closedMenu = vm.menuOpen && !menuBounds.contains(down.position)
                        if (press.closedMenu) vm.closeMenu()
                    }
                },
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (vm.tab) {
                        0 -> DrawTab(vm, topInset) { !press.closedMenu && !vm.menuOpen }
                        1 -> LogTab(vm, topInset)
                        else -> PoolTab(vm, topInset)
                    }
                }
                TabBar(vm)
            }
            MainMenu(
                vm,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp + topInset, end = 12.dp)
                    .onGloballyPositioned { menuBounds = it.boundsInRoot() },
            )
            ToastView(vm.toast, Modifier.align(Alignment.BottomCenter))
            Dialogs(vm)
        }
        Effects(vm)
    }
}

/**
 * A status message, for 2.2 seconds. It sits its own height lower than 12 dp over the tab bar, so it reaches over the
 * tab bar's top edge.
 */
@Composable
private fun ToastView(toast: Toast?, modifier: Modifier) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(toast) {
        if (toast == null) return@LaunchedEffect
        shown = true
        delay(2200)
        shown = false
    }
    val alpha by animateFloatAsState(if (shown) 1f else 0f, tween(150), label = "toast")
    if (toast == null || alpha == 0f) return
    Box(
        modifier
            .navigationBarsPadding()
            .padding(bottom = 76.dp)
            .layout { measurable, constraints ->
                val p = measurable.measure(constraints)
                layout(p.width, p.height) { p.place(0, p.height) }
            }
            .alpha(alpha)
            .shadow(4.dp, RoundedCornerShape(3.dp))
            .background(Ink.Toast, RoundedCornerShape(3.dp))
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) { Label(toast.text, color = Color.White, oneLine = true) }
}

/** Carries out what the view model asks of the system: links, the share sheet and the file pickers. */
@Composable
private fun Effects(vm: PickerViewModel) {
    val context = LocalContext.current
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain"), vm::writePreset)
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), vm::readPreset)
    LaunchedEffect(vm) {
        vm.effects.collect { e ->
            try {
                when (e) {
                    is Effect.OpenUrl -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(e.url)))
                    is Effect.Share -> context.startActivity(
                        Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, e.text), null),
                    )
                    is Effect.SavePreset -> save.launch(e.fileName)
                    Effect.LoadPreset -> open.launch(arrayOf("text/plain"))
                }
            } catch (_: ActivityNotFoundException) {
                vm.linkFailed()
            }
        }
    }
}
