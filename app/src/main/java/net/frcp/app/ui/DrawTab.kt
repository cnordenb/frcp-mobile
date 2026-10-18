package net.frcp.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.frcp.app.Assets
import net.frcp.app.PickerViewModel
import net.frcp.app.SwipeKind
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val SWIPE_MIN = 40.dp
private val SWIPE_SLOP = 10.dp

/** The parchment edge that each swipe's moving icon leaves by or comes from. */
private val SwipeKind.edge
    get() = when (this) {
        SwipeKind.Draw, SwipeKind.Undo -> -1f to 0f
        SwipeKind.Reset -> 0f to -1f
        SwipeKind.Reroll -> 0f to 1f
    }

/** What a swipe [d] px along its axis does: left draws, right undoes, up re-rolls and down resets. */
internal fun swipeKind(horizontal: Boolean, d: Float) =
    if (horizontal) (if (d < 0) SwipeKind.Draw else SwipeKind.Undo) else (if (d < 0) SwipeKind.Reroll else SwipeKind.Reset)

/**
 * The axis a finger [dx], [dy] px from where it went down settles: none while it's within [slop] px, and then
 * horizontal (true) when it has gone further sideways than up or down.
 */
internal fun swipeAxis(dx: Float, dy: Float, slop: Float): Boolean? =
    if (max(abs(dx), abs(dy)) < slop) null else abs(dx) > abs(dy)

/** Where an icon sits on a swipe: how far towards the swipe's edge, and how opaque. */
internal data class IconPlace(val distance: Float, val alpha: Float)

/**
 * Where a swipe of [kind] at progress [t] of [travel] puts the civ icon and the ghost, the ghost null while hidden.
 * Until the swipe [acted], the civ icon shows the current civ and the ghost the civ coming in. Acting puts the new civ
 * on the civ icon, so the two swap places.
 */
internal fun swipePlaces(kind: SwipeKind, t: Float, travel: Float, acted: Boolean): Pair<IconPlace, IconPlace?> {
    val p = t / travel
    if (kind == SwipeKind.Draw) {
        val leaving = IconPlace(t, 1 - p)
        return if (acted) IconPlace(0f, 1f) to leaving else leaving to null
    }
    val fading = IconPlace(0f, 1 - p)
    val coming = IconPlace(travel - t, p)
    return if (acted) coming to fading else fading to coming
}

/**
 * How long a lifted swipe glides [distance] px, starting at the finger's [speed] in px per ms and slowing to a stop:
 * the ease-out starts at twice its average speed, hence the doubled distance. Between 80 and 350 ms.
 */
internal fun glideMillis(distance: Float, speed: Float) = (2 * distance / max(speed, 0.1f)).coerceIn(80f, 350f)

/** The civ icon's and the swipe ghost's offsets and opacities while a swipe moves them, and the ghost's civ. */
private class SwipeLook {
    var iconX by mutableFloatStateOf(0f)
    var iconY by mutableFloatStateOf(0f)
    var iconAlpha by mutableFloatStateOf(1f)
    var ghostX by mutableFloatStateOf(0f)
    var ghostY by mutableFloatStateOf(0f)
    var ghostAlpha by mutableFloatStateOf(1f)
    var ghostShown by mutableStateOf(false)
    var ghostCell by mutableIntStateOf(0)
    var glide: Job? = null

    fun place(icon: Boolean, edge: Pair<Float, Float>, distance: Float, alpha: Float) {
        if (icon) {
            iconX = edge.first * distance
            iconY = edge.second * distance
            iconAlpha = alpha
        } else {
            ghostX = edge.first * distance
            ghostY = edge.second * distance
            ghostAlpha = alpha
        }
    }

    fun rest() {
        glide?.cancel()
        glide = null
        place(true, 0f to 0f, 0f, 1f)
        place(false, 0f to 0f, 0f, 1f)
        ghostShown = false
    }
}

/** One swipe: where it started, the axis its first few pixels settled, and how far along it is. */
private class Swipe(val x: Float, val y: Float) {
    var horizontal: Boolean? = null
    var kind: SwipeKind? = null
    var moves = false
    var travel = 1f
    var t = 0f
    var d = 0f
    var acted = false
    val trail = ArrayDeque<Pair<Long, Float>>()
}

/**
 * The Draw tab: the round counter at the top, the civ in the middle and the swipe hints at the bottom. It has no
 * buttons: a swipe left draws, right undoes, up re-rolls and down resets, as on frcp.net's phone layout. A tap on the
 * civ icon plays its jingle.
 *
 * The first few pixels of a move settle the swipe's axis, and from then on the icons follow the finger along it,
 * pixel for pixel. A swipe left takes the current icon towards the parchment's left edge, fading it out on the way,
 * and the drawn civ shows up when the finger lifts, with its name and jingle. The other swipes bring in the civ they
 * lead to, fading in from the parchment's edge while the current icon fades out in place: right brings the undone civ
 * from the left edge, down the random icon from the top and up the re-rolled civ from the bottom. A finger lifted
 * short of a swipe takes the icons back, and one lifted past it lets them glide the rest of the way, starting at the
 * finger's speed. A swipe with nothing to do, such as undoing with nothing to undo, moves nothing.
 */
