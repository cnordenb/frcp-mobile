package net.frcp.app.ui.menu

import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog as WindowDialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import net.frcp.app.BuildConfig
import net.frcp.app.device.Assets
import net.frcp.app.logic.Civ
import net.frcp.app.logic.Edition
import net.frcp.app.logic.Picker
import net.frcp.app.logic.SharedLink
import net.frcp.app.picker.Dialog
import net.frcp.app.picker.PickerViewModel
import net.frcp.app.ui.Ink
import net.frcp.app.ui.Label
import net.frcp.app.ui.LocalImages
import net.frcp.app.ui.NineSlice
import net.frcp.app.ui.Paragraph
import net.frcp.app.ui.Tile
import net.frcp.app.ui.widgets.ParchmentButton
import net.frcp.app.ui.widgets.ParchmentTextField
import kotlin.math.roundToInt

@Composable
fun Dialogs(vm: PickerViewModel) {
    when (val d = vm.dialog) {
        null -> Unit
        Dialog.Options -> OptionsDialog(vm)
        Dialog.About -> AboutDialog(vm)
        Dialog.OpenLink -> OpenLinkDialog(vm)
        is Dialog.Preview -> PreviewDialog(vm, d.link)
        is Dialog.Message -> MessageDialog(vm, d.title, d.text)
    }
}

@Composable
fun ParchmentDialog(
    title: String,
    onDismiss: () -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val wholeScreen = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    WindowDialog(onDismissRequest = onDismiss, properties = wholeScreen) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        LaunchedEffect(window) {
            window ?: return@LaunchedEffect
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            withFrameNanos { }
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
        val dismiss by rememberUpdatedState(onDismiss)
        val panel = LocalImages.current.nineSlice(Assets.ui("panel_9slice"), 64)
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
                .pointerInput(Unit) { detectTapGestures { dismiss() } }
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Panel(panel, title, buttons, content)
        }
    }
}

@Composable
private fun Panel(
    panel: NineSlice,
    title: String,
    buttons: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .padding(16.dp)
            .widthIn(max = 480.dp)
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures { } }
            .drawBehind {
                val slice = 38.dp.toPx().roundToInt().toFloat()
                drawIntoCanvas {
                    panel.drawInlay(it.nativeCanvas, size.width, size.height, slice / 2)
                    panel.draw(it.nativeCanvas, size.width, size.height, slice, Tile.Round, fill = false)
                }
            }
            .padding(13.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(32.dp).padding(start = 10.dp), contentAlignment = Alignment.CenterStart) {
            Label(title, size = 16.sp, bold = true, oneLine = true)
        }
        Column(
            Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            content = buttons,
        )
    }
}

@Composable
fun DialogButton(text: String, onClick: () -> Unit) = ParchmentButton(text, onClick, size = DpSize(96.dp, 34.dp))

@Composable
private fun Text(text: String, color: Color = Ink.Text, size: Int = 14) = Label(text, size = size.sp, color = color)

private fun AnnotatedString.Builder.link(text: String, url: String) {
    val style = TextLinkStyles(SpanStyle(color = Ink.Link, textDecoration = TextDecoration.Underline))
    withLink(LinkAnnotation.Url(url, style)) { append(text) }
}

@Composable
private fun AboutDialog(vm: PickerViewModel) {
    ParchmentDialog("About Fresh Random Civ Picker", vm::closeDialog, { DialogButton("OK", vm::closeDialog) }) {
        Paragraph(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("Fresh Random Civ Picker") }
                append(", Android version v${BuildConfig.VERSION_NAME}")
            },
        )
        Paragraph(
            buildAnnotatedString {
                append("Created by Carl Nordenbrink, alias ")
                link("Hjörleif", "https://www.youtube.com/hjoerleif")
                append(". Also available on the web at ")
                link("frcp.net", PickerViewModel.SITE)
                append(" and as a ")
                link("desktop app", "https://github.com/cnordenb/Fresh-Random-Civ-Picker_GUI/releases")
                append(".")
            },
        )
        Text(
            "Custom civ pool, draw log and user settings are saved on this device only. " +
                "Share links carry a copy of the custom civ pool, and optionally of this round's draws.",
        )
        Paragraph(
            buildAnnotatedString {
                append("Age of Empires II © Microsoft Corporation. ")
                append("Fresh Random Civ Picker was created under Microsoft's \"")
                link("Game Content Usage Rules", "https://www.xbox.com/en-US/developers/rules")
                append("\" using assets from Age of Empires II and Age of Empires II: Definitive Edition, ")
                append("and it is not endorsed by or affiliated with Microsoft.")
            },
            size = 12.sp, color = Ink.Brown,
        )
    }
}

@Composable
private fun MessageDialog(vm: PickerViewModel, title: String, text: String) {
    ParchmentDialog(title, vm::closeDialog, { DialogButton("OK", vm::closeDialog) }) { Text(text) }
}

@Composable
private fun OpenLinkDialog(vm: PickerViewModel) {
    var text by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    fun open() {
        if (!vm.openLinkText(text)) wrong = true
    }
    ParchmentDialog("Open Link", vm::closeDialog, {
        DialogButton("Open", ::open)
        DialogButton("Cancel", vm::closeDialog)
    }) {
        Text("Paste a Fresh Random Civ Picker link:")
        ParchmentTextField(text, {
            text = it
            wrong = false
        }, ::open, Modifier.focusRequester(focus))
        if (wrong) Text("That isn't a Fresh Random Civ Picker link.", Ink.Error)
        LaunchedEffect(Unit) { focus.requestFocus() }
    }
}

@Composable
private fun PreviewDialog(vm: PickerViewModel, link: SharedLink) {
    val logic = vm.logic
    val enabled = link.enabled.count { it }
    val out = logic.displayOrder.filter { logic.inEdition(it, link.edition) && !link.enabled[it.id] }.map { it.name }
    val leftOut = if (out.isNotEmpty() && out.size <= 12) ". Left out: ${out.joinToString(", ")}" else ""
    val pool = "$enabled of ${logic.editionTotal(link.edition)} civs" + leftOut
    val round = if (link.draws.isEmpty()) "Nothing drawn. Loading the link starts a new round."
    else "${link.draws.size} drawn: ${link.draws.joinToString(", ") { logic.civs[it].name }}"
    ParchmentDialog("Open Shared Link", vm::closeDialog, {
        DialogButton("Load Link") { vm.loadPreviewedLink(link) }
        DialogButton("Cancel", vm::closeDialog)
    }) {
        Text("This link holds:")
        val edition = logic.editionInfo.getValue(link.edition).name
        for ((term, value) in listOf("Edition" to edition, "Civ pool" to pool, "This round" to round)) {
            Row {
                Label(term, Modifier.width(84.dp), size = 14.sp, color = Ink.Brown)
                Label(value, Modifier.weight(1f), size = 14.sp)
            }
        }
        Text("Loading it replaces the current civ pool and current round. The log of past rounds is still kept.")
    }
}
