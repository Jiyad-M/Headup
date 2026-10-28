package com.example.ui

import android.app.Activity
import android.app.Application
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
import com.example.soundbox.PaymentSoundboxManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KioskViewModel(
    application: Application,
    activity: Activity
) : AndroidViewModel(application) {

    val preferences = KioskPreferences(application)
    val securityManager = KioskSecurityManager(application, preferences)
    val appManager = AppManager(application, preferences)
    val soundboxManager = PaymentSoundboxManager(application, preferences)
    val powerMonitor = PowerMonitor(activity, preferences)

    private val _permittedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val permittedApps: StateFlow<List<AppItem>> = _permittedApps.asStateFlow()

    private val _allInstalledApps = MutableStateFlow<List<AppItem>>(emptyList())
    val allInstalledApps: StateFlow<List<AppItem>> = _allInstalledApps.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _showOtpDialog = MutableStateFlow(false)
    val showOtpDialog: StateFlow<Boolean> = _showOtpDialog.asStateFlow()

    private val _showAdminSettings = MutableStateFlow(false)
    val showAdminSettings: StateFlow<Boolean> = _showAdminSettings.asStateFlow()

    private val _showSplitResultDialog = MutableStateFlow(false)
    val showSplitResultDialog: StateFlow<Boolean> = _showSplitResultDialog.asStateFlow()

    private val _lastSplitResult = MutableStateFlow<SplitLaunchResult?>(null)
    val lastSplitResult: StateFlow<SplitLaunchResult?> = _lastSplitResult.asStateFlow()

    private val _otpChallenge = MutableStateFlow<OtpChallenge?>(null)
    val otpChallenge: StateFlow<OtpChallenge?> = _otpChallenge.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError.asStateFlow()

    private val _paymentHistory = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentRecord>> = _paymentHistory.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val powerState: StateFlow<PowerState> = powerMonitor.powerState
    val isDisplaySleeping: StateFlow<Boolean> = powerMonitor.isDisplaySleeping
    val isAnnouncingPayment: StateFlow<Boolean> = soundboxManager.isAnnouncing
    val lastAnnouncedText: StateFlow<String> = soundboxManager.lastAnnouncedText

    init {
        powerMonitor.startMonitoring()
        refreshApps()
        loadPaymentHistory()
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

    /**
     * Programmatically launch both Uber Driver and Google Maps in split-screen mode
     */
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

    fun openPinDialog() {
        _pinError.value = null
        _showPinDialog.value = true
    }

    fun closePinDialog() {
        _showPinDialog.value = false
        _pinError.value = null
    }

    fun submitPin(pin: String) {
        if (securityManager.verifyPin(pin)) {
            _isAdminAuthenticated.value = true
            _showPinDialog.value = false
            _pinError.value = null
            _showAdminSettings.value = true
        } else {
            _pinError.value = "Incorrect 8-digit PIN. Please try again or use Emergency OTP."
        }
    }

    fun startOtpRecovery() {
        _showPinDialog.value = false
        val challenge = securityManager.createOtpChallenge()
        _otpChallenge.value = challenge
        _otpError.value = null
        _showOtpDialog.value = true
    }

    fun closeOtpDialog() {
        _showOtpDialog.value = false
        _otpError.value = null
    }

    fun submitOtp(otp: String) {
        if (securityManager.verifyOtp(otp)) {
            _isAdminAuthenticated.value = true
            _showOtpDialog.value = false
            _otpError.value = null
            _showAdminSettings.value = true
            _statusMessage.value = "Emergency OTP verified! Admin unlocked."
        } else {
            _otpError.value = "Invalid OTP code. Please check secondary phone or resend."
        }
    }

    fun resendOtp() {
        val challenge = securityManager.createOtpChallenge()
        _otpChallenge.value = challenge
        _otpError.value = null
        _statusMessage.value = "New OTP code generated and sent!"
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
        val ok = securityManager.updatePin(newPin)
        if (ok) {
            _statusMessage.value = "8-digit PIN updated successfully"
        }
        return ok
    }

    fun updateSecondaryPhone(newPhone: String) {
        securityManager.updateSecondaryPhone(newPhone)
        _statusMessage.value = "Secondary phone updated"
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
    }
}
