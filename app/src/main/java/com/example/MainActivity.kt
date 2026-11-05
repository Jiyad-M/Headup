package com.example

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.KioskHomeScreen
import com.example.ui.KioskViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: KioskViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return KioskViewModel(application, this@MainActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure display turns on cleanly without black lockscreen
        unlockAndTurnScreenOn()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.powerState.collect { state ->
                    if (state.isPlugged && viewModel.preferences.autoDisplayOnPlugged) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else if (!state.isPlugged && viewModel.preferences.sleepOnUnplugged) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }

        setContent {
            MyApplicationTheme {
                KioskHomeScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        unlockAndTurnScreenOn()
        viewModel.refreshDefaultHomeState()
        viewModel.refreshApps()
        viewModel.loadPaymentHistory()
        // Do NOT trigger gauge sweep on onResume (only triggers on charger plug / display on from plug)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        unlockAndTurnScreenOn()
        viewModel.refreshDefaultHomeState()
        val blockedPkg = intent.getStringExtra("BLOCKED_APP_PKG")
        if (!blockedPkg.isNullOrEmpty()) {
            viewModel.notifyAppBlocked(blockedPkg)
        }
    }

    private fun unlockAndTurnScreenOn() {
        try {
            val lp = window.attributes
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = lp

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                km?.requestDismissKeyguard(this, null)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
        } catch (_: Exception) {}
    }
}
