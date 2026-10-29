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
import com.example.ui.components.AutomotiveHudCluster
import com.example.ui.components.DisplaySleepOverlay
import com.example.ui.components.KioskTopBar
import com.example.ui.components.OtpRecoveryDialog
import com.example.ui.components.PaymentSoundboxCard
import com.example.ui.components.PinUnlockDialog
import com.example.ui.components.SplitResultDialog
import com.example.ui.components.SplitScreenShortcutCard
import com.example.ui.theme.CockpitBlack

@Composable
fun KioskHomeScreen(viewModel: KioskViewModel) {
    val permittedApps by viewModel.permittedApps.collectAsStateWithLifecycle()
    val allInstalledApps by viewModel.allInstalledApps.collectAsStateWithLifecycle()
    val powerState by viewModel.powerState.collectAsStateWithLifecycle()
    val isDisplaySleeping by viewModel.isDisplaySleeping.collectAsStateWithLifecycle()
    val isAnnouncing by viewModel.isAnnouncingPayment.collectAsStateWithLifecycle()
    val lastAnnouncedText by viewModel.lastAnnouncedText.collectAsStateWithLifecycle()
    val paymentHistory by viewModel.paymentHistory.collectAsStateWithLifecycle()
    val hudTheme by viewModel.hudTheme.collectAsStateWithLifecycle()
    val activePinPurpose by viewModel.activePinPurpose.collectAsStateWithLifecycle()

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
        // Back press requests password-protected exit
        viewModel.requestExitKiosk()
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
            .background(CockpitBlack)
            .safeDrawingPadding()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = CockpitBlack,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                KioskTopBar(
                    powerState = powerState,
                    isAnnouncing = isAnnouncing,
                    currentTheme = hudTheme,
                    onLockClick = { viewModel.openPinDialog(PinPurpose.ADMIN_ACCESS) },
                    onSoundboxClick = { viewModel.openPinDialog(PinPurpose.ADMIN_ACCESS) },
                    onExitClick = { viewModel.requestExitKiosk() }
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

                // 1. Tesla / Tata EV Head-Up Display (HUD) Instrument Cluster
                AutomotiveHudCluster(
                    powerState = powerState,
                    currentTheme = hudTheme,
                    onThemeChange = { viewModel.setHudTheme(it) }
                )

                // 2. Uber Driver + Google Maps Split Screen Shortcut Card
                SplitScreenShortcutCard(
                    onLaunchSplitScreen = { viewModel.launchUberMapsSplitScreen() }
                )

                // 3. Allowed Apps Whitelist Grid (Hides unwanted apps)
                AllowedAppsGrid(
                    apps = permittedApps,
                    onAppClick = { pkg -> viewModel.launchApp(pkg) },
                    onConfigureAppsClick = { viewModel.openPinDialog(PinPurpose.ADMIN_ACCESS) }
                )

                // 4. Payment Soundbox Card ("{Tone}{ payment amount} received")
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

        // 5. Power goes -> Display Sleep Overlay
        DisplaySleepOverlay(
            isSleeping = isDisplaySleeping,
            powerState = powerState,
            onWakeTap = { viewModel.wakeDisplay() }
        )

        // 6. 8-digit PIN Unlock Dialog (Protected Exit, Uninstall, or Admin Settings)
        if (showPinDialog) {
            PinUnlockDialog(
                purpose = activePinPurpose,
                errorMessage = pinError,
                onDismiss = { viewModel.closePinDialog() },
                onSubmitPin = { pin -> viewModel.submitPin(pin) },
                onForgotPinClick = { viewModel.startOtpRecovery() }
            )
        }

        // 7. Emergency OTP Recovery Dialog with Random Words
        if (showOtpDialog) {
            OtpRecoveryDialog(
                challenge = otpChallenge,
                errorMessage = otpError,
                onDismiss = { viewModel.closeOtpDialog() },
                onSubmitOtp = { otp -> viewModel.submitOtp(otp) },
                onResendOtp = { viewModel.resendOtp() }
            )
        }

        // 8. Split-Screen Launch feedback dialog
        if (showSplitResult && lastSplitResult != null) {
            SplitResultDialog(
                result = lastSplitResult!!,
                onDismiss = { viewModel.dismissSplitResultDialog() },
                onInstallUber = { viewModel.appManager.openPlayStoreForUberDriver() },
                onOpenMapsFallback = { viewModel.appManager.openMapsNavigationFallback() }
            )
        }

        // 9. Admin Settings Sheet (Allowed apps, Power policies, PIN change, Soundbox config, Exit/Uninstall)
        if (showAdminSettings) {
            AdminSettingsSheet(
                allApps = allInstalledApps,
                currentSecondaryPhone = viewModel.preferences.secondaryPhone,
                autoDisplayOnPlugged = viewModel.preferences.autoDisplayOnPlugged,
                sleepOnUnplugged = viewModel.preferences.sleepOnUnplugged,
                currency = viewModel.preferences.currency,
                paymentHistory = paymentHistory,
                currentTheme = hudTheme,
                onThemeChange = { viewModel.setHudTheme(it) },
                onToggleAppAllowed = { pkg, allowed -> viewModel.toggleAppAllowed(pkg, allowed) },
                onUpdatePin = { newPin -> viewModel.updatePin(newPin) },
                onUpdateSecondaryPhone = { phone -> viewModel.updateSecondaryPhone(phone) },
                onUpdatePowerSettings = { disp, sleep -> viewModel.updatePowerSettings(disp, sleep) },
                onUpdateCurrency = { curr -> viewModel.updateCurrency(curr) },
                onTestAnnouncement = { amt -> viewModel.announcePayment(amt) },
                onRequestExit = {
                    viewModel.closeAdminSettings()
                    viewModel.requestExitKiosk()
                },
                onRequestUninstall = {
                    viewModel.closeAdminSettings()
                    viewModel.requestUninstallKiosk()
                },
                onClose = { viewModel.closeAdminSettings() }
            )
        }
    }
}
