package com.example.power

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
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

/**
 * System-wide Power Monitor that manages hardware display state.
 * When plugged: screen is awakened and kept continuously ON (zero sleep).
 * When unplugged: after 3 seconds silent wait, the hardware display turns completely off
 * via DevicePolicyManager.lockNow() across ALL apps (e.g. Uber, Google Maps).
 */
class PowerMonitor private constructor(
    private val context: Context,
    private val preferences: KioskPreferences
) {
    companion object {
        private const val TAG = "PowerMonitor"

        @Volatile
        private var INSTANCE: PowerMonitor? = null

        fun getInstance(context: Context, preferences: KioskPreferences? = null): PowerMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PowerMonitor(
                    context.applicationContext,
                    preferences ?: KioskPreferences(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }

        fun isDeviceAdminActive(context: Context): Boolean {
            return try {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                val admin = KioskDeviceAdminReceiver.getComponentName(context)
                dpm?.isAdminActive(admin) == true
            } catch (_: Exception) {
                false
            }
        }

        fun createDeviceAdminIntent(context: Context): Intent {
            return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, KioskDeviceAdminReceiver.getComponentName(context))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Enables instant hardware display turn-off when power is disconnected across any app (Uber, Maps, etc.)."
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        fun canWriteSystemSettings(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.System.canWrite(context)
            } else {
                true
            }
        }

        fun createWriteSettingsIntent(context: Context): Intent {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }

        /**
         * Real-time synchronous battery state query directly from Android BatteryManager
         */
        fun queryLiveBatteryState(context: Context): PowerState {
            return try {
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                if (intent != null) {
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
                    PowerState(
                        isPlugged = isPlugged,
                        batteryLevel = batteryPct,
                        isCharging = isCharging,
                        plugType = plugType
                    )
                } else {
                    PowerState()
                }
            } catch (_: Exception) {
                PowerState()
            }
        }
    }

    private val _powerState = MutableStateFlow(queryLiveBatteryState(context))
    val powerState: StateFlow<PowerState> = _powerState.asStateFlow()

    private val _isDisplayCompletelyOff = MutableStateFlow(false)
    val isDisplayCompletelyOff: StateFlow<Boolean> = _isDisplayCompletelyOff.asStateFlow()

    private val _unplugCountdown = MutableStateFlow<Int?>(null)
    val unplugCountdown: StateFlow<Int?> = _unplugCountdown.asStateFlow()

    private var powerOffJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            intent ?: return
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    _powerState.value = _powerState.value.copy(
                        isPlugged = true,
                        isCharging = true,
                        plugType = "AC Wall Charger"
                    )
                    handlePowerConnected()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    _powerState.value = _powerState.value.copy(
                        isPlugged = false,
                        isCharging = false,
                        plugType = "Battery Discharging"
                    )
                    handlePowerDisconnected()
                }
                Intent.ACTION_SCREEN_ON -> {
                    handleScreenOn()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    handleScreenOff()
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

                    if (isPlugged && !wasPlugged) {
                        handlePowerConnected()
                    } else if (!isPlugged && wasPlugged) {
                        handlePowerDisconnected()
                    }
                }
            }
        }
    }

    fun startMonitoring() {
        if (isRegistered) return
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            val stickyIntent = ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            isRegistered = true
            stickyIntent?.let { receiver.onReceive(context, it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering power receiver", e)
        }
    }

    fun stopMonitoring() {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(receiver)
            isRegistered = false
        } catch (_: Exception) {}
        powerOffJob?.cancel()
    }

    private val _powerPlugSweepTrigger = MutableStateFlow(0L)
    val powerPlugSweepTrigger: StateFlow<Long> = _powerPlugSweepTrigger.asStateFlow()

    private val recentWakeTimestamps = mutableListOf<Long>()
    private var isTemporaryWakeOverridden = false

    /**
     * Power plugged: Display ON permanently, screen bright, zero sleep.
     * Restores the previous app that was last open (Uber, Google Maps, etc.)
     * and triggers the car ignition gauge sweep animation on the display.
     */
    fun handlePowerConnected() {
        powerOffJob?.cancel()
        powerOffJob = null
        recentWakeTimestamps.clear()
        isTemporaryWakeOverridden = false
        _unplugCountdown.value = null
        _isDisplayCompletelyOff.value = false

        // Immediately update state to plugged so UI and lock watchdog are in sync
        _powerState.value = _powerState.value.copy(
            isPlugged = true,
            isCharging = true,
            plugType = "AC Wall Charger"
        )

        // Always wake the hardware display when plugged in
        wakeDevice()

        if (preferences.autoDisplayOnPlugged) {
            applyNoSleepScreenTimeout()
        } else {
            restoreNormalScreenTimeout()
        }

        // Directly bring Kiosk app to front on charger plugged (display on, then kiosk app)
        bringKioskToFront()

        // Trigger gauge sweep on display on from charger plug
        _powerPlugSweepTrigger.value = System.currentTimeMillis()
    }

    /**
     * Power unplugged:
     * After 3 seconds silent delay (no animation, no fake black box), turn OFF physical display hardware.
     */
    fun handlePowerDisconnected() {
        powerOffJob?.cancel()
        recentWakeTimestamps.clear()
        isTemporaryWakeOverridden = false
        _unplugCountdown.value = null

        // Immediately mark unplugged so UI updates instantly and displays correct status
        _powerState.value = _powerState.value.copy(
            isPlugged = false,
            isCharging = false,
            plugType = "Battery Discharging"
        )

        if (!preferences.sleepOnUnplugged) {
            return
        }

        powerOffJob = coroutineScope.launch {
            // Wait 3 seconds silently (as requested: "within 3seconds. Remove 3 secont animation")
            delay(3000L)

            // If still unplugged, turn off display hardware
            val liveState = queryLiveBatteryState(context)
            if (!_powerState.value.isPlugged && !liveState.isPlugged) {
                turnHardwareScreenOff()
            }
        }
    }

    /**
     * Handles display waking up (via power button or display double-tap):
     * If power mode is active (sleepOnUnplugged = true) and device is unplugged:
     * - Single power button press or double tap to wake is DENIED (turns hardware display immediately back OFF).
     * - Triple power button press (3 presses within 2.5s) allows the display to turn ON.
     */
    fun handleScreenOn() {
        val liveState = queryLiveBatteryState(context)
        val isPlugged = _powerState.value.isPlugged && liveState.isPlugged
        val sleepOnUnplugged = preferences.sleepOnUnplugged

        // Ensure state is kept synchronized if live check reports unplugged
        if (!isPlugged && _powerState.value.isPlugged) {
            _powerState.value = _powerState.value.copy(
                isPlugged = false,
                isCharging = false,
                plugType = "Battery Discharging"
            )
        }

        // If not in sleep on unplugged mode or device is currently plugged into charger, allow screen on
        if (!sleepOnUnplugged || isPlugged) {
            recentWakeTimestamps.clear()
            return
        }

        // If already unlocked via triple press in this session, keep screen on
        if (isTemporaryWakeOverridden) {
            return
        }

        val now = System.currentTimeMillis()
        // Retain wake events within the last 2500ms
        recentWakeTimestamps.removeAll { now - it > 2500L }
        recentWakeTimestamps.add(now)

        if (recentWakeTimestamps.size >= 3) {
            // OVERRIDE: Triple power button press detected!
            Log.i(TAG, "Triple power button press detected while unplugged! Allowing display to stay ON.")
            recentWakeTimestamps.clear()
            isTemporaryWakeOverridden = true
            _isDisplayCompletelyOff.value = false
            restoreNormalScreenTimeout()
            bringKioskToFront()
        } else {
            // DENY: Single power button press or double tap
            Log.i(TAG, "Display turn-on DENIED while unplugged (Wake attempt ${recentWakeTimestamps.size}/3). Forcing screen OFF.")
            turnHardwareScreenOff()
        }
    }

    fun handleScreenOff() {
        // Reset temporary triple-press override on screen off so next wake requires 3 presses again
        isTemporaryWakeOverridden = false
    }

    fun wakeDisplay() {
        powerOffJob?.cancel()
        powerOffJob = null
        _unplugCountdown.value = null
        _isDisplayCompletelyOff.value = false
        wakeDevice()
    }

    private fun turnHardwareScreenOff() {
        _isDisplayCompletelyOff.value = true

        // 1. Primary: Use DevicePolicyManager.lockNow() for true hardware screen and light turn-off
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val admin = KioskDeviceAdminReceiver.getComponentName(context)
            if (dpm != null && dpm.isAdminActive(admin)) {
                dpm.lockNow()
                Log.i(TAG, "Hardware display turned OFF via DevicePolicyManager.lockNow()")
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "DevicePolicyManager lockNow failed", e)
        }

        // 2. Secondary: Use AccessibilityService GLOBAL_ACTION_LOCK_SCREEN for instant hardware screen lock
        try {
            if (com.example.service.KioskAppLockAccessibilityService.lockScreen()) {
                Log.i(TAG, "Hardware display turned OFF via AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN")
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Accessibility lockScreen failed", e)
        }

        // 3. Fallback: Set screen timeout to 1000ms if Write Settings permission is available
        if (canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    1000 // 1 second timeout
                )
                Log.i(TAG, "Screen off timeout set to 1000ms via Settings.System")
            } catch (e: Exception) {
                Log.e(TAG, "Failed setting screen timeout", e)
            }
        }
    }

    private fun applyNoSleepScreenTimeout() {
        if (canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    86400000 // 24 hours (continuous display on)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed setting no-sleep timeout", e)
            }
        }
    }

    private fun restoreNormalScreenTimeout() {
        if (canWriteSystemSettings(context)) {
            try {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_OFF_TIMEOUT,
                    60000 // Standard 1 minute timeout
                )
            } catch (_: Exception) {}
        }
    }

    fun bringKioskToFront() {
        try {
            val intent = Intent(context, com.example.MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            }
            context.startActivity(intent)
            Log.i(TAG, "Directly brought Kiosk app to front on power connected")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bring kiosk app to front", e)
        }
    }

    fun restoreLastOpenedApp() {
        val lastPkg = preferences.lastOpenedPackage
        if (lastPkg.isNotBlank() && lastPkg != context.packageName) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(lastPkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                if (intent != null) {
                    context.startActivity(intent)
                    Log.i(TAG, "Restored last opened app on power connect: $lastPkg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore last opened app $lastPkg", e)
            }
        }
    }

    private fun wakeDevice() {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            @Suppress("DEPRECATION")
            val wl = pm?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "KioskLauncher:PowerPlugWake"
            )
            wl?.acquire(4000L)
        } catch (e: Exception) {
            Log.w(TAG, "Wake lock error", e)
        }
    }
}
