package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppItem
import com.example.model.PaymentRecord
import com.example.model.SoundboxTone
import com.example.model.SpeechEngine
import com.example.model.VoiceGender
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.HudBronze
import com.example.ui.theme.HudCyan
import com.example.ui.theme.HudMint
import com.example.ui.theme.HudOrange
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.TeslaRed

@Composable
fun AdminSettingsSheet(
    allApps: List<AppItem>,
    currentSecondaryPhone: String,
    autoDisplayOnPlugged: Boolean,
    sleepOnUnplugged: Boolean,
    currency: String,
    paymentHistory: List<PaymentRecord>,
    currentTheme: AutomotiveBrandTheme,
    selectedVoiceGender: VoiceGender = VoiceGender.MALE,
    selectedTone: SoundboxTone = SoundboxTone.PAYTM_TRIAD,
    selectedSpeechEngine: SpeechEngine = SpeechEngine.NATURAL_CLOUD,
    isDefaultHome: Boolean = false,
    onThemeChange: (AutomotiveBrandTheme) -> Unit,
    onToggleAppAllowed: (String, Boolean) -> Unit,
    onToggleAppBlacklisted: (String, Boolean) -> Unit = { _, _ -> },
    onUpdatePin: (String) -> Boolean,
    onUpdateSecondaryPhone: (String) -> Unit,
    onUpdatePowerSettings: (Boolean, Boolean) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onVoiceGenderChange: (VoiceGender) -> Unit = {},
    onSoundboxToneChange: (SoundboxTone) -> Unit = {},
    onSpeechEngineChange: (SpeechEngine) -> Unit = {},
    onPreviewTone: (SoundboxTone) -> Unit = {},
    onPreviewVoice: (VoiceGender) -> Unit = {},
    onPromptSetDefaultLauncher: () -> Unit = {},
    onTestAnnouncement: (Double) -> Unit,
    onRequestExit: () -> Unit,
    onRequestUninstall: () -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Allowed Apps", "HUD & Cockpit", "Power & Screen", "Security & Exit", "Soundbox")

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("admin_settings_screen"),
            color = CockpitBlack
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CockpitSurface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(currentTheme.primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Kiosk Cockpit Controls",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Admin Authenticated Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = currentTheme.primaryColor
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CockpitSurface,
                    contentColor = currentTheme.primaryColor,
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(color = CockpitBorder)

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> AllowedAppsTab(
                            allApps = allApps,
                            onToggleAppAllowed = onToggleAppAllowed,
                            onToggleAppBlacklisted = onToggleAppBlacklisted
                        )
                        1 -> HudThemeTab(currentTheme = currentTheme, onThemeChange = onThemeChange)
                        2 -> PowerSettingsTab(
                            autoDisplayOnPlugged = autoDisplayOnPlugged,
                            sleepOnUnplugged = sleepOnUnplugged,
                            onUpdatePowerSettings = onUpdatePowerSettings
                        )
                        3 -> SecurityAndExitTab(
                            currentSecondaryPhone = currentSecondaryPhone,
                            isDefaultHome = isDefaultHome,
                            onPromptSetDefaultLauncher = onPromptSetDefaultLauncher,
                            onUpdatePin = onUpdatePin,
                            onUpdateSecondaryPhone = onUpdateSecondaryPhone,
                            onRequestExit = onRequestExit,
                            onRequestUninstall = onRequestUninstall
                        )
                        4 -> SoundboxSettingsTab(
                            currency = currency,
                            selectedVoiceGender = selectedVoiceGender,
                            selectedTone = selectedTone,
                            selectedSpeechEngine = selectedSpeechEngine,
                            paymentHistory = paymentHistory,
                            onUpdateCurrency = onUpdateCurrency,
                            onVoiceGenderChange = onVoiceGenderChange,
                            onSoundboxToneChange = onSoundboxToneChange,
                            onSpeechEngineChange = onSpeechEngineChange,
                            onPreviewTone = onPreviewTone,
                            onPreviewVoice = onPreviewVoice,
                            onTestAnnouncement = onTestAnnouncement
                        )
                    }
                }

                // Bottom Action Bar
                Surface(
                    color = CockpitSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onRequestExit,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exit Kiosk", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onClose,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primaryColor, contentColor = CockpitBlack)
                        ) {
                            Text("Save & Return", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HudThemeTab(
    currentTheme: AutomotiveBrandTheme,
    onThemeChange: (AutomotiveBrandTheme) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Automotive Head-Up Display (HUD) Themes",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Switch between Tesla minimalist cockpit, Tata Curvv EV instrument cluster, or high-contrast night windshield projection mode.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AutomotiveBrandTheme.values().forEach { theme ->
            val isSelected = theme == currentTheme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeChange(theme) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) theme.primaryColor else CockpitBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                            )
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (theme) {
                                AutomotiveBrandTheme.TATA_PUNCH_EV -> "2026 Tata Punch EV / Nexon EV Facelift jade infotainment cluster with central rotary dial gauge."
                                AutomotiveBrandTheme.TESLA -> "Cyber-slate minimalist theme with electric cyan telemetry and autopilot status."
                                AutomotiveBrandTheme.MONO_HUD -> "High-contrast monochrome green projection optimized for car windshield reflection."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = theme.primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllowedAppsTab(
    allApps: List<AppItem>,
    onToggleAppAllowed: (String, Boolean) -> Unit,
    onToggleAppBlacklisted: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "App Access & Blacklist Policy",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "When Kiosk is activated, non-whitelisted apps remain hidden and blacklisted apps are strictly denied from opening anywhere.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("all_apps_whitelist_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allApps, key = { it.packageName }) { app ->
                AppWhitelistRow(
                    app = app,
                    onToggleAllowed = { isAllowed -> onToggleAppAllowed(app.packageName, isAllowed) },
                    onToggleBlacklist = { isBlacklisted -> onToggleAppBlacklisted(app.packageName, isBlacklisted) }
                )
            }
        }
    }
}

