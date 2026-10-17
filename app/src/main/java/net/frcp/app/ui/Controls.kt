package net.frcp.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.frcp.app.Assets
import kotlin.math.roundToInt

/** Draws the whole of [image] over the whole of this element. */
fun Modifier.image(image: ImageBitmap) = drawBehind { drawPart(image, 0, 0, image.width, image.height) }

/**
 * A wooden text button with a blackletter face, as the web version draws it. Its text turns gold and moves up and
 * right with the face of the pressed image. A phone has no blackletter font, so it's the serif type, as in a browser.
 */
@Composable
fun ParchmentButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: DpSize = DpSize(100.dp, 30.dp)) {
    val images = LocalImages.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier
            .size(size)
            .image(images[Assets.ui(if (pressed) "button_pressed" else "button")])
            .clickable(source, null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val shift = if (pressed) 1.dp else 0.dp
        Label(text, Modifier.offset(shift, -shift), size = 14.sp, color = if (pressed) Ink.ButtonPressed else Ink.ButtonText, oneLine = true)
    }
}

/**
 * A button drawn from the 6 dp wooden nine-slice, pressed while held down or while [down] is set. Its content moves
 * up and right with the pressed face.
 */
@Composable
fun WoodButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier,
    down: Boolean = false,
    content: @Composable BoxScope.(pressed: Boolean) -> Unit,
) {
    val images = LocalImages.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val look = pressed || down
    Box(
        modifier
            .nineSlice(images.nineSlice(Assets.ui(if (look) "button_pressed_9slice" else "button_9slice"), 6), 6.dp)
            .clickable(source, null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { content(look) }
}

/** The share button: the share icon on a wooden button, 37 dp square. */
@Composable
fun ShareButton(description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon = LocalImages.current[Assets.ui("share_icon")]
    WoodButton(description, onClick, modifier.size(37.dp)) { pressed ->
        Box(
            Modifier.size(28.dp).drawBehind {
                translate(if (pressed) 1.dp.toPx() else 0f, if (pressed) -1.dp.toPx() else 0f) {
                    drawPart(icon, 0, 0, icon.width, icon.height)
                }
            },
        )
    }
}

/**
 * A wooden checkbox. The checkbox image has the unticked box above the ticked one, each with its pressed look on the
 * right.
 */
@Composable
fun CheckBox(checked: Boolean, pressed: Boolean, size: Dp = 16.dp) {
    val atlas = LocalImages.current[Assets.ui("checkbox")]
    Box(
        Modifier.size(size).drawBehind {
            val cell = atlas.width / 2
            drawPart(atlas, if (pressed) cell else 0, if (checked) cell else 0, cell, cell)
        },
    )
}

/** A checkbox and its label, which toggle together, pressed while held down. */
@Composable
fun CheckRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: TextUnit = 13.sp,
    gap: Dp = 4.dp,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Row(
        modifier.toggleable(checked, source, null, enabled = enabled, role = Role.Checkbox) { onToggle() },
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckBox(checked, pressed)
        Label(text, size = size, oneLine = true)
    }
}

/**
 * The icon that stands in for a civ's or DLC's checkbox: the whole of [image], or its [cell] in an atlas of [cols]
 * cells per row. While it's off, it turns grey under the off cross, drawn at 90 % of its size.
 */
@Composable
fun ToggleIcon(image: ImageBitmap, on: Boolean, size: Dp, cell: Int? = null, cols: Int = 1) {
    val cross = LocalImages.current[Assets.ui("off")]
    Box(
        Modifier.size(size).drawBehind {
            val filter = if (on) null else Grayscale
            if (cell == null) drawPart(image, 0, 0, image.width, image.height, colorFilter = filter)
            else drawCell(image, cell, cols, colorFilter = filter)
            if (!on) {
                val side = (this.size.width * 0.9f).roundToInt()
                val at = ((this.size.width - side) / 2).roundToInt()
                drawPart(cross, 0, 0, cross.width, cross.height, dstOffset = IntOffset(at, at), dstSize = IntSize(side, side))
            }
        },
    )
}
