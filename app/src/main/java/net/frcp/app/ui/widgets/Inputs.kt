package net.frcp.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import net.frcp.app.Assets
import kotlin.math.roundToInt

// The slider image has the off track above the on track, each 340×25 px. The knob travels along the track's groove,
// which runs from 32 to 308 px of the 340, between the arrowheads.
internal const val GROOVE_START = 32f / 340f
internal const val GROOVE_LENGTH = 276f / 340f

/** Where the knob's centre sits for [value], as a share of the slider's width: [min] at the groove's start, [max] at its end. */
internal fun sliderKnobAt(value: Int, min: Int, max: Int): Float {
    val fill = if (max > min) (value - min).toFloat() / (max - min) else 0f
    return GROOVE_START + GROOVE_LENGTH * fill
}

/** The value under a finger [x] px along a slider [width] px wide: the nearest, and [min] or [max] past the groove's ends. */
internal fun sliderValueAt(x: Float, width: Float, min: Int, max: Int): Int {
    val p = ((x - width * GROOVE_START) / (width * GROOVE_LENGTH)).coerceIn(0f, 1f)
    return (min + p * (max - min)).roundToInt()
}

/**
 * The parchment slider: the off track, with the on track over it up to the knob, and the knob image as the thumb.
 * [onChange] follows the finger, and [onSettled] comes when it lets go.
 */
@Composable
fun ParchmentSlider(value: Int, min: Int, max: Int, enabled: Boolean, onChange: (Int) -> Unit, onSettled: () -> Unit) {
    val images = LocalImages.current
    val track = images[Assets.ui("slider")]
    val knob = images[Assets.ui("slider_knob")]
    val change by rememberUpdatedState(onChange)
    val settled by rememberUpdatedState(onSettled)
    val knobAt = sliderKnobAt(value, min, max)
    Box(
        Modifier
            .fillMaxWidth()
            .height(30.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(value.toFloat(), min.toFloat()..max.toFloat(), (max - min - 1).coerceAtLeast(0))
                if (enabled) {
                    setProgress { v ->
                        change(v.roundToInt().coerceIn(min, max))
                        settled()
                        true
                    }
                }
            }
            .pointerInput(enabled, min, max) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    change(sliderValueAt(down.position.x, size.width.toFloat(), min, max))
                    drag(down.id) {
                        it.consume()
                        change(sliderValueAt(it.position.x, size.width.toFloat(), min, max))
                    }
                    settled()
                }
            }
            .drawBehind {
                val w = size.width
                val h = w * 25f / 340f
                val at = IntOffset(0, ((size.height - h) / 2).roundToInt())
                val dst = IntSize(w.roundToInt(), h.roundToInt())
                drawPart(track, 0, 0, 340, 25, at, dst)
                val end = w * knobAt
                clipRect(right = end) { drawPart(track, 0, 25, 340, 25, at, dst) }
                val k = 24.dp.toPx()
                drawPart(
                    knob, 0, 0, knob.width, knob.height,
                    IntOffset((end - k / 2).roundToInt(), ((size.height - k) / 2).roundToInt()), IntSize(k.roundToInt(), k.roundToInt()),
                )
            },
    )
}

/**
 * A dark brown dropdown like the menus, with the wooden dropdown button as its arrow: its normal look, or its pressed
 * look while the list is open. The list's options turn gold while pressed.
 */
@Composable
fun ParchmentDropdown(options: List<String>, selected: Int, enabled: Boolean, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val arrow = LocalImages.current[Assets.ui("dropdown_button")]
    var open by remember { mutableStateOf(false) }
    var height by remember { mutableIntStateOf(0) }
    var width by remember { mutableIntStateOf(0) }
    Box(modifier.onSizeChanged { height = it.height; width = it.width }) {
        Box(
            Modifier
                .width(160.dp)
                .height(30.dp)
                .alpha(if (enabled) 1f else 0.55f)
                .background(Ink.Menu)
                .border(1.dp, Ink.Brown)
                .drawBehind {
                    val w = 16.dp.toPx().roundToInt()
                    val look = arrow.width / 2
                    drawPart(arrow, if (open) look else 0, 0, look, arrow.height, IntOffset(size.width.roundToInt() - w, 0), IntSize(w, size.height.roundToInt()))
                }
                .clickable(enabled = enabled, role = Role.DropdownList) { open = true }
                .padding(start = 6.dp, end = 22.dp),
            contentAlignment = Alignment.CenterStart,
        ) { Label(options[selected], size = 14.sp, color = Ink.Cream, oneLine = true) }
        if (open) {
            Popup(offset = IntOffset(0, height), onDismissRequest = { open = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(with(LocalDensity.current) { width.toDp() }).background(Ink.Menu).border(1.dp, Ink.Brown).padding(2.dp)) {
                    options.forEachIndexed { i, option ->
                        val source = remember { MutableInteractionSource() }
                        val pressed by source.collectIsPressedAsState()
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(if (pressed) Ink.Gold else Color.Transparent)
                                .clickable(source, null) {
                                    open = false
                                    if (i != selected) onSelect(i)
                                }
                                .padding(horizontal = 8.dp, vertical = 9.dp),
                        ) { Label(option, size = 14.sp, color = if (pressed) Ink.Text else Ink.Cream, oneLine = true) }
                    }
                }
            }
        }
    }
}

/** A dark text field drawn from the 15 px nine-slice at about half size, with white text. */
@Composable
fun ParchmentTextField(value: String, onValueChange: (String) -> Unit, onGo: () -> Unit, modifier: Modifier = Modifier) {
    val slices = LocalImages.current.nineSlice(Assets.ui("textfield_9slice"), 15)
    BasicTextField(
        value, onValueChange,
        modifier
            .fillMaxWidth()
            .background(Ink.Field)
            .nineSlice(slices, 7.dp, Tile.Stretch)
            .padding(horizontal = 9.dp, vertical = 9.dp),
        singleLine = true,
        textStyle = textStyle(14.sp, color = Color.White),
        cursorBrush = SolidColor(Color.White),
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onGo() }),
    )
}