@Composable
fun AppWhitelistRow(
    app: AppItem,
    onToggleAllowed: (Boolean) -> Unit,
    onToggleBlacklist: (Boolean) -> Unit
) {
    val bitmap = rememberDrawableBitmap(app.icon)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                app.isBlacklisted -> CrimsonRed.copy(alpha = 0.8f)
                app.isAllowed -> HudCyan.copy(alpha = 0.5f)
                else -> CockpitBorder
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CockpitSurface),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = HudCyan)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Status Badge
                        if (app.isBlacklisted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CrimsonRed.copy(alpha = 0.2f))
                                    .border(1.dp, CrimsonRed, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "BLOCKED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = CrimsonRed
                                    )
                                }
                            }
                        } else if (app.isAllowed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HudCyan.copy(alpha = 0.2f))
                                    .border(1.dp, HudCyan, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ALLOWED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = HudCyan
                                )
                            }
                        }
                    }

                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Blacklist toggle chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (app.isBlacklisted) CrimsonRed else CockpitSurface)
                        .border(1.dp, if (app.isBlacklisted) CrimsonRed else CockpitBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleBlacklist(!app.isBlacklisted) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("blacklist_btn_${app.packageName}")
                ) {
                    Text(
                        text = if (app.isBlacklisted) "DENIED" else "BLOCK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = if (app.isBlacklisted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Whitelist toggle switch (disabled if explicitly blacklisted)
                Switch(
                    checked = app.isAllowed,
                    onCheckedChange = onToggleAllowed,
                    enabled = !app.isBlacklisted,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CockpitBlack,
                        checkedTrackColor = HudCyan,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = CockpitSurface
                    ),
                    modifier = Modifier.testTag("switch_${app.packageName}")
                )
            }
        }
    }
}

