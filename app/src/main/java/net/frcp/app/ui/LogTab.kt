package net.frcp.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.frcp.app.PickerViewModel

/**
 * The Log tab: Clear Log and the share button over the drawn log, and Show over the remaining one, then the counts
 * just over the logs, and the logs filling the rest. The drawn log is the wider, as its lines are the longer.
 */
@Composable
fun LogTab(vm: PickerViewModel, topInset: Dp) {
    val view = vm.view
    val showRemaining = vm.settings.showRemaining
    val n = view.pool.size
    Column(
        Modifier.fillMaxSize().padding(top = 58.dp + topInset, start = 14.dp, end = 14.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(5f), verticalAlignment = Alignment.CenterVertically) {
                ParchmentButton("Clear Log", vm::clearLog)
                Spacer(Modifier.weight(1f))
                ShareButton("Share a link to this civ pool and this round's draws", { vm.shareLink(true) })
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(4f), contentAlignment = Alignment.CenterEnd) {
                CheckRow("Show", showRemaining, { vm.toggleSetting { copy(showRemaining = !showRemaining) } })
            }
        }
        Row {
            Label("Drawn: ${view.drawn}/$n", Modifier.weight(5f), bold = true)
            Spacer(Modifier.width(10.dp))
            Label("Remaining: ${view.remaining.size}/$n", Modifier.weight(4f), bold = true)
        }
        Row(Modifier.weight(1f)) {
            LogPanel(view.drawnRounds, "Drawn civs, newest first", Modifier.weight(5f).fillMaxHeight())
            Spacer(Modifier.width(10.dp))
            if (showRemaining) {
                LogPanel(listOf(view.remainingRows), "Civs left to draw this round", Modifier.weight(4f).fillMaxHeight())
            } else {
                Spacer(Modifier.weight(4f))
            }
        }
    }
}
