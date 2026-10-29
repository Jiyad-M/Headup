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

                // Notification Access Banner for GPay / UPI Auto-Chime
                if (!isNotificationAccessGranted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberGold.copy(alpha = 0.15f))
                            .border(1.dp, AmberGold, RoundedCornerShape(12.dp))
                            .clickable { viewModel.openNotificationAccessSettings() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("notification_access_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable GPay / UPI Payment Chime Listener",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to grant notification access so payment receipts speak automatically.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "ENABLE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AmberGold
                            )
                        }
                    }
                }

                // Samsung One UI Home Conversion Banner ("When i press divice home bttn now going to samsung home it convert to app.")
                if (!isDefaultHome) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TataPunchDarkJade.copy(alpha = 0.45f))
                            .border(1.dp, TataPunchJade, RoundedCornerShape(12.dp))
                            .clickable { viewModel.promptSetDefaultLauncher() }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("convert_home_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = TataPunchMint,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Convert Samsung Home Button to Kiosk",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Tap to make Kiosk Launcher the Default Home app. Pressing device Home stays in this app.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "ACTIVATE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = TataPunchMint
                            )
                        }
                    }
                }

                // 2. Tesla / Tata EV Head-Up Display (HUD) Instrument Cluster
                AutomotiveHudCluster(
                    powerState = powerState,
                    currentTheme = hudTheme,
                    onThemeChange = { viewModel.setHudTheme(it) }
                )

                // 3. Allowed Apps Whitelist Grid (Hides unwanted apps)
                AllowedAppsGrid(
                    apps = permittedApps,
                    onAppClick = { pkg -> viewModel.launchApp(pkg) },
                    onConfigureAppsClick = { viewModel.openPinDialog(PinPurpose.ADMIN_ACCESS) }
                )

                // 5. Payment Soundbox Card ("{Tone}{ payment amount} received")
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
