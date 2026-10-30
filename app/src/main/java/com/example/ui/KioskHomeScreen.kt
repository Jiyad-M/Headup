package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AccountActivationScreen
import com.example.ui.components.AdminSettingsSheet
import com.example.ui.components.AllowedAppsGrid
import com.example.ui.components.AutomotiveHudCluster
import com.example.ui.components.DisplaySleepOverlay
import com.example.ui.components.KioskTopBar
import com.example.ui.components.PaymentSoundboxCard
import com.example.ui.components.PinUnlockDialog
import com.example.ui.components.TataPunchCockpitView
import com.example.ui.components.TwoWordsRecoveryDialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.HudCyan
import com.example.ui.theme.Slate900
import com.example.ui.theme.TataPunchDarkJade
import com.example.ui.theme.TataPunchJade
import com.example.ui.theme.TataPunchMint

@Composable
fun KioskHomeScreen(viewModel: KioskViewModel) {
    val isActivated by viewModel.isActivated.collectAsStateWithLifecycle()
    val permittedApps by viewModel.permittedApps.collectAsStateWithLifecycle()
    val allInstalledApps by viewModel.allInstalledApps.collectAsStateWithLifecycle()
    val powerState by viewModel.powerState.collectAsStateWithLifecycle()
    val isDefaultHome by viewModel.isDefaultHome.collectAsStateWithLifecycle()
    val isDisplayCompletelyOff by viewModel.isDisplayCompletelyOff.collectAsStateWithLifecycle()
    val unplugCountdown by viewModel.unplugCountdown.collectAsStateWithLifecycle()
    val isAnnouncing by viewModel.isAnnouncingPayment.collectAsStateWithLifecycle()
    val lastAnnouncedText by viewModel.lastAnnouncedText.collectAsStateWithLifecycle()
    val paymentHistory by viewModel.paymentHistory.collectAsStateWithLifecycle()
    val hudTheme by viewModel.hudTheme.collectAsStateWithLifecycle()
    val activePinPurpose by viewModel.activePinPurpose.collectAsStateWithLifecycle()
    val isNotificationAccessGranted by viewModel.isNotificationAccessGranted.collectAsStateWithLifecycle()

    val gpsSpeed by viewModel.gpsSpeed.collectAsStateWithLifecycle()
    val hasGpsFix by viewModel.hasGpsFix.collectAsStateWithLifecycle()
    val gpsStatusText by viewModel.gpsStatusText.collectAsStateWithLifecycle()
    val activePaymentAlert by viewModel.activePaymentAlert.collectAsStateWithLifecycle()

    val showPinDialog by viewModel.showPinDialog.collectAsStateWithLifecycle()
    val showTwoWordsDialog by viewModel.showTwoWordsDialog.collectAsStateWithLifecycle()
    val recoveryWord1 by viewModel.recoveryWord1.collectAsStateWithLifecycle()
    val recoveryWord2 by viewModel.recoveryWord2.collectAsStateWithLifecycle()
    val isRecoveryVerified by viewModel.isRecoveryVerified.collectAsStateWithLifecycle()
    val remainingRecoveryTimeMillis by viewModel.remainingRecoveryTimeMillis.collectAsStateWithLifecycle()

    val showAdminSettings by viewModel.showAdminSettings.collectAsStateWithLifecycle()

    val pinError by viewModel.pinError.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val blockedAppAttempt by viewModel.blockedAppAttempt.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept back button in kiosk launcher so user cannot exit without 8-digit password
    BackHandler {
        if (isActivated) {
            viewModel.requestExitKiosk()
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // 1. Account Activation Gate
    if (!isActivated) {
        AccountActivationScreen(
            onActivate = { name, phone, altPhone, password ->
                viewModel.activateAccount(name, phone, altPhone, password)
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        // Primary Screen: 2026 Tata Punch EV Cockpit View (Matching Image 3)
        TataPunchCockpitView(
            permittedApps = permittedApps,
            powerState = powerState,
            isDefaultHome = isDefaultHome,
            isAnnouncing = isAnnouncing,
            lastAnnouncedText = lastAnnouncedText,
            gpsSpeed = gpsSpeed,
            hasGpsFix = hasGpsFix,
            gpsStatusText = gpsStatusText,
            onAppClick = { pkg -> viewModel.launchApp(pkg) },
            onPromptSetDefaultLauncher = { viewModel.promptSetDefaultLauncher() },
            onOpenSettings = { viewModel.openPinDialog(PinPurpose.ADMIN_ACCESS) },
            onQuickChimeTest = { amount -> viewModel.announcePayment(amount.toDouble()) },
            onToggleGpsSimulation = { viewModel.toggleGpsSimulation() }
        )

        // Realistic GPay Soundbox Alert Overlay
        com.example.ui.components.RealisticPaymentAlertCard(
            payment = activePaymentAlert,
            onDismiss = { viewModel.dismissPaymentAlert() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Snackbar Host for status notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // 6. Power Disconnected -> Complete Screen Light Blackout after 3s countdown (Screen OFF like power button)
        DisplaySleepOverlay(
            isCompletelyOff = isDisplayCompletelyOff,
            countdownSec = unplugCountdown,
            onSimulatePlugIn = { viewModel.togglePowerSimulation() }
        )

        // 7. 8-digit PIN Unlock Dialog (Protected Exit, Uninstall, or Admin Settings)
        if (showPinDialog) {
            PinUnlockDialog(
                purpose = activePinPurpose,
                errorMessage = pinError,
                onDismiss = { viewModel.closePinDialog() },
                onSubmitPin = { pin -> viewModel.submitPin(pin) },
                onForgotPinClick = { viewModel.startTwoWordsRecovery() }
            )
        }

        // 8. 2 Random Words Recovery Dialog with Alt Phone SMS & 3-Hour Timelock
        if (showTwoWordsDialog) {
            TwoWordsRecoveryDialog(
                word1 = recoveryWord1,
                word2 = recoveryWord2,
                altPhone = viewModel.preferences.altPhone,
                isVerified = isRecoveryVerified,
                remainingTimeMillis = remainingRecoveryTimeMillis,
                onDismiss = { viewModel.closeTwoWordsRecovery() },
                onManualSimulateSms = { sender, msg -> viewModel.simulateAltSmsVerification(sender, msg) },
                onRegenerateWords = { viewModel.regenerateRecoveryWords() },
                onProceedUnlock = { viewModel.proceedRecoveryUnlock() }
            )
        }

        // 9. Admin Settings Sheet (Allowed apps, Power policies, PIN change, Soundbox config, Exit/Uninstall)
        if (showAdminSettings) {
            AdminSettingsSheet(
                allApps = allInstalledApps,
                currentSecondaryPhone = viewModel.preferences.altPhone,
                autoDisplayOnPlugged = viewModel.preferences.autoDisplayOnPlugged,
                sleepOnUnplugged = viewModel.preferences.sleepOnUnplugged,
                currency = viewModel.preferences.currency,
                paymentHistory = paymentHistory,
                currentTheme = hudTheme,
                onThemeChange = { viewModel.setHudTheme(it) },
                onToggleAppAllowed = { pkg, allowed -> viewModel.toggleAppAllowed(pkg, allowed) },
                onToggleAppBlacklisted = { pkg, blacklisted -> viewModel.toggleAppBlacklisted(pkg, blacklisted) },
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

        // 10. Access Denied Dialog when user attempts to open an unauthorized/blacklisted app
        if (blockedAppAttempt != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissBlockedDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "ACCESS DENIED",
                        fontWeight = FontWeight.ExtraBold,
                        color = CrimsonRed
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "The application '$blockedAppAttempt' is blacklisted and strictly blocked by Kiosk Mode.",
                            color = Color.White
                        )
                        Text(
                            text = "Access to non-whitelisted and blacklisted applications is completely denied under active kiosk mode.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissBlockedDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Slate900,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
