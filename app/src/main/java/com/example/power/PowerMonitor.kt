package com.example.power

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.view.WindowManager
import com.example.data.KioskPreferences
import com.example.model.PowerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PowerMonitor(
    private val activity: Activity,
    private val preferences: KioskPreferences
) {
    private val _powerState = MutableStateFlow(PowerState())
    val powerState: StateFlow<PowerState> = _powerState.asStateFlow()

    private val _isDisplaySleeping = MutableStateFlow(false)
    val isDisplaySleeping: StateFlow<Boolean> = _isDisplaySleeping.asStateFlow()

    private var wakeLock: PowerManager.WakeLock? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    handlePowerConnected()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    handlePowerDisconnected()
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val isPlugged = chargePlug == BatteryManager.BATTERY_PLUGGED_AC ||
                            chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                            chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS

                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = if (level != -1 && scale != -1) {
                        (level * 100 / scale.toFloat()).toInt()
                    } else 100

                    val plugType = when (chargePlug) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                        else -> "Battery Only"
                    }

                    _powerState.value = PowerState(
                        isPlugged = isPlugged,
                        batteryLevel = batteryPct,
                        isCharging = isCharging,
                        plugType = plugType
                    )

                    if (isPlugged) {
                        applyScreenOnPolicy(true)
                    } else if (preferences.sleepOnUnplugged) {
                        applyScreenOnPolicy(false)
                    }
                }
            }
        }
    }

    fun startMonitoring() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        val stickyIntent = activity.registerReceiver(receiver, filter)
        // Parse sticky intent immediately
        stickyIntent?.let { receiver.onReceive(activity, it) }
    }

    fun stopMonitoring() {
        try {
            activity.unregisterReceiver(receiver)
        } catch (_: Exception) {}
        releaseWakeLock()
    }

    private fun handlePowerConnected() {
        _isDisplaySleeping.value = false
        applyScreenOnPolicy(true)
        wakeDevice()
    }

    private fun handlePowerDisconnected() {
        applyScreenOnPolicy(false)
        if (preferences.sleepOnUnplugged) {
            _isDisplaySleeping.value = true
        }
    }

    fun wakeDisplayManual() {
        _isDisplaySleeping.value = false
    }

    private fun applyScreenOnPolicy(turnOn: Boolean) {
        activity.runOnUiThread {
            if (turnOn && preferences.autoDisplayOnPlugged) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun wakeDevice() {
        try {
            val powerManager = activity.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (wakeLock == null) {
                @Suppress("DEPRECATION")
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "KioskLauncher:PowerPlugWake"
                )
            }
            wakeLock?.acquire(3000L) // 3 seconds acquire to wake display
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }
}
