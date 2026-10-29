package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apps.AppManager
import com.example.apps.SplitLaunchResult
import com.example.data.KioskPreferences
import com.example.model.AppItem
import com.example.model.OtpChallenge
import com.example.model.PaymentRecord
import com.example.model.PowerState
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
    UNINSTALL_APP
}

class KioskViewModel(
    application: Application,
    private val activity: Activity
) : AndroidViewModel(application) {

    val preferences = KioskPreferences(application)
    val securityManager = KioskSecurityManager(application, preferences)
    val appManager = AppManager(application, preferences)
    val soundboxManager = PaymentSoundboxManager(application, preferences)
    val powerMonitor = PowerMonitor(activity, preferences)

    private val _isActivated = MutableStateFlow(preferences.isActivated)
    val isActivated: StateFlow<Boolean> = _isActivated.asStateFlow()

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

    private val _showSplitResultDialog = MutableStateFlow(false)
    val showSplitResultDialog: StateFlow<Boolean> = _showSplitResultDialog.asStateFlow()

    private val _lastSplitResult = MutableStateFlow<SplitLaunchResult?>(null)
    val lastSplitResult: StateFlow<SplitLaunchResult?> = _lastSplitResult.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private val _paymentHistory = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentRecord>> = _paymentHistory.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isNotificationAccessGranted = MutableStateFlow(checkNotificationAccessGranted())
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    private val _hudTheme = MutableStateFlow(
        try {
            AutomotiveBrandTheme.valueOf(preferences.hudTheme)
        } catch (_: Exception) {
            AutomotiveBrandTheme.TESLA
        }
    )
    val hudTheme: StateFlow<AutomotiveBrandTheme> = _hudTheme.asStateFlow()

    val powerState: StateFlow<PowerState> = powerMonitor.powerState
    val isDisplaySleeping: StateFlow<Boolean> = powerMonitor.isDisplaySleeping
    val isAnnouncingPayment: StateFlow<Boolean> = soundboxManager.isAnnouncing
    val lastAnnouncedText: StateFlow<String> = soundboxManager.lastAnnouncedText

    init {
        powerMonitor.startMonitoring()
        refreshApps()
        loadPaymentHistory()

        // Hook real-time payment notification listener
        PaymentNotificationListenerService.onPaymentReceivedListener = { amount, rawText ->
            loadPaymentHistory()
            _statusMessage.value = "Payment Received: ₹$amount"
        }

        // Hook real-time incoming SMS recovery listener
        SmsRecoveryReceiver.onSmsRecoveryVerified = { sender, message ->
            _isRecoveryVerified.value = true
            _remainingRecoveryTimeMillis.value = securityManager.getRemainingLockTimeMillis()
            _statusMessage.value = "Recovery SMS verified from $sender! 3-Hour timelock initiated."
        }
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
        _statusMessage.value = "Kiosk Activated for $name! Prompting launcher selection..."

        // Prompt user to set as default launcher to switch from original Samsung One UI
        promptSetDefaultLauncher()
    }

    fun promptSetDefaultLauncher() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(Intent.createChooser(intent, "Select Kiosk Launcher to replace Samsung One UI"))
        } catch (e: Exception) {
            _statusMessage.value = "Open Settings -> Default Apps -> Home App to select Kiosk Launcher"
        }
    }

    fun setHudTheme(theme: AutomotiveBrandTheme) {
        _hudTheme.value = theme
        preferences.hudTheme = theme.name
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
        val success = appManager.launchApp(pkg)
        if (!success) {
            _statusMessage.value = "Unable to launch $pkg"
        }
    }

    fun launchUberMapsSplitScreen() {
        val result = appManager.launchSplitScreenUberAndMaps()
        _lastSplitResult.value = result
        _showSplitResultDialog.value = true
    }

    fun dismissSplitResultDialog() {
        _showSplitResultDialog.value = false
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
            activity.startActivity(Intent.createChooser(intent, "Choose Home Screen"))
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

    /**
     * Requirement 4:
     * When tap forget button a two random words displayed.
     * If the word received from alt number via SMS, change password after 3 hours.
     */
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
        // Enforce 3-hour rule if recovering, or normal update if authenticated
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
    }

    fun updateCurrency(newCurrency: String) {
        preferences.currency = newCurrency
    }

    fun wakeDisplay() {
        powerMonitor.wakeDisplayManual()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        powerMonitor.stopMonitoring()
        soundboxManager.release()
        PaymentNotificationListenerService.onPaymentReceivedListener = null
        SmsRecoveryReceiver.onSmsRecoveryVerified = null
    }
}
