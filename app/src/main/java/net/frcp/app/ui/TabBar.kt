package net.frcp.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import net.frcp.app.Assets
import net.frcp.app.PickerViewModel
import kotlin.math.roundToInt

/**
 * Where things sit on the page art: the parchment (background.png, 1024 × 2135 px) with the selected tab's strip
 * (draw_tab.png and so on, 1024 × 280 px) over its bottom, where the two meet seamlessly. The art's crop, the parchment
 * and its tabs without the see-through margin around them, is stretched to [width] and [height], the whole screen
 * but for a navigation bar of buttons. Positions are given in the images' pixels, the tabs' from the strip's top.
 */
class Page(val width: Dp, val height: Dp) {
    fun x(px: Float): Dp = width * ((px - CROP_LEFT) / (CROP_RIGHT - CROP_LEFT))
    fun y(px: Float): Dp = height * ((px - CROP_TOP) / (CROP_BOTTOM - CROP_TOP))

    /** The room the page's burnt border takes at each side, which the content keeps clear of. */
    val left = x(INNER_LEFT)
    val right = width - x(INNER_RIGHT)
    val top = y(INNER_TOP)
    val bottom = height - y(PAGE_BOTTOM)

    /** The top of the tabs, under the page. */
    val tabTop = y(TAB_TOP)

    companion object {
        const val ART_WIDTH = 1024f
        const val ART_HEIGHT = 2135f
        const val STRIP_HEIGHT = 280f
        const val STRIP_TOP = ART_HEIGHT - STRIP_HEIGHT

        /** The crop: the art's parchment and tabs, out to their outermost pixels that show (half opaque or more). */
        internal const val CROP_LEFT = 37f
        internal const val CROP_TOP = 23f
        internal const val CROP_RIGHT = 987f
        internal const val CROP_BOTTOM = 2095f

        internal const val INNER_LEFT = 80f
        internal const val INNER_RIGHT = 944f
        internal const val INNER_TOP = 60f
        internal const val PAGE_BOTTOM = STRIP_TOP + 80f
        internal const val TAB_TOP = STRIP_TOP + 140f
        internal const val TAB_BOTTOM = STRIP_TOP + 233f

        /** Each tab's left and right edges: Draw, Log and Pool */
        internal val TABS = listOf(59f to 351f, 374f to 650f, 676f to 965f)
        private val LABELS = listOf("Draw", "Log", "Pool")
        private val STRIPS = listOf("draw_tab", "log_tab", "pool_tab")

        fun strip(tab: Int) = Assets.page(STRIPS[tab])
    }

    /** The tabs along the page's bottom, each taking touches from the page's border down. The selected one is bold. */
    @Composable
    fun TabBar(vm: PickerViewModel) {
        Box(Modifier.fillMaxSize().selectableGroup()) {
            TABS.forEachIndexed { i, (l, r) ->
                Box(
                    Modifier
                        .offset(x(l), y(PAGE_BOTTOM))
                        .size(x(r) - x(l), height - y(PAGE_BOTTOM))
                        .selectable(vm.tab == i, null, null, role = Role.Tab) { vm.selectTab(i) },
                ) {
                    Box(
                        Modifier.fillMaxWidth().padding(top = y(TAB_TOP) - y(PAGE_BOTTOM)).height(y(TAB_BOTTOM) - y(TAB_TOP)),
                        contentAlignment = Alignment.Center,
                    ) { Label(LABELS[i], size = 16.sp, bold = vm.tab == i) }
                }
            }
        }
    }
}

/** Draws the art's crop over the whole element: the parchment, and the selected tab's strip over its bottom. */
fun Modifier.pageArt(images: Images, tab: Int) = drawBehind {
    val w = size.width.roundToInt()
    val h = size.height.roundToInt()
    val left = Page.CROP_LEFT.toInt()
    val cropWidth = (Page.CROP_RIGHT - Page.CROP_LEFT).toInt()
    val cropTop = Page.CROP_TOP.toInt()
    val stripTop = Page.STRIP_TOP.toInt()
    val bottom = Page.CROP_BOTTOM.toInt()
    drawPart(images[Assets.page("background")], left, cropTop, cropWidth, bottom - cropTop, dstSize = IntSize(w, h))
    val top = (size.height * (stripTop - cropTop) / (bottom - cropTop)).roundToInt()
    drawPart(images[Page.strip(tab)], left, 0, cropWidth, bottom - stripTop, IntOffset(0, top), IntSize(w, h - top))
}
