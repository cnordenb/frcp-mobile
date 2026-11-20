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
import net.frcp.app.device.Assets
import net.frcp.app.picker.PickerViewModel
import kotlin.math.roundToInt

class Page(val width: Dp, val height: Dp) {
    fun x(px: Float): Dp = width * ((px - CROP_LEFT) / (CROP_RIGHT - CROP_LEFT))
    fun y(px: Float): Dp = height * ((px - CROP_TOP) / (CROP_BOTTOM - CROP_TOP))

    val left = x(INNER_LEFT)
    val right = width - x(INNER_RIGHT)
    val top = y(INNER_TOP)
    val bottom = height - y(PAGE_BOTTOM)

    val tabTop = y(TAB_TOP)

    companion object {
        const val ART_WIDTH = 1024f
        const val ART_HEIGHT = 2135f
        const val STRIP_HEIGHT = 280f
        const val STRIP_TOP = ART_HEIGHT - STRIP_HEIGHT

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

        internal val TABS = listOf(59f to 351f, 374f to 650f, 676f to 965f)
        private val LABELS = listOf("Draw", "Log", "Pool")
        private val STRIPS = listOf("draw_tab", "log_tab", "pool_tab")

        fun strip(tab: Int) = Assets.page(STRIPS[tab])
    }

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
                    val aboveLabel = y(TAB_TOP) - y(PAGE_BOTTOM)
                    Box(
                        Modifier.fillMaxWidth().padding(top = aboveLabel).height(y(TAB_BOTTOM) - y(TAB_TOP)),
                        contentAlignment = Alignment.Center,
                    ) { Label(LABELS[i], size = 16.sp, bold = vm.tab == i) }
                }
            }
        }
    }
}

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
