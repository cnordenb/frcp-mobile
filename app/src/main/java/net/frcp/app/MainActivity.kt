package net.frcp.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import net.frcp.app.ui.AppScreen

class MainActivity : ComponentActivity() {
    private val vm: PickerViewModel by viewModels { PickerViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The page runs under the status bar, whose icons are dark on it. The navigation bar's are set by AppScreen.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) vm.onLaunch(intent?.dataString)
        setContent { AppScreen(vm) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        vm.onLink(intent.dataString)
    }
}
