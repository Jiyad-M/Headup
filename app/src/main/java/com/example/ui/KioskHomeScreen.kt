package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AdminSettingsSheet
import com.example.ui.components.AllowedAppsGrid
import com.example.ui.components.DisplaySleepOverlay
import com.example.ui.components.KioskTopBar
import com.example.ui.components.OtpRecoveryDialog
import com.example.ui.components.PaymentSoundboxCard
import com.example.ui.components.PinUnlockDialog
import com.example.ui.components.SplitResultDialog
import com.example.ui.components.SplitScreenShortcutCard
import com.example.ui.theme.Slate950

@Composable
fun KioskHomeScreen(viewModel: KioskViewModel) {
    val permittedApps by viewModel.permittedApps.collectAsStateWithLifecycle()
    val allInstalledApps by viewModel.allInstalledApps.collectAsStateWithLifecycle()
    val powerState by viewModel.powerState.collectAsStateWithLifecycle()
    val isDisplaySleeping by viewModel.isDisplaySleeping.collectAsStateWithLifecycle()
    val isAnnouncing by viewModel.isAnnouncingPayment.collectAsStateWithLifecycle()
    val lastAnnouncedText by viewModel.lastAnnouncedText.collectAsStateWithLifecycle()
    val paymentHistory by viewModel.paymentHistory.collectAsStateWithLifecycle()

    val showPinDialog by viewModel.showPinDialog.collectAsStateWithLifecycle()
    val showOtpDialog by viewModel.showOtpDialog.collectAsStateWithLifecycle()
    val showAdminSettings by viewModel.showAdminSettings.collectAsStateWithLifecycle()
    val showSplitResult by viewModel.showSplitResultDialog.collectAsStateWithLifecycle()
    val lastSplitResult by viewModel.lastSplitResult.collectAsStateWithLifecycle()

    val otpChallenge by viewModel.otpChallenge.collectAsStateWithLifecycle()
    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val otpError by viewModel.otpError.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept back button in kiosk launcher so the user cannot simply back out of the kiosk
    BackHandler {
        // In kiosk mode, back button remains on home launcher screen
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .safeDrawingPadding()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Slate950,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                KioskTopBar(
                    powerState = powerState,
                    isAnnouncing = isAnnouncing,
                    onLockClick = { viewModel.openPinDialog() },
                    onSoundboxClick = { viewModel.openPinDialog() }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("kiosk_main_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // 1. Uber Driver + Google Maps Split Screen Shortcut Card
                SplitScreenShortcutCard(
                    onLaunchSplitScreen = { viewModel.launchUberMapsSplitScreen() }
                )

                // 2. Allowed Apps Whitelist Grid (Hides unwanted apps)
                AllowedAppsGrid(
                    apps = permittedApps,
                    onAppClick = { pkg -> viewModel.launchApp(pkg) },
                    onConfigureAppsClick = { viewModel.openPinDialog() }
                )

                // 3. Payment Soundbox Card ("{Tone}{ payment amount} received")
                PaymentSoundboxCard(
                    currency = viewModel.preferences.currency,
                    isAnnouncing = isAnnouncing,
                    lastAnnouncedText = lastAnnouncedText,
                    recentPayments = paymentHistory,
                    onAnnounce = { amount -> viewModel.announcePayment(amount) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 4. Power goes -> Display Sleep Overlay
        DisplaySleepOverlay(
            isSleeping = isDisplaySleeping,
            powerState = powerState,
            onWakeTap = { viewModel.wakeDisplay() }
        )

        // 5. 8-digit PIN Unlock Dialog
        if (showPinDialog) {
            PinUnlockDialog(
                errorMessage = pinError,
                onDismiss = { viewModel.closePinDialog() },
                onSubmitPin = { pin -> viewModel.submitPin(pin) },
                onForgotPinClick = { viewModel.startOtpRecovery() }
            )
        }

        // 6. Emergency OTP Recovery Dialog with Random Words
        if (showOtpDialog) {
            OtpRecoveryDialog(
                challenge = otpChallenge,
                errorMessage = otpError,
                onDismiss = { viewModel.closeOtpDialog() },
                onSubmitOtp = { otp -> viewModel.submitOtp(otp) },
                onResendOtp = { viewModel.resendOtp() }
            )
        }

        // 7. Split-Screen Launch feedback dialog
        if (showSplitResult && lastSplitResult != null) {
            SplitResultDialog(
                result = lastSplitResult!!,
                onDismiss = { viewModel.dismissSplitResultDialog() },
                onInstallUber = { viewModel.appManager.openPlayStoreForUberDriver() },
                onOpenMapsFallback = { viewModel.appManager.openMapsNavigationFallback() }
            )
        }

        // 8. Admin Settings Sheet (Allowed apps, Power policies, PIN change, Soundbox config)
        if (showAdminSettings) {
            AdminSettingsSheet(
                allApps = allInstalledApps,
                currentSecondaryPhone = viewModel.preferences.secondaryPhone,
                autoDisplayOnPlugged = viewModel.preferences.autoDisplayOnPlugged,
                sleepOnUnplugged = viewModel.preferences.sleepOnUnplugged,
                currency = viewModel.preferences.currency,
                paymentHistory = paymentHistory,
                onToggleAppAllowed = { pkg, allowed -> viewModel.toggleAppAllowed(pkg, allowed) },
                onUpdatePin = { newPin -> viewModel.updatePin(newPin) },
                onUpdateSecondaryPhone = { phone -> viewModel.updateSecondaryPhone(phone) },
                onUpdatePowerSettings = { disp, sleep -> viewModel.updatePowerSettings(disp, sleep) },
                onUpdateCurrency = { curr -> viewModel.updateCurrency(curr) },
                onTestAnnouncement = { amt -> viewModel.announcePayment(amt) },
                onClose = { viewModel.closeAdminSettings() }
            )
        }
    }
}
