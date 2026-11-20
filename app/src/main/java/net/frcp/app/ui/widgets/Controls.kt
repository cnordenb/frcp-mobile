package net.frcp.app.ui.widgets

import net.frcp.app.ui.nineSlice
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
import net.frcp.app.device.Assets
import net.frcp.app.ui.Grayscale
import net.frcp.app.ui.Ink
import net.frcp.app.ui.Label
import net.frcp.app.ui.LocalImages
import net.frcp.app.ui.drawCell
import net.frcp.app.ui.drawPart
import kotlin.math.roundToInt

fun Modifier.image(image: ImageBitmap) = drawBehind { drawPart(image, 0, 0, image.width, image.height) }

@Composable
fun ParchmentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(100.dp, 30.dp),
) {
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
        val color = if (pressed) Ink.ButtonPressed else Ink.ButtonText
        Label(text, Modifier.offset(shift, -shift), size = 14.sp, color = color, oneLine = true)
    }
}

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
                drawPart(cross, 0, 0, cross.width, cross.height, IntOffset(at, at), IntSize(side, side))
            }
        },
    )
}
