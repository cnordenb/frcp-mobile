package net.frcp.app.picker

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import net.frcp.app.device.Assets
import net.frcp.app.device.DocumentFiles
import net.frcp.app.device.PrefsStore
import net.frcp.app.device.Sounds
import net.frcp.app.logic.GameData
import net.frcp.app.logic.Logic
import net.frcp.app.ui.Images

val PickerFactory = viewModelFactory {
    initializer {
        val app = checkNotNull(this[APPLICATION_KEY])
        val data = GameData.parse(app.assets.open(Assets.DATA).bufferedReader().use { it.readText() })
        val files = DocumentFiles(app.contentResolver)
        PickerViewModel(Logic(data), Images(app.assets::open), PrefsStore(app), Sounds(app.assets), files)
            .apply { decodeAhead() }
    }
}
