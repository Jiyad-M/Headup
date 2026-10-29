package com.example.power

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.WindowManager
import com.example.data.KioskPreferences
import com.example.model.PowerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PowerMonitor(
    private val activity: Activity,
    private val preferences: KioskPreferences
) {
    private val _powerState = MutableStateFlow(PowerState())
    val powerState: StateFlow<PowerState> = _powerState.asStateFlow()

    private val _isDisplayCompletelyOff = MutableStateFlow(false)
    val isDisplayCompletelyOff: StateFlow<Boolean> = _isDisplayCompletelyOff.asStateFlow()

    private val _unplugCountdown = MutableStateFlow<Int?>(null)
    val unplugCountdown: StateFlow<Int?> = _unplugCountdown.asStateFlow()

    private var countdownJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
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
                        else -> "Battery Discharging"
                    }

                    val wasPlugged = _powerState.value.isPlugged
                    _powerState.value = PowerState(
                        isPlugged = isPlugged,
                        batteryLevel = batteryPct,
                        isCharging = isCharging,
                        plugType = plugType
                    )

                    if (isPlugged) {
                        handlePowerConnected()
                    } else if (wasPlugged && !isPlugged) {
                        handlePowerDisconnected()
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
        stickyIntent?.let { receiver.onReceive(activity, it) }
    }

    fun stopMonitoring() {
        try {
            activity.unregisterReceiver(receiver)
        } catch (_: Exception) {}
        countdownJob?.cancel()
        releaseWakeLock()
    }

    /**
     * Power plugged: Display ON permanently, screen bright, kiosk works.
     */
    fun handlePowerConnected() {
        countdownJob?.cancel()
        _unplugCountdown.value = null
        _isDisplayCompletelyOff.value = false

        activity.runOnUiThread {
            val lp = activity.window.attributes
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.window.attributes = lp
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        wakeDevice()
    }

    /**
     * Power unplugged:
     * "unplugged completly light off after 3 second like power button press compltly off"
     */
    fun handlePowerDisconnected() {
        countdownJob?.cancel()
        countdownJob = coroutineScope.launch {
            // 3-second countdown
            for (sec in 3 downTo 1) {
                _unplugCountdown.value = sec
                delay(1000)
            }
            _unplugCountdown.value = 0

            // Turn screen completely black/off
            _isDisplayCompletelyOff.value = true
            activity.runOnUiThread {
                val lp = activity.window.attributes
                lp.screenBrightness = 0.001f // Blackout brightness (lowest hardware level)
                activity.window.attributes = lp
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    /**
     * Emergency test simulator (for emulator or dev without power cable)
     */
    fun togglePowerSimulation() {
        val currentPlugged = _powerState.value.isPlugged
        if (currentPlugged) {
            _powerState.value = _powerState.value.copy(isPlugged = false, isCharging = false, plugType = "Battery (Simulated)")
            handlePowerDisconnected()
        } else {
            _powerState.value = _powerState.value.copy(isPlugged = true, isCharging = true, plugType = "AC Power (Simulated)")
            handlePowerConnected()
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
            wakeLock?.acquire(3000L)
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
