package net.frcp.app.picker

import net.frcp.app.device.Assets
import net.frcp.app.logic.Civ
import net.frcp.app.logic.Draw
import net.frcp.app.logic.Logic
import net.frcp.app.logic.PickerState
import net.frcp.app.logic.SharedLink

class LogRow(val icon: String, val text: String)

class PickerView(logic: Logic, val state: PickerState, val flash: String?) {
    val pool = logic.poolIds(state)
    val drawn = logic.drawnCount(state)
    val remaining = logic.remainingIds(state)
    val current: Civ? = logic.round(state).lastOrNull()?.let { logic.civs[it.id] }
    val editionTotal = logic.editionTotal(state.edition)

    val drawnRounds: List<List<LogRow>> = state.log.asReversed().filter { it.isNotEmpty() }.map { r ->
        r.asReversed().map { d ->
            val civ = logic.civs[d.id]
            LogRow(Assets.civIcon(civ.file), if (d.k != 0) "${civ.name} (${d.k}/${d.n})" else civ.name)
        }
    }

    val remainingRows: List<LogRow> = remaining.toSet().let { left ->
        logic.displayOrder.filter { it.id in left }.map { LogRow(Assets.civIcon(it.file), it.name) }
    }
}

sealed interface Effect {
    data class OpenUrl(val url: String) : Effect
    data class Share(val text: String) : Effect
    data class SavePreset(val fileName: String) : Effect
    data object LoadPreset : Effect
}

sealed interface Dialog {
    data object Options : Dialog
    data object About : Dialog
    data object OpenLink : Dialog
    data class Preview(val link: SharedLink) : Dialog
    data class Message(val title: String, val text: String) : Dialog
}

data class Toast(val text: String, val serial: Long)

enum class SwipeKind { Draw, Undo, Reroll, Reset }