@Composable
fun PowerSettingsTab(
    autoDisplayOnPlugged: Boolean,
    sleepOnUnplugged: Boolean,
    onUpdatePowerSettings: (Boolean, Boolean) -> Unit
) {
    var displayOn by remember { mutableStateOf(autoDisplayOnPlugged) }
    var sleepOn by remember { mutableStateOf(sleepOnUnplugged) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Power & Screen Behavior",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Automate display power states based on charging cable connection.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Power Plugged -> Display ON",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Keep screen permanently on when connected to AC or USB charger.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = displayOn,
                        onCheckedChange = {
                            displayOn = it
                            onUpdatePowerSettings(displayOn, sleepOn)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = HudMint)
                    )
                }

                HorizontalDivider(color = CockpitBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Power Goes -> Display Sleep",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Instantly sleep display when power cable is unplugged to preserve battery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = sleepOn,
                        onCheckedChange = {
                            sleepOn = it
                            onUpdatePowerSettings(displayOn, sleepOn)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = AmberGold)
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityAndExitTab(
    currentSecondaryPhone: String,
    isDefaultHome: Boolean = false,
    onPromptSetDefaultLauncher: () -> Unit = {},
    onUpdatePin: (String) -> Boolean,
    onUpdateSecondaryPhone: (String) -> Unit,
    onRequestExit: () -> Unit,
    onRequestUninstall: () -> Unit
) {
    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }
    var phoneText by remember { mutableStateOf(currentSecondaryPhone) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Kiosk Security & Launcher Control",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // Default Home Launcher Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDefaultHome) EmeraldGreen.copy(alpha = 0.6f) else AmberGold
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = if (isDefaultHome) EmeraldGreen else AmberGold
                        )
                        Text(
                            text = "Default Home Launcher",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = if (isDefaultHome) {
                            "✅ Kiosk is configured as the active default Home Launcher. Samsung One UI home button is permanently locked to this app."
                        } else {
                            "⚠️ Kiosk is NOT yet set as the default launcher! When you press the device home button, it exits to Samsung Home. Tap below to set Kiosk as Default Home."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDefaultHome) EmeraldGreen else AmberGold
                    )

                    Button(
                        onClick = onPromptSetDefaultLauncher,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDefaultHome) CockpitSurface else AmberGold,
                            contentColor = if (isDefaultHome) HudCyan else CockpitBlack
                        )
                    ) {
                        Text(
                            text = if (isDefaultHome) "Re-verify Default Home Role" else "Set as Default Home Launcher Now",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Change 8-Digit PIN Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Change 8-Digit Password",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = newPinText,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) newPinText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New 8-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = HudCyan,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    OutlinedTextField(
                        value = confirmPinText,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) confirmPinText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm 8-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = HudCyan,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    if (pinMessage != null) {
                        Text(
                            text = pinMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (pinMessage?.contains("success", ignoreCase = true) == true) HudMint else CrimsonRed
                        )
                    }

                    Button(
                        onClick = {
                            if (newPinText.length != 8) {
                                pinMessage = "PIN must be exactly 8 digits"
                            } else if (newPinText != confirmPinText) {
                                pinMessage = "PIN confirmation does not match"
                            } else {
                                val ok = onUpdatePin(newPinText)
                                pinMessage = if (ok) "8-Digit PIN successfully updated!" else "Error updating PIN"
                                if (ok) {
                                    newPinText = ""
                                    confirmPinText = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HudCyan, contentColor = CockpitBlack)
                    ) {
                        Text("Update 8-Digit PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Secondary Phone Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Secondary Phone (Emergency OTP)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Text(
                        text = "If you forget the 8-digit password, the locked kiosk generates random challenge words. The emergency OTP is dispatched to this secondary phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Secondary Phone Number") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = HudCyan,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    Button(
                        onClick = { onUpdateSecondaryPhone(phoneText) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = CockpitBlack)
                    ) {
                        Text("Save Secondary Phone", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Password Protected Exit & Uninstall Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TeslaRed.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TeslaRed)
                        Text(
                            text = "Kiosk Exit & App Uninstall (Password Protected)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "To exit kiosk mode or uninstall this app from the device, the 8-digit password is required to prevent unauthorized tampering.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRequestExit,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exit Kiosk", color = AmberGold, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onRequestUninstall,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TeslaRed, contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uninstall App", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundboxSettingsTab(
    currency: String,
    selectedVoiceGender: VoiceGender,
    selectedTone: SoundboxTone,
    selectedSpeechEngine: SpeechEngine = SpeechEngine.NATURAL_CLOUD,
    paymentHistory: List<PaymentRecord>,
    onUpdateCurrency: (String) -> Unit,
    onVoiceGenderChange: (VoiceGender) -> Unit,
    onSoundboxToneChange: (SoundboxTone) -> Unit,
    onSpeechEngineChange: (SpeechEngine) -> Unit = {},
    onPreviewTone: (SoundboxTone) -> Unit,
    onPreviewVoice: (VoiceGender) -> Unit,
    onTestAnnouncement: (Double) -> Unit
) {
    val currencies = listOf("$", "₹", "€", "£", "¥", "₩")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Payment Soundbox Voice & Chime Settings",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // 0. Speech Engine Selection Card (Google Natural Cloud vs Local)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, HudCyan.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = HudCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Speech Engine (Voice Quality)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Switch between high-definition studio speech API and local device synthesizers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpeechEngine.entries.forEach { engine ->
                            val isSelected = engine == selectedSpeechEngine
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) HudCyan.copy(alpha = 0.15f) else CockpitSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) HudCyan else CockpitBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSpeechEngineChange(engine) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) HudCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column {
                                    Text(
                                        text = engine.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) HudCyan else Color.White
                                    )
                                    Text(
                                        text = engine.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Voice Gender Selection (Male / Female)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = HudMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Soundbox Voice Gender",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Select male or female announcement voice for realistic payment notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VoiceGender.entries.forEach { gender ->
                            val isSelected = gender == selectedVoiceGender
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onVoiceGenderChange(gender) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) HudMint.copy(alpha = 0.15f) else CockpitSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) HudMint else CockpitBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (gender == VoiceGender.MALE) "👨 Male" else "👩 Female",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) HudMint else Color.White
                                        )
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isSelected) HudMint else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = gender.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        minLines = 2,
                                        maxLines = 2
                                    )

                                    OutlinedButton(
                                        onClick = {
                                            onVoiceGenderChange(gender)
                                            onPreviewVoice(gender)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) HudMint else CockpitBorder
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isSelected) HudMint else Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Test Voice",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Chime Tone Options Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = HudCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Soundbox Alert Chime Tone",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Choose the bell melody played immediately prior to the voice announcement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SoundboxTone.entries.forEach { tone ->
                            val isSelected = tone == selectedTone
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) HudCyan.copy(alpha = 0.15f) else CockpitSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) HudCyan else CockpitBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSoundboxToneChange(tone)
                                        onPreviewTone(tone)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) HudCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Column {
                                        Text(
                                            text = tone.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) HudCyan else Color.White
                                        )
                                        Text(
                                            text = tone.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onPreviewTone(tone) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Preview ${tone.displayName}",
                                        tint = if (isSelected) HudCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Currency Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Active Currency Symbol",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { curr ->
                            val isSelected = curr == currency
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) HudMint else CockpitSurface)
                                    .border(1.dp, if (isSelected) HudMint else CockpitBorder, RoundedCornerShape(10.dp))
                                    .clickable { onUpdateCurrency(curr) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = curr,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) CockpitBlack else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Test Complete Soundbox Alert Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, HudMint.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Test Complete Soundbox Alert",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Current: ${selectedTone.displayName} • ${selectedVoiceGender.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HudMint
                    )

                    Button(
                        onClick = { onTestAnnouncement(150.0) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HudMint, contentColor = CockpitBlack)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Play Alert: [Chime] 150 ${if (currency == "₹") "rupees" else "received"} on Paytm / UPI",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Recent Payments History
        item {
            Text(
                text = "Payment Announcement History (${paymentHistory.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        if (paymentHistory.isEmpty()) {
            item {
                Text(
                    text = "No payments announced yet. Soundbox will announce whenever payment notifications or SMS arrive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(paymentHistory, key = { it.id }) { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CockpitCard)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.announcementText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = HudMint
                        )
                        Text(
                            text = record.formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { onTestAnnouncement(record.amount) }) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Replay",
                            tint = HudMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