@Composable
fun DrawTab(vm: PickerViewModel, pageTop: Dp, swipeAllowed: () -> Boolean) {
    val view = vm.view
    val settings = vm.settings
    val current = view.current
    val look = remember { SwipeLook() }
    val scope = rememberCoroutineScope()
    var panel by remember { mutableStateOf(Rect.Zero) }
    var icon by remember { mutableStateOf(Rect.Zero) }

    // How far a swipe's moving icon travels: from the civ icon's place to the parchment's edge
    fun travel(kind: SwipeKind) = max(
        1f,
        when (kind) {
            SwipeKind.Reset -> icon.top - panel.top
            SwipeKind.Reroll -> panel.bottom - icon.bottom
            else -> icon.left - panel.left
        },
    )

    // Places the icons for the swipe's progress s.t, from 0 to s.travel
    fun paint(s: Swipe) {
        val kind = s.kind ?: return
        if (!s.moves) return
        val (icon, ghost) = swipePlaces(kind, s.t, s.travel, s.acted)
        look.place(true, kind.edge, icon.distance, icon.alpha)
        if (ghost != null) look.place(false, kind.edge, ghost.distance, ghost.alpha)
        look.ghostShown = ghost != null
    }

    // Sets what the swipe does from the way it now points, and the civ the ghost shows coming in
    fun aim(s: Swipe, kind: SwipeKind) {
        if (kind == s.kind) return
        look.rest()
        val cell = vm.swipeCell(kind)
        s.kind = kind
        s.moves = cell != null && vm.settings.showIcon
        s.travel = travel(kind)
        if (cell != null && s.moves) look.ghostCell = cell
    }

    fun follow(s: Swipe, change: PointerInputChange) {
        s.d = if (s.horizontal == true) change.position.x - s.x else change.position.y - s.y
        s.trail.addLast(change.uptimeMillis to s.d)
        while (s.trail.size > 2 && change.uptimeMillis - s.trail.first().first > 100) s.trail.removeFirst()
        aim(s, swipeKind(s.horizontal == true, s.d))
        s.t = min(abs(s.d), s.travel)
        paint(s)
    }

    // Takes the swipe's progress to `to`, starting at the finger's speed over its last 100 ms and slowing to a stop
    fun glideTo(s: Swipe, to: Float) {
        if (!s.moves || s.trail.isEmpty()) return
        val (t0, d0) = s.trail.first()
        val (t1, d1) = s.trail.last()
        val speed = if (t1 > t0) abs(d1 - d0) / (t1 - t0) else 0f
        val from = s.t
        val ms = glideMillis(abs(to - from), speed)
        look.glide = scope.launch {
            val start = withFrameMillis { it }
            while (true) {
                val k = ((withFrameMillis { it } - start) / ms).coerceIn(0f, 1f)
                s.t = from + (to - from) * (1 - (1 - k) * (1 - k))
                paint(s)
                if (k >= 1f) break
            }
            look.glide = null
            look.rest()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { panel = it.boundsInRoot() }
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Draw") { vm.draw(); true },
                    CustomAccessibilityAction("Undraw") { vm.undo(); true },
                    CustomAccessibilityAction("Redraw") { vm.reroll(); true },
                    CustomAccessibilityAction("Reset") { vm.reset(); true },
                )
            }
            .pointerInput(Unit) {
                val slop = SWIPE_SLOP.toPx()
                val minimum = SWIPE_MIN.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    look.rest()
                    if (!swipeAllowed()) return@awaitEachGesture
                    val s = Swipe(down.position.x, down.position.y)
                    try {
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                if (s.horizontal == null) break
                                change.consume()
                                follow(s, change)
                                if (abs(s.d) < minimum) {
                                    glideTo(s, 0f)
                                } else {
                                    val before = vm.iconCell(vm.view.current?.id)
                                    vm.swipe(s.kind ?: break)
                                    if (s.moves) {
                                        s.acted = true
                                        look.ghostCell = before
                                        glideTo(s, s.travel)
                                    }
                                }
                                break
                            }
                            if (s.horizontal == null) {
                                s.horizontal = swipeAxis(change.position.x - s.x, change.position.y - s.y, slop) ?: continue
                            }
                            change.consume()
                            follow(s, change)
                        }
                    } catch (e: CancellationException) {
                        if (s.horizontal != null) glideTo(s, 0f)
                        throw e
                    }
                }
            },
    ) {
        Column(
            Modifier.fillMaxSize().padding(top = 10.dp + pageTop, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
                if (settings.showCounter) Label("${view.drawn}/${view.pool.size}", Modifier.offset(5.dp, 5.dp), bold = true)
            }
            Spacer(Modifier.weight(1f))
            if (settings.showIcon) {
                Box(Modifier.size(144.dp).onGloballyPositioned { icon = it.boundsInRoot() }) {
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
                Spacer(Modifier.height(6.dp))
            }
            if (settings.showName) Label(view.flash ?: current?.name ?: "?", size = 20.sp, bold = true, align = TextAlign.Center)
            Spacer(Modifier.weight(1f))
            for (hint in listOf("Swipe left to draw", "Swipe right to undraw", "Swipe up to redraw", "Swipe down to reset")) {
                Label(hint, color = Ink.Brown, align = TextAlign.Center, lineHeight = 22.sp)
            }
        }
    }
}

/** A civ's own icon, or random's, filling its place. */
@Composable
private fun CivIcon(vm: PickerViewModel, cell: Int, modifier: Modifier) {
    val path = if (cell == vm.randomCell) Assets.RANDOM_ICON else Assets.civIcon(vm.logic.civs[cell].file)
    Box(modifier.size(144.dp).image(LocalImages.current[path]))
}
