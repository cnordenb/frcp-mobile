package net.frcp.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.frcp.app.Assets
import net.frcp.app.LogRow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** The logs move straight to where the finger takes them, without coasting on, as the web version's do. */
private object NoFling : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float) = initialVelocity
}

/**
 * A sunken brown log panel of rounds of rows, each led by its civ's icon, with a blank line between the rounds. While
 * it overflows, a scrollbar in the game's style lies over its right side.
 *
 * Only the lines on screen are built, so a long log costs no more to show than a short one.
 */
@Composable
fun LogPanel(rounds: List<List<LogRow>>, description: String, modifier: Modifier = Modifier) {
    val images = LocalImages.current
    val atlas = images[Assets.CIV_ATLAS]
    // The rounds' rows one after another, with null for the blank line between two rounds
    val lines = remember(rounds) { rounds.flatMapIndexed { i, rows -> if (i == 0) rows else listOf(null) + rows } }
    val list = rememberLazyListState()
    val hasBar = list.canScrollForward || list.canScrollBackward
    Box(modifier.background(Ink.LogBack).nineSlice(images.nineSlice(Assets.ui("log_9slice"), 6), 6.dp)) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(6.dp)
                .semantics { contentDescription = description },
            state = list,
            contentPadding = PaddingValues(start = 8.dp, top = 6.dp, bottom = 6.dp, end = if (hasBar) 29.dp else 2.dp),
            flingBehavior = NoFling,
        ) {
            items(lines.size, contentType = { if (lines[it] == null) 0 else 1 }) { i ->
                val row = lines[i]
                if (row == null) {
                    Spacer(Modifier.height(ROW_HEIGHT))
                } else {
                    Row(Modifier.height(ROW_HEIGHT), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(18.dp).drawBehind { drawCell(atlas, row.civ, Assets.CIV_ATLAS_COLS) })
                        Label(row.text, color = Ink.LogText, oneLine = true)
                    }
                }
            }
        }
        if (hasBar) LogScrollBar(list, Modifier.align(Alignment.TopEnd).padding(6.dp).fillMaxHeight().width(27.dp))
    }
}

private val ROW_HEIGHT = 20.dp

/**
 * The game's scrollbar: an up button, a track and a down button, with a knob that runs the whole track. The buttons
 * scroll a line at a time while held, a press on the track pages towards it until the knob gets there, and the knob
 * drags the log along in proportion. The scrollbar image has the up button, the track and the down button from top to
 * bottom, 27 px square each, and the track stretches its middle.
 */
@Composable
private fun LogScrollBar(list: LazyListState, modifier: Modifier) {
    val images = LocalImages.current
    val bar = images[Assets.ui("scrollbar_3slice")]
    val knob = images[Assets.ui("scrollbar_knob")]
    val density = LocalDensity.current
    val line = with(density) { ROW_HEIGHT.roundToPx().toFloat() }
    val knobSize = with(density) { 21.dp.toPx() }
    var trackHeight by remember { mutableFloatStateOf(0f) }
    // Every line of a log is a row high, so how far the log has scrolled, and how far it can, follow from its lines.
    fun scrolled() = list.firstVisibleItemIndex * line + list.firstVisibleItemScrollOffset
    fun range() = list.layoutInfo.let { it.totalItemsCount * line + it.beforeContentPadding + it.afterContentPadding - it.viewportSize.height }
    fun knobTop(): Float {
        val p = if (!list.canScrollForward) 1f else if (range() <= 0f) 0f else min(1f, scrolled() / range())
        return p * (trackHeight - knobSize)
    }
    fun page() = max(line, list.layoutInfo.viewportSize.height - line)

    Box(modifier) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(27.dp)
                .drawBehind { drawPart(bar, 0, 0, 27, 27) }
                .pointerInput(Unit) { pressAndRepeat { list.dispatchRawDelta(-line) != 0f } },
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(vertical = 27.dp)
                .onSizeChanged { trackHeight = it.height.toFloat() }
                .drawBehind { drawPart(bar, 0, 27, 27, 27) }
                .pointerInput(Unit) {
                    coroutineScope {
                        val scope = this
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            down.consume()
                            val top = knobTop()
                            if (down.position.y in top..top + knobSize) {
                                // Dragging the knob scrolls in proportion, the track's free length standing for the hidden part of the log.
                                val y0 = down.position.y
                                val scrolled0 = scrolled()
                                val ratio = range() / max(1f, trackHeight - knobSize)
                                drag(down.id) {
                                    it.consume()
                                    list.dispatchRawDelta(scrolled0 + (it.position.y - y0) * ratio - scrolled())
                                }
                            } else {
                                val dir = if (down.position.y < top) -1 else 1
                                repeatWhileHeld(scope, down) { y ->
                                    val t = knobTop()
                                    val reached = if (dir < 0) y >= t else y < t + knobSize
                                    !reached && list.dispatchRawDelta(dir * page()) != 0f
                                }
                            }
                        }
                    }
                },
        ) {
            Box(
                Modifier
                    .offset { IntOffset(3.dp.roundToPx(), knobTop().roundToInt()) }
                    .size(21.dp)
                    .drawBehind { drawPart(knob, 0, 0, knob.width, knob.height) },
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .size(27.dp)
                .drawBehind { drawPart(bar, 0, 54, 27, 27) }
                .pointerInput(Unit) { pressAndRepeat { list.dispatchRawDelta(line) != 0f } },
        )
    }
}

/** A press that calls step at once and then repeatedly while it lasts. */
private suspend fun PointerInputScope.pressAndRepeat(step: () -> Boolean) = coroutineScope {
    val scope = this
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()
        repeatWhileHeld(scope, down) { step() }
    }
}

/**
 * Calls step(the finger's y) at once, again after 350 ms and then every 50 ms while the press lasts, as a scrollbar's
 * buttons and track do. Step returns false to stop.
 */
private suspend fun AwaitPointerEventScope.repeatWhileHeld(scope: CoroutineScope, down: PointerInputChange, step: (Float) -> Boolean) {
    var y = down.position.y
    val job = scope.launch {
        if (!step(y)) return@launch
        delay(350)
        while (step(y)) delay(50)
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
