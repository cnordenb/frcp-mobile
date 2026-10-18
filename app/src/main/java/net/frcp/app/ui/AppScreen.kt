package net.frcp.app.ui

import android.app.Activity
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import net.frcp.app.Effect
import net.frcp.app.PickerViewModel
import net.frcp.app.Toast

/** Whether the press now going on closed the menu, which keeps it from also swiping on the Draw tab. */
private class MenuPress {
    var closedMenu = false
}

/**
 * The whole screen, as frcp.net's phone layout: the parchment page with its tabs along the bottom, and a hamburger
 * menu at the top right that stands in for the menu bar. The page fills the screen, under the status bar and a
 * gesture bar, whose icons are dark on it. A navigation bar of buttons takes taps, so the page stops above it, and
 * its buttons are light on the black under it. The tabs' content keeps within the page's burnt border and clear of the
 * status bar. Status messages show just over the tabs.
 */
@Composable
fun AppScreen(vm: PickerViewModel) {
    CompositionLocalProvider(LocalImages provides vm.images, LocalOverscrollFactory provides null) {
        val press = remember { MenuPress() }
        var menuBounds by remember { mutableStateOf(Rect.Zero) }
        val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val underNavBar = WindowInsets.tappableElement.getBottom(LocalDensity.current) == 0
        val view = LocalView.current
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = underNavBar
        }
        BackHandler(enabled = vm.menuOpen, onBack = vm::closeMenu)
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                // A press outside the open menu closes it, and still reaches what it lands on.
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        press.closedMenu = vm.menuOpen && !menuBounds.contains(down.position)
                        if (press.closedMenu) vm.closeMenu()
                    }
                },
        ) {
            BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.tappableElement.only(WindowInsetsSides.Bottom))) {
                val page = Page(maxWidth, maxHeight)
                val top = max(page.top, statusBar)
                Box(Modifier.fillMaxSize().pageArt(vm.images, vm.tab))
                Tabs(vm, top, Modifier.fillMaxSize().padding(start = page.left, end = page.right, bottom = page.bottom)) {
                    !press.closedMenu && !vm.menuOpen
                }
                page.TabBar(vm)
                MainMenu(
                    vm,
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = top + 10.dp, end = page.right)
                        .onGloballyPositioned { menuBounds = it.boundsInRoot() },
                )
                ToastView(vm.toast, Modifier.align(Alignment.TopCenter).offset(y = page.tabTop - 12.dp))
            }
            Dialogs(vm)
        }
        Effects(vm)
    }
}

/**
 * The three tabs. They all stay built, as the web page's do, so switching tabs only shows another one: the tab on
 * show is placed, and the others are laid out but left unplaced, so they neither draw nor take touches. The two not
 * on show at the start are built once the first frame is up, so they don't hold up the app's opening.
 */
@Composable
private fun Tabs(vm: PickerViewModel, pageTop: Dp, modifier: Modifier, swipeAllowed: () -> Boolean) {
    var built by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // The first wait ends as the first frame starts, the second once it's drawn.
        repeat(2) { withFrameNanos { } }
        built = true
    }
    Box(modifier) {
        for (i in 0..2) {
            key(i) {
                val shown = i == vm.tab
                if (shown || built) {
                    Box(Modifier.fillMaxSize().then(if (shown) Modifier else Unplaced)) {
                        when (i) {
                            0 -> DrawTab(vm, pageTop, swipeAllowed)
                            1 -> LogTab(vm, pageTop)
                            else -> PoolTab(vm, pageTop)
                        }
                    }
                }
            }
        }
    }
}

private val Unplaced = Modifier.layout { measurable, constraints ->
    val p = measurable.measure(constraints)
    layout(p.width, p.height) {}
}

/** A status message, for 2.2 seconds. Its top sits 12 dp over the tabs, so it reaches over their top edge. */
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
