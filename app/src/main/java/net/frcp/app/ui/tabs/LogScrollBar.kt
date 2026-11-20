package net.frcp.app.ui.tabs

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.frcp.app.device.Assets
import net.frcp.app.ui.LocalImages
import net.frcp.app.ui.drawPart
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val BAR_CELL = 27
private const val FIRST_REPEAT_MILLIS = 350L
private const val REPEAT_MILLIS = 50L

internal fun scrollKnobTop(scrolled: Float, range: Float, atEnd: Boolean, free: Float): Float {
    val p = if (atEnd) 1f else if (range <= 0f) 0f else min(1f, scrolled / range)
    return p * free
}

internal fun scrollForKnobDrag(scrolled0: Float, dy: Float, range: Float, free: Float) =
    scrolled0 + dy * range / max(1f, free)

internal fun scrollPage(viewport: Float, line: Float) = max(line, viewport - line)

private class ScrollBarState(val list: LazyListState, val line: Float, val knobSize: Float) {
    var trackHeight by mutableFloatStateOf(0f)

    val free get() = trackHeight - knobSize

    fun scrolled() = list.firstVisibleItemIndex * line + list.firstVisibleItemScrollOffset

    fun range(): Float {
        val info = list.layoutInfo
        val content = info.totalItemsCount * line + info.beforeContentPadding + info.afterContentPadding
        return content - info.viewportSize.height
    }

    fun knobTop() = scrollKnobTop(scrolled(), range(), !list.canScrollForward, free)

    fun page() = scrollPage(list.layoutInfo.viewportSize.height.toFloat(), line)
}

@Composable
internal fun LogScrollBar(list: LazyListState, modifier: Modifier) {
    val images = LocalImages.current
    val strip = images[Assets.ui("scrollbar_3slice")]
    val knob = images[Assets.ui("scrollbar_knob")]
    val density = LocalDensity.current
    val line = with(density) { ROW_HEIGHT.roundToPx().toFloat() }
    val knobSize = with(density) { 21.dp.toPx() }
    val bar = remember(list, line, knobSize) { ScrollBarState(list, line, knobSize) }
    Box(modifier) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(BAR_CELL.dp)
                .drawBehind { drawPart(strip, 0, 0, BAR_CELL, BAR_CELL) }
                .pointerInput(Unit) { pressAndRepeat { list.dispatchRawDelta(-line) != 0f } },
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(vertical = BAR_CELL.dp)
                .onSizeChanged { bar.trackHeight = it.height.toFloat() }
                .drawBehind { drawPart(strip, 0, BAR_CELL, BAR_CELL, BAR_CELL) }
                .pointerInput(Unit) { trackPresses(bar) },
        ) {
            Box(
                Modifier
                    .offset { IntOffset(3.dp.roundToPx(), bar.knobTop().roundToInt()) }
                    .size(21.dp)
                    .drawBehind { drawPart(knob, 0, 0, knob.width, knob.height) },
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .size(BAR_CELL.dp)
                .drawBehind { drawPart(strip, 0, 2 * BAR_CELL, BAR_CELL, BAR_CELL) }
                .pointerInput(Unit) { pressAndRepeat { list.dispatchRawDelta(line) != 0f } },
        )
    }
}

private suspend fun PointerInputScope.trackPresses(bar: ScrollBarState) = coroutineScope {
    val scope = this
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        val top = bar.knobTop()
        if (down.position.y in top..top + bar.knobSize) {
            dragKnob(bar, down)
        } else {
            pageTowards(scope, bar, down, if (down.position.y < top) -1 else 1)
        }
    }
}

private suspend fun AwaitPointerEventScope.dragKnob(bar: ScrollBarState, down: PointerInputChange) {
    val y0 = down.position.y
    val scrolled0 = bar.scrolled()
    val range0 = bar.range()
    val free = bar.free
    drag(down.id) {
        it.consume()
        val target = scrollForKnobDrag(scrolled0, it.position.y - y0, range0, free)
        bar.list.dispatchRawDelta(target - bar.scrolled())
    }
}

private suspend fun AwaitPointerEventScope.pageTowards(
    scope: CoroutineScope,
    bar: ScrollBarState,
    down: PointerInputChange,
    dir: Int,
) {
    repeatWhileHeld(scope, down) { y ->
        val t = bar.knobTop()
        val reached = if (dir < 0) y >= t else y < t + bar.knobSize
        !reached && bar.list.dispatchRawDelta(dir * bar.page()) != 0f
    }
}

private suspend fun PointerInputScope.pressAndRepeat(step: () -> Boolean) = coroutineScope {
    val scope = this
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        repeatWhileHeld(scope, down) { step() }
    }
}

private suspend fun AwaitPointerEventScope.repeatWhileHeld(
    scope: CoroutineScope,
    down: PointerInputChange,
    step: (Float) -> Boolean,
) {
    var y = down.position.y
    val job = scope.launch {
        if (!step(y)) return@launch
        delay(FIRST_REPEAT_MILLIS)
        while (step(y)) delay(REPEAT_MILLIS)
    }
    try {
        while (true) {
            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
            y = change.position.y
            change.consume()
            if (!change.pressed) break
        }
    } finally {
        job.cancel()
    }
}
