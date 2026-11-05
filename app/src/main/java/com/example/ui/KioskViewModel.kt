package com.example.ui

import android.app.Activity
import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apps.AppManager
import com.example.data.KioskPreferences
import com.example.model.AppItem
import com.example.model.OtpChallenge
import com.example.model.PaymentRecord
import com.example.model.PowerState
import com.example.model.SoundboxTone
import com.example.model.SpeechEngine
import com.example.model.VoiceGender
import com.example.power.PowerMonitor
import com.example.security.KioskSecurityManager
import com.example.security.SmsRecoveryReceiver
import com.example.soundbox.PaymentNotificationListenerService
import com.example.soundbox.PaymentSoundboxManager
import com.example.ui.components.AutomotiveBrandTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PinPurpose {
    ADMIN_ACCESS,
    EXIT_KIOSK,
    UNINSTALL_APP,
    UNLOCK_BLOCKED_APP
}

class KioskViewModel(
    application: Application,
    private val activity: Activity
) : AndroidViewModel(application) {

    val preferences = KioskPreferences(application)
    val securityManager = KioskSecurityManager(application, preferences)
    val appManager = AppManager(application, preferences)
    val soundboxManager = PaymentSoundboxManager.getInstance(application, preferences)
    val powerMonitor = PowerMonitor.getInstance(application, preferences)
    val gpsSpeedometer = com.example.speed.GpsSpeedometerManager(application)

    private val _isActivated = MutableStateFlow(preferences.isActivated)
    val isActivated: StateFlow<Boolean> = _isActivated.asStateFlow()

    private val _isDefaultHome = MutableStateFlow(checkIsDefaultHome())
    val isDefaultHome: StateFlow<Boolean> = _isDefaultHome.asStateFlow()

    private val _permittedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val permittedApps: StateFlow<List<AppItem>> = _permittedApps.asStateFlow()

    private val _allInstalledApps = MutableStateFlow<List<AppItem>>(emptyList())
    val allInstalledApps: StateFlow<List<AppItem>> = _allInstalledApps.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _activePinPurpose = MutableStateFlow(PinPurpose.ADMIN_ACCESS)
    val activePinPurpose: StateFlow<PinPurpose> = _activePinPurpose.asStateFlow()

    private val _showTwoWordsDialog = MutableStateFlow(false)
    val showTwoWordsDialog: StateFlow<Boolean> = _showTwoWordsDialog.asStateFlow()

    private val _recoveryWord1 = MutableStateFlow(preferences.recoveryWord1)
    val recoveryWord1: StateFlow<String> = _recoveryWord1.asStateFlow()

    private val _recoveryWord2 = MutableStateFlow(preferences.recoveryWord2)
    val recoveryWord2: StateFlow<String> = _recoveryWord2.asStateFlow()

    private val _isRecoveryVerified = MutableStateFlow(preferences.isRecoveryVerified)
    val isRecoveryVerified: StateFlow<Boolean> = _isRecoveryVerified.asStateFlow()

    private val _remainingRecoveryTimeMillis = MutableStateFlow(securityManager.getRemainingLockTimeMillis())
    val remainingRecoveryTimeMillis: StateFlow<Long> = _remainingRecoveryTimeMillis.asStateFlow()

    private val _showAdminSettings = MutableStateFlow(false)
    val showAdminSettings: StateFlow<Boolean> = _showAdminSettings.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private val _paymentHistory = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentRecord>> = _paymentHistory.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var pendingUnlockPackage: String? = null

    private val _displayWakeTrigger = MutableStateFlow(System.currentTimeMillis())
    val displayWakeTrigger: StateFlow<Long> = _displayWakeTrigger.asStateFlow()

    fun triggerDisplayWake() {
        _displayWakeTrigger.value = System.currentTimeMillis()
    }

    private val _blockedAppAttempt = MutableStateFlow<String?>(null)
    val blockedAppAttempt: StateFlow<String?> = _blockedAppAttempt.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(checkLocationPermissionGranted())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _hasSmsPermission = MutableStateFlow(checkSmsPermissionGranted())
    val hasSmsPermission: StateFlow<Boolean> = _hasSmsPermission.asStateFlow()

    private val _isNotificationAccessGranted = MutableStateFlow(checkNotificationAccessGranted())
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    private val _isDeviceAdminActive = MutableStateFlow(PowerMonitor.isDeviceAdminActive(application))
    val isDeviceAdminActive: StateFlow<Boolean> = _isDeviceAdminActive.asStateFlow()

    private val _canWriteSettings = MutableStateFlow(PowerMonitor.canWriteSystemSettings(application))
    val canWriteSettings: StateFlow<Boolean> = _canWriteSettings.asStateFlow()

    private val _hasAllPermissions = MutableStateFlow(false)
    val hasAllPermissions: StateFlow<Boolean> = _hasAllPermissions.asStateFlow()

    private val _hudTheme = MutableStateFlow(
        try {
            AutomotiveBrandTheme.valueOf(preferences.hudTheme)
        } catch (_: Exception) {
            AutomotiveBrandTheme.TATA_PUNCH_EV
        }
    )
    val hudTheme: StateFlow<AutomotiveBrandTheme> = _hudTheme.asStateFlow()

    private val _isLightMode = MutableStateFlow(preferences.isLightMode)
    val isLightMode: StateFlow<Boolean> = _isLightMode.asStateFlow()

    val powerState: StateFlow<PowerState> = powerMonitor.powerState
    val powerPlugSweepTrigger: StateFlow<Long> = powerMonitor.powerPlugSweepTrigger
    val isDisplayCompletelyOff: StateFlow<Boolean> = powerMonitor.isDisplayCompletelyOff
    val unplugCountdown: StateFlow<Int?> = powerMonitor.unplugCountdown
    val isAnnouncingPayment: StateFlow<Boolean> = soundboxManager.isAnnouncing
    val lastAnnouncedText: StateFlow<String> = soundboxManager.lastAnnouncedText
    val activePaymentAlert: StateFlow<PaymentRecord?> = soundboxManager.activePaymentAlert

    private val _voiceGender = MutableStateFlow(
        try {
            VoiceGender.valueOf(preferences.voiceGender)
        } catch (_: Exception) {
            VoiceGender.MALE
        }
    )
    val voiceGender: StateFlow<VoiceGender> = _voiceGender.asStateFlow()

    private val _soundboxTone = MutableStateFlow(
        SoundboxTone.fromId(preferences.soundboxTone)
    )
    val soundboxTone: StateFlow<SoundboxTone> = _soundboxTone.asStateFlow()

    private val _speechEngine = MutableStateFlow(
        SpeechEngine.fromId(preferences.speechEngine)
    )
    val speechEngine: StateFlow<SpeechEngine> = _speechEngine.asStateFlow()

    val gpsSpeed: StateFlow<Int> = gpsSpeedometer.speedKmH
    val hasGpsFix: StateFlow<Boolean> = gpsSpeedometer.hasGpsFix
    val gpsStatusText: StateFlow<String> = gpsSpeedometer.gpsStatusText

    init {
        try {
            powerMonitor.startMonitoring()
        } catch (e: Exception) {
            android.util.Log.e("KioskViewModel", "powerMonitor init failed", e)
        }

        try {
            gpsSpeedometer.startTracking()
        } catch (e: Exception) {
            android.util.Log.e("KioskViewModel", "gpsSpeedometer init failed", e)
        }

        try {
            com.example.service.KioskBackgroundService.start(application)
        } catch (e: Exception) {
            android.util.Log.e("KioskViewModel", "KioskBackgroundService start failed", e)
        }

        try {
            refreshApps()
            loadPaymentHistory()
        } catch (e: Exception) {
            android.util.Log.e("KioskViewModel", "data loading failed", e)
        }

        PaymentNotificationListenerService.onPaymentReceivedListener = { amount, _ ->
            loadPaymentHistory()
            _statusMessage.value = "Payment Received: ₹$amount"
        }

        SmsRecoveryReceiver.onSmsRecoveryVerified = { sender, _ ->
            _isRecoveryVerified.value = true
            _remainingRecoveryTimeMillis.value = securityManager.getRemainingLockTimeMillis()
            _statusMessage.value = "Recovery SMS verified from $sender! 3-Hour timelock initiated."
        }
    }

    fun checkIsDefaultHome(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = activity.getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                }
            }
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
            val resolveInfo = activity.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == activity.packageName
        } catch (_: Exception) {
            false
        }
    }

    fun checkLocationPermissionGranted(): Boolean {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun checkSmsPermissionGranted(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun refreshDefaultHomeState() {
        refreshAllPermissions()
    }

    fun refreshAllPermissions() {
        val home = checkIsDefaultHome()
        val notif = checkNotificationAccessGranted()
        val loc = checkLocationPermissionGranted()
        val sms = checkSmsPermissionGranted()
        val admin = PowerMonitor.isDeviceAdminActive(activity)
        val writeSet = PowerMonitor.canWriteSystemSettings(activity)

        _isDefaultHome.value = home
        _isNotificationAccessGranted.value = notif
        _hasLocationPermission.value = loc
        _hasSmsPermission.value = sms
        _isDeviceAdminActive.value = admin
        _canWriteSettings.value = writeSet
        _hasAllPermissions.value = home && notif && loc && sms

        if (loc) {
            gpsSpeedometer.startTracking()
        }
    }

    /**
     * Prompts to set this app as Default Home
     * replacing Samsung One UI / default launcher when Home button is tapped.
     */
    fun promptSetDefaultLauncher(launcher: androidx.activity.result.ActivityResultLauncher<Intent>? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = activity.getSystemService(RoleManager::class.java)
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                        val roleIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                        if (launcher != null) {
                            launcher.launch(roleIntent)
                            return
                        } else {
                            try {
                                activity.startActivity(roleIntent)
                                return
                            } catch (_: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("KioskViewModel", "RoleManager error", e)
            }
        }

        // Direct Samsung One UI / Android settings
        openHomeSettingsDirectly()
    }

    /**
     * Opens system Default Apps or Home Settings directly
     * (100% reliable on Samsung One UI, Xiaomi, Oppo, and generic Android)
     */
    fun openHomeSettingsDirectly() {
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${activity.packageName}")
            }
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                _statusMessage.value = "Tap 'Home app' and select 'Kiosk Launcher'"
                return
            } catch (_: Exception) {}
        }

        _statusMessage.value = "Open Phone Settings -> Apps -> Default apps -> Home app -> Select Kiosk Launcher"
    }

    fun wakeDisplay() {
        powerMonitor.wakeDisplay()
        triggerDisplayWake()
    }

    fun checkNotificationAccessGranted(): Boolean {
        return try {
            val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(activity)
            enabledListeners.contains(activity.packageName)
        } catch (_: Exception) {
            false
        }
    }

    fun openNotificationAccessSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        } catch (_: Exception) {
            _statusMessage.value = "Cannot open notification settings automatically"
        }
    }

    fun activateAccount(name: String, phone: String, altPhone: String, password: String) {
        preferences.accountName = name
        preferences.primaryPhone = phone
        preferences.altPhone = altPhone
        preferences.pin = password
        preferences.isActivated = true
        _isActivated.value = true
        _statusMessage.value = "Kiosk Activated! Setting as default Home to convert Samsung Home button..."
        promptSetDefaultLauncher()
    }

    fun setHudTheme(theme: AutomotiveBrandTheme) {
        _hudTheme.value = theme
        preferences.hudTheme = theme.name
    }

    fun setLightMode(enabled: Boolean) {
        preferences.isLightMode = enabled
        _isLightMode.value = enabled
        _statusMessage.value = if (enabled) "Switched to Daylight Cockpit Mode" else "Switched to Night Cockpit Mode"
    }

    fun refreshApps() {
        viewModelScope.launch {
            _permittedApps.value = appManager.getPermittedApps()
            _allInstalledApps.value = appManager.loadAllInstalledApps()
        }
    }

    fun loadPaymentHistory() {
        _paymentHistory.value = preferences.getPaymentHistory()
    }

    fun launchApp(pkg: String) {
        if (!appManager.isPackageAllowed(pkg)) {
            pendingUnlockPackage = pkg
            _blockedAppAttempt.value = pkg
            openPinDialog(PinPurpose.UNLOCK_BLOCKED_APP)
            return
        }
        preferences.lastOpenedPackage = pkg
        val success = appManager.launchApp(pkg)
        if (!success) {
            _statusMessage.value = "Unable to launch $pkg"
        }
    }

    fun dismissBlockedDialog() {
        pendingUnlockPackage = null
        _blockedAppAttempt.value = null
    }

    fun notifyAppBlocked(pkg: String) {
        pendingUnlockPackage = pkg
        _blockedAppAttempt.value = pkg
        openPinDialog(PinPurpose.UNLOCK_BLOCKED_APP)
    }

    fun toggleAppBlacklisted(pkg: String, isBlacklisted: Boolean) {
        appManager.toggleAppBlacklisted(pkg, isBlacklisted)
        refreshApps()
        _statusMessage.value = if (isBlacklisted) "Added to Blacklist (Blocked)" else "Removed from Blacklist"
    }

    fun announcePayment(amount: Double) {
        soundboxManager.announcePayment(amount, preferences.currency) {
            loadPaymentHistory()
        }
    }

    fun openPinDialog(purpose: PinPurpose = PinPurpose.ADMIN_ACCESS) {
        _activePinPurpose.value = purpose
        _pinError.value = null
        _showPinDialog.value = true
    }

    fun closePinDialog() {
        _showPinDialog.value = false
        _pinError.value = null
    }

    fun requestExitKiosk() {
        openPinDialog(PinPurpose.EXIT_KIOSK)
    }

    fun requestUninstallKiosk() {
        openPinDialog(PinPurpose.UNINSTALL_APP)
    }

    fun submitPin(pin: String) {
        if (securityManager.verifyPin(pin)) {
            _showPinDialog.value = false
            _pinError.value = null

            when (_activePinPurpose.value) {
                PinPurpose.EXIT_KIOSK -> {
                    _statusMessage.value = "PIN Verified. Exiting to Samsung Home..."
                    performExitToLauncher()
                }
                PinPurpose.UNINSTALL_APP -> {
                    _statusMessage.value = "PIN Verified. Launching Uninstaller..."
                    performUninstallApp()
                }
                PinPurpose.ADMIN_ACCESS -> {
                    _isAdminAuthenticated.value = true
                    _showAdminSettings.value = true
                }
                PinPurpose.UNLOCK_BLOCKED_APP -> {
                    val pkgToUnlock = pendingUnlockPackage
                    pendingUnlockPackage = null
                    _blockedAppAttempt.value = null
                    if (pkgToUnlock != null) {
                        _statusMessage.value = "Password verified. Opening application..."
                        appManager.authorizeTemporarily(pkgToUnlock)
                        preferences.lastOpenedPackage = pkgToUnlock
                        appManager.launchAppDirectly(pkgToUnlock)
                    }
                }
            }
        } else {
            _pinError.value = "Incorrect 8-digit password. Authorization failed."
        }
    }

    fun performExitToLauncher() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(Intent.createChooser(intent, "Choose Samsung Home to exit"))
        } catch (_: Exception) {
            _statusMessage.value = "Could not open launcher selector"
        }
    }

    fun performUninstallApp() {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:${activity.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            _statusMessage.value = "Cannot open uninstaller: ${e.message}"
        }
    }

    fun startTwoWordsRecovery() {
        _showPinDialog.value = false
        val (w1, w2) = securityManager.generateTwoRandomWords()
        _recoveryWord1.value = w1
        _recoveryWord2.value = w2
        _isRecoveryVerified.value = preferences.isRecoveryVerified
        _remainingRecoveryTimeMillis.value = securityManager.getRemainingLockTimeMillis()
        _showTwoWordsDialog.value = true
    }

    fun closeTwoWordsRecovery() {
        _showTwoWordsDialog.value = false
    }

    fun regenerateRecoveryWords() {
        val (w1, w2) = securityManager.generateTwoRandomWords()
        _recoveryWord1.value = w1
        _recoveryWord2.value = w2
        _isRecoveryVerified.value = false
        _remainingRecoveryTimeMillis.value = 0L
    }

    fun simulateAltSmsVerification(sender: String, messageText: String): Boolean {
        val ok = securityManager.verifyTwoWordsFromAlt(sender, messageText)
        if (ok) {
            _isRecoveryVerified.value = true
            _remainingRecoveryTimeMillis.value = securityManager.getRemainingLockTimeMillis()
            _statusMessage.value = "SMS words verified! 3-hour timelock countdown active."
        }
        return ok
    }

    fun proceedRecoveryUnlock() {
        _showTwoWordsDialog.value = false
        _isAdminAuthenticated.value = true
        _showAdminSettings.value = true
        _statusMessage.value = "Kiosk unlocked via Alt Number verification."
    }

    fun closeAdminSettings() {
        _showAdminSettings.value = false
        _isAdminAuthenticated.value = false
        refreshApps()
    }

    fun toggleAppAllowed(pkg: String, allowed: Boolean) {
        appManager.toggleAppAllowed(pkg, allowed)
        refreshApps()
    }

    fun updatePin(newPin: String): Boolean {
        if (preferences.isRecoveryVerified && !securityManager.canChangePasswordNow()) {
            val remain = securityManager.getRemainingLockTimeMillis()
            val mins = (remain / (1000 * 60))
            _statusMessage.value = "Security timelock active: Cannot change password for another $mins minutes (3-hour rule)."
            return false
        }

        val ok = securityManager.updatePin(newPin)
        if (ok) {
            _statusMessage.value = "8-digit PIN updated successfully"
        }
        return ok
    }

    fun updateSecondaryPhone(newPhone: String) {
        securityManager.updateSecondaryPhone(newPhone)
        _statusMessage.value = "Alt phone updated"
    }

    fun updatePowerSettings(autoDisplayOn: Boolean, sleepOnUnplug: Boolean) {
        preferences.autoDisplayOnPlugged = autoDisplayOn
        preferences.sleepOnUnplugged = sleepOnUnplug
        if (sleepOnUnplug && !PowerMonitor.isDeviceAdminActive(activity)) {
            _statusMessage.value = "Device Admin permission needed for instant hardware screen off"
            requestDeviceAdmin()
        } else {
            _statusMessage.value = "Power settings saved"
        }
    }

    fun requestDeviceAdmin() {
        try {
            val intent = PowerMonitor.createDeviceAdminIntent(activity)
            activity.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("KioskViewModel", "Error requesting device admin", e)
        }
    }

    fun requestWriteSettings() {
        try {
            val intent = PowerMonitor.createWriteSettingsIntent(activity)
            activity.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("KioskViewModel", "Error requesting write settings", e)
        }
    }

    fun updateCurrency(newCurrency: String) {
        preferences.currency = newCurrency
    }

    fun restartGpsTracking() {
        gpsSpeedometer.startTracking()
    }

    fun dismissPaymentAlert() {
        soundboxManager.dismissActiveAlert()
    }

    fun setVoiceGender(gender: VoiceGender) {
        preferences.voiceGender = gender.name
        _voiceGender.value = gender
        soundboxManager.configureVoice(gender)
        _statusMessage.value = "${gender.displayName} selected"
    }

    fun setSoundboxTone(tone: SoundboxTone) {
        preferences.soundboxTone = tone.id
        _soundboxTone.value = tone
        _statusMessage.value = "${tone.displayName} selected"
    }

    fun previewTone(tone: SoundboxTone) {
        soundboxManager.playTone(tone)
    }

    fun setSpeechEngine(engine: SpeechEngine) {
        preferences.speechEngine = engine.id
        _speechEngine.value = engine
        soundboxManager.initTtsEngine(engine)
        _statusMessage.value = "${engine.displayName} selected"
    }

    fun enforceDefaultLauncherIfActive() {
        refreshDefaultHomeState()
    }

    fun previewVoice(gender: VoiceGender) {
        soundboxManager.testVoicePreview(gender)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        powerMonitor.stopMonitoring()
        gpsSpeedometer.stopTracking()
        soundboxManager.release()
        PaymentNotificationListenerService.onPaymentReceivedListener = null
        SmsRecoveryReceiver.onSmsRecoveryVerified = null
    }
}
