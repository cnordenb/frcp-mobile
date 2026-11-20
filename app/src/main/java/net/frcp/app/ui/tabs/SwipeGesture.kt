package net.frcp.app.ui.tabs

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.frcp.app.picker.PickerViewModel
import net.frcp.app.picker.SwipeKind
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val SWIPE_MIN = 40.dp
private val SWIPE_SLOP = 10.dp
private const val TRAIL_MILLIS = 100

private val SwipeKind.edge
    get() = when (this) {
        SwipeKind.Draw, SwipeKind.Undo -> -1f to 0f
        SwipeKind.Reset -> 0f to -1f
        SwipeKind.Reroll -> 0f to 1f
    }

internal fun swipeKind(horizontal: Boolean, d: Float) = when {
    horizontal -> if (d < 0) SwipeKind.Draw else SwipeKind.Undo
    else -> if (d < 0) SwipeKind.Reroll else SwipeKind.Reset
}

internal fun swipeAxis(dx: Float, dy: Float, slop: Float): Boolean? =
    if (max(abs(dx), abs(dy)) < slop) null else abs(dx) > abs(dy)

internal data class IconPlace(val distance: Float, val alpha: Float)

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

internal fun glideMillis(distance: Float, speed: Float) = (2 * distance / max(speed, 0.1f)).coerceIn(80f, 350f)

internal class SwipeLook {
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

internal class SwipeTracker(
    private val vm: PickerViewModel,
    private val look: SwipeLook,
    private val scope: CoroutineScope,
    private val swipeAllowed: () -> Boolean,
) {
    var panel = Rect.Zero
    var icon = Rect.Zero

    private fun travel(kind: SwipeKind) = max(
        1f,
        when (kind) {
            SwipeKind.Reset -> icon.top - panel.top
            SwipeKind.Reroll -> panel.bottom - icon.bottom
            else -> icon.left - panel.left
        },
    )

    private fun paint(s: Swipe) {
        val kind = s.kind ?: return
        if (!s.moves) return
        val (icon, ghost) = swipePlaces(kind, s.t, s.travel, s.acted)
        look.place(true, kind.edge, icon.distance, icon.alpha)
        if (ghost != null) look.place(false, kind.edge, ghost.distance, ghost.alpha)
        look.ghostShown = ghost != null
    }

    private fun aim(s: Swipe, kind: SwipeKind) {
        if (kind == s.kind) return
        look.rest()
        val cell = vm.swipeCell(kind)
        s.kind = kind
        s.moves = cell != null && vm.settings.showIcon
        s.travel = travel(kind)
        if (cell != null && s.moves) look.ghostCell = cell
    }

    private fun follow(s: Swipe, change: PointerInputChange) {
        s.d = if (s.horizontal == true) change.position.x - s.x else change.position.y - s.y
        s.trail.addLast(change.uptimeMillis to s.d)
        while (s.trail.size > 2 && change.uptimeMillis - s.trail.first().first > TRAIL_MILLIS) s.trail.removeFirst()
        aim(s, swipeKind(s.horizontal == true, s.d))
        s.t = min(abs(s.d), s.travel)
        paint(s)
    }

    private fun glideTo(s: Swipe, to: Float) {
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

    private fun lift(s: Swipe, change: PointerInputChange, minimum: Float) {
        if (s.horizontal == null) return
        change.consume()
        follow(s, change)
        if (abs(s.d) < minimum) {
            glideTo(s, 0f)
            return
        }
        val before = vm.iconCell(vm.view.current?.id)
        vm.swipe(s.kind ?: return)
        if (!s.moves) return
        s.acted = true
        look.ghostCell = before
        glideTo(s, s.travel)
    }

    suspend fun track(input: PointerInputScope) = with(input) {
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
                        lift(s, change, minimum)
                        break
                    }
                    if (s.horizontal == null) {
                        val dx = change.position.x - s.x
                        s.horizontal = swipeAxis(dx, change.position.y - s.y, slop) ?: continue
                    }
                    change.consume()
                    follow(s, change)
                }
            } catch (e: CancellationException) {
                if (s.horizontal != null) glideTo(s, 0f)
                throw e
            }
        }
    }
}
