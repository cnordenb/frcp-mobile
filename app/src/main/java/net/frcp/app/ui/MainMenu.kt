package net.frcp.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.frcp.app.Assets
import net.frcp.app.PickerViewModel
import kotlin.math.roundToInt

/**
 * The hamburger menu at the top right, which stands in for the desktop layout's menu bar as on frcp.net's phone
 * layout: three cream bars on a wooden button, pressed while the menu is open, over a dark brown list whose items turn
 * gold while pressed. The items that open another site have the external icon in their left margin.
 */
@Composable
fun MainMenu(vm: PickerViewModel, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        WoodButton("Menu", vm::toggleMenu, Modifier.size(44.dp, 40.dp), down = vm.menuOpen) { pressed ->
            Box(
                Modifier.matchParentSize().drawBehind {
                    val shift = if (pressed) 1.dp.toPx() else 0f
                    for (y in listOf(11, 18, 25)) {
                        drawRect(
                            Ink.Cream,
                            Offset((size.width - 24.dp.toPx()) / 2 + shift, y.dp.toPx() - shift),
                            Size(24.dp.toPx(), 3.dp.toPx()),
                        )
                    }
                },
            )
        }
        if (!vm.menuOpen) return@Column
        fun item(action: () -> Unit): () -> Unit = {
            vm.closeMenu()
            action()
        }
        Spacer(Modifier.height(2.dp))
        Column(
            Modifier
                .widthIn(min = 190.dp)
                .width(IntrinsicSize.Max)
                .shadow(4.dp, RectangleShape)
                .background(Ink.Menu)
                .border(1.dp, Ink.Brown)
                .padding(2.dp),
        ) {
            MenuItem("Open Link ...", onClick = item(vm::openLinkDialog))
            MenuItem("Save Preset ...", onClick = item(vm::savePreset))
            MenuItem("Load Preset ...", onClick = item(vm::loadPreset))
            MenuItem("Tech Tree ...", external = true, onClick = item(vm::openTechTree))
            MenuItem("Options ...", onClick = item(vm::openOptions))
            MenuItem("GitHub ...", external = true) { vm.openExternal(PickerViewModel.GITHUB) }
            MenuItem("Donate ...", external = true) { vm.openExternal(PickerViewModel.DONATE) }
            MenuItem("About ...", onClick = item(vm::openAbout))
        }
    }
}

@Composable
private fun MenuItem(text: String, external: Boolean = false, onClick: () -> Unit) {
    val icon = LocalImages.current[Assets.ui("external")]
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        Modifier
            .fillMaxWidth()
            .background(if (pressed) Ink.Gold else Color.Transparent)
            .clickable(source, null, role = Role.Button, onClick = onClick)
            .drawBehind {
                if (!external) return@drawBehind
                // The external icon is drawn in the menu text's colour, so it turns black with the text when pressed.
                val side = 12.dp.toPx().roundToInt()
                drawPart(
                    icon, 0, 0, icon.width, icon.height,
                    IntOffset(9.dp.toPx().roundToInt(), ((size.height - side) / 2).roundToInt()), IntSize(side, side),
                    colorFilter = if (pressed) ColorFilter.tint(Color.Black, BlendMode.SrcIn) else null,
                )
            }
            .padding(start = 32.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
    ) { Label(text, size = 15.sp, color = if (pressed) Ink.Text else Ink.Cream, oneLine = true) }
}
