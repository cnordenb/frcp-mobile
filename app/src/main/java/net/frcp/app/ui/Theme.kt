package net.frcp.app.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** The parchment theme's colours, from the web version's style.css. */
object Ink {
    val Text = Color.Black
    val Brown = Color(0xFF3D2C1C)
    val Menu = Color(0xFF65523B)
    val Cream = Color(0xFFF1DEAE)
    val Gold = Color(0xFFDCCB70)
    val LogText = Color(0xFFD9C69D)
    val LogBack = Color(0xFF615035)
    val ButtonText = Color(0xFFD7C6AD)
    val ButtonPressed = Color(0xFFD7CA15)
    val Link = Color(0xFF08306A)
    val Error = Color(0xFF680A0A)
    val Toast = Color(0xFF2B2B2B)
    val Field = Color(0xFF474747)
    val Group = Color(0x593C2814)
    val Parchment = Color(0xFFD9B48F)
}

/** A civ or DLC that's off turns grey. */
val Grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/**
 * The page's type: the web version asks for Georgia, which a phone doesn't have, so a phone's browser shows its serif.
 * The app does the same.
 */
fun textStyle(
    size: TextUnit = 13.sp,
    bold: Boolean = false,
    color: Color = Ink.Text,
    italic: Boolean = false,
    strike: Boolean = false,
    align: TextAlign = TextAlign.Start,
    lineHeight: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = FontFamily.Serif,
    fontSize = size,
    fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    textDecoration = if (strike) TextDecoration.LineThrough else null,
    color = color,
    textAlign = align,
    lineHeight = lineHeight,
)

@Composable
fun Label(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 13.sp,
    bold: Boolean = false,
    color: Color = Ink.Text,
    italic: Boolean = false,
    strike: Boolean = false,
    align: TextAlign = TextAlign.Start,
    lineHeight: TextUnit = TextUnit.Unspecified,
    oneLine: Boolean = false,
) = BasicText(
    text, modifier,
    style = textStyle(size, bold, color, italic, strike, align, lineHeight),
    softWrap = !oneLine,
    maxLines = if (oneLine) 1 else Int.MAX_VALUE,
    overflow = TextOverflow.Clip,
)

/** Text with links in it, which open in the browser when tapped. */
@Composable
fun Paragraph(text: AnnotatedString, modifier: Modifier = Modifier, size: TextUnit = 14.sp, color: Color = Ink.Text) =
    BasicText(text, modifier, style = textStyle(size, color = color))
