package net.frcp.app

import net.frcp.app.logic.Civ
import net.frcp.app.logic.Logic
import net.frcp.app.logic.PickerState
import net.frcp.app.logic.SharedLink

/** One line of a log: a civ's icon and its text. */
class LogRow(val civ: Int, val text: String)

/**
 * What the screen shows of the picker, worked out once per change, as the web version's render() does. [state] is a
 * copy that nothing changes.
 */
class PickerView(logic: Logic, val state: PickerState, val flash: String?) {
    val pool = logic.poolIds(state)
    val drawn = logic.drawnCount(state)
    val remaining = logic.remainingIds(state)
    val current: Civ? = logic.round(state).lastOrNull()?.let { logic.civs[it.id] }
    val editionTotal = logic.editionTotal(state.edition)

    /** The drawn log's rounds, newest first, each listing its draws newest first. */
    val drawnRounds: List<List<LogRow>> = state.log.asReversed().filter { it.isNotEmpty() }.map { r ->
        r.asReversed().map { d ->
            val name = logic.civs[d.id].name
            LogRow(d.id, if (d.k != 0) "$name (${d.k}/${d.n})" else name)
        }
    }

    /** The civs left this round, in alphabetical order. */
    val remainingRows: List<LogRow> = remaining.toSet().let { left ->
        logic.displayOrder.filter { it.id in left }.map { LogRow(it.id, it.name) }
    }
}

/** Something the screen does for the view model: open another app or a file picker. */
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

/** A status message. Its serial tells two of the same text apart, so the second shows too. */
data class Toast(val text: String, val serial: Long)

/** On the Draw tab, a swipe left draws, right undoes, up re-rolls and down resets. */
enum class SwipeKind { Draw, Undo, Reroll, Reset }
