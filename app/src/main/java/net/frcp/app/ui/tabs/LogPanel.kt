package net.frcp.app.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.frcp.app.device.Assets
import net.frcp.app.picker.LogRow
import net.frcp.app.ui.Ink
import net.frcp.app.ui.Label
import net.frcp.app.ui.LocalImages
import net.frcp.app.ui.nineSlice
import net.frcp.app.ui.widgets.image

private object NoFling : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float) = initialVelocity
}

@Composable
fun LogPanel(rounds: List<List<LogRow>>, description: String, modifier: Modifier = Modifier) {
    val images = LocalImages.current
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
                    Row(
                        Modifier.height(ROW_HEIGHT),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(18.dp).image(images[row.icon]))
                        Label(row.text, color = Ink.LogText, oneLine = true)
                    }
                }
            }
        }
        if (hasBar) LogScrollBar(list, Modifier.align(Alignment.TopEnd).padding(6.dp).fillMaxHeight().width(27.dp))
    }
}

internal val ROW_HEIGHT = 20.dp

