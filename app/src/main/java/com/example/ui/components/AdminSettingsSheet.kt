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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
    isLightMode: Boolean = false,
    isDeviceAdminActive: Boolean = true,
    canWriteSettings: Boolean = true,
    isAccessibilityEnabled: Boolean = false,
    onToggleLightMode: (Boolean) -> Unit = {},
    onRequestDeviceAdmin: () -> Unit = {},
    onRequestWriteSettings: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {},
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
                        1 -> HudThemeTab(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange,
                            isLightMode = isLightMode,
                            onToggleLightMode = onToggleLightMode
                        )
                        2 -> PowerSettingsTab(
                            autoDisplayOnPlugged = autoDisplayOnPlugged,
                            sleepOnUnplugged = sleepOnUnplugged,
                            isDeviceAdminActive = isDeviceAdminActive,
                            canWriteSettings = canWriteSettings,
                            onRequestDeviceAdmin = onRequestDeviceAdmin,
                            onRequestWriteSettings = onRequestWriteSettings,
                            onUpdatePowerSettings = onUpdatePowerSettings
                        )
                        3 -> SecurityAndExitTab(
                            currentSecondaryPhone = currentSecondaryPhone,
                            isDefaultHome = isDefaultHome,
                            isAccessibilityEnabled = isAccessibilityEnabled,
                            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
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
    onThemeChange: (AutomotiveBrandTheme) -> Unit,
    isLightMode: Boolean = false,
    onToggleLightMode: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. APPEARANCE MODE (Dark Cockpit / Daylight Mode)
        Text(
            text = "Cockpit Appearance Mode",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Switch between Deep Teal Night Cockpit and Crisp Daylight Silver/Jade Mode.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dark Mode Option Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleLightMode(false) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isLightMode) Color(0xFF0F473E) else CockpitCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (!isLightMode) 2.dp else 1.dp,
                    if (!isLightMode) HudMint else CockpitBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (!isLightMode) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (!isLightMode) HudMint else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Deep luminous glowing teal cluster, optimized for night driving and OLED screens.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isLightMode) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = HudMint
                        )
                    }
                }
            }

            // Light Mode Option Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleLightMode(true) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLightMode) Color(0xFFE2EFEA) else CockpitCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isLightMode) 2.dp else 1.dp,
                    if (isLightMode) Color(0xFF00796B) else CockpitBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Light Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isLightMode) Color(0xFF0A2922) else Color.White
                        )
                        Icon(
                            imageVector = if (isLightMode) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isLightMode) Color(0xFF00796B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Crisp daylight silver & jade cockpit, ultra-clear visibility under bright sunlight.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (isLightMode) Color(0xFF32544C) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isLightMode) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF00796B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Automotive Instrument Cluster Brands",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Allowed, 2 = Not Allowed (Protected)

    val allowedCount = remember(allApps) { allApps.count { it.isAllowed } }
    val blockedCount = remember(allApps) { allApps.count { !it.isAllowed } }

    val filteredApps = remember(allApps, searchQuery, selectedFilter) {
        allApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.name.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                1 -> app.isAllowed
                2 -> !app.isAllowed
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "App Access & Password Protection",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Toggle ON for free launch. If toggle is OFF, the app is automatically blocked and strictly password-protected system-wide across full screen, split-screen, Samsung DeX, multitasking, and links from other apps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Search installed apps...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = HudCyan, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CockpitSurface,
                unfocusedContainerColor = CockpitSurface,
                focusedBorderColor = HudCyan,
                unfocusedBorderColor = CockpitBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == 0,
                onClick = { selectedFilter = 0 },
                label = { Text("All (${allApps.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HudCyan,
                    selectedLabelColor = CockpitBlack,
                    containerColor = CockpitCard,
                    labelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedFilter == 1,
                onClick = { selectedFilter = 1 },
                label = { Text("Allowed ($allowedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldGreen,
                    selectedLabelColor = CockpitBlack,
                    containerColor = CockpitCard,
                    labelColor = EmeraldGreen
                )
            )

            FilterChip(
                selected = selectedFilter == 2,
                onClick = { selectedFilter = 2 },
                label = { Text("Not Allowed / Locked ($blockedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberGold,
                    selectedLabelColor = CockpitBlack,
                    containerColor = CockpitCard,
                    labelColor = AmberGold
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("all_apps_whitelist_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppWhitelistRow(
                    app = app,
                    onToggleAllowed = { isAllowed -> onToggleAppAllowed(app.packageName, isAllowed) }
                )
            }
        }
    }
}

@Composable
fun AppWhitelistRow(
    app: AppItem,
    onToggleAllowed: (Boolean) -> Unit
) {
    val bitmap = rememberDrawableBitmap(app.icon)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isAllowed) EmeraldGreen.copy(alpha = 0.5f) else AmberGold.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
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

                    if (!app.isAllowed) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(AmberGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(9.dp))
                        }
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
                        if (app.isAllowed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.2f))
                                    .border(1.dp, EmeraldGreen.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(10.dp))
                                    Text(
                                        text = "ALLOWED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberGold.copy(alpha = 0.2f))
                                    .border(1.dp, AmberGold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold, modifier = Modifier.size(10.dp))
                                    Text(
                                        text = "PASSWORD PROTECTED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = AmberGold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = if (app.isAllowed) "Allowed to open directly without password" else "Requires 8-digit password to open",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (app.isAllowed) MaterialTheme.colorScheme.onSurfaceVariant else AmberGold.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Allowed Toggle Switch (Toggle OFF = Blocked & Password Protected)
            Switch(
                checked = app.isAllowed,
                onCheckedChange = onToggleAllowed,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CockpitBlack,
                    checkedTrackColor = EmeraldGreen,
                    uncheckedThumbColor = AmberGold,
                    uncheckedTrackColor = CockpitSurface
                ),
                modifier = Modifier.testTag("switch_${app.packageName}")
            )
        }
    }
}

@Composable
fun PowerSettingsTab(
    autoDisplayOnPlugged: Boolean,
    sleepOnUnplugged: Boolean,
    isDeviceAdminActive: Boolean = true,
    canWriteSettings: Boolean = true,
    onRequestDeviceAdmin: () -> Unit = {},
    onRequestWriteSettings: () -> Unit = {},
    onUpdatePowerSettings: (Boolean, Boolean) -> Unit
) {
    var displayOn by remember { mutableStateOf(autoDisplayOnPlugged) }
    var sleepOn by remember { mutableStateOf(sleepOnUnplugged) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hardware Power & Display Behavior",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Controls physical hardware display power when charging cable is connected or disconnected. Default is OFF.",
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
                // Plugged in
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Plugged In -> Display Completely ON",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Instantly wakes screen when charger plugged. Zero display sleep (keeps screen on permanently).",
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

                // Unplugged
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unplugged -> Complete Hardware Display OFF",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Within 3 seconds of unplugging, turns display and lights completely OFF in ANY app (Uber, Maps, etc.).",
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

        // Hardware Permissions Section
        Text(
            text = "Required Permissions for Hardware Display Control",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // 1. Device Admin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Device Administrator",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            if (isDeviceAdminActive) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                                    color = EmeraldGreen
                                )
                            }
                        }
                        Text(
                            text = "Allows Kiosk to turn off physical screen lights system-wide when power is cut (lockNow).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isDeviceAdminActive) {
                        Button(
                            onClick = onRequestDeviceAdmin,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black)
                        ) {
                            Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = CockpitBorder)

                // 2. Write System Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Modify System Settings",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            if (canWriteSettings) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                                    color = EmeraldGreen
                                )
                            }
                        }
                        Text(
                            text = "Sets Android display timeout to 24 hours while plugged so screen never sleeps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!canWriteSettings) {
                        Button(
                            onClick = onRequestWriteSettings,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Color.Black)
                        ) {
                            Text("Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityAndExitTab(
    currentSecondaryPhone: String,
    isDefaultHome: Boolean = false,
    isAccessibilityEnabled: Boolean = false,
    onOpenAccessibilitySettings: () -> Unit = {},
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

        // System-Wide App Lock (Split-Screen & Multi-Window Protection) Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAccessibilityEnabled) EmeraldGreen.copy(alpha = 0.6f) else AmberGold
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isAccessibilityEnabled) EmeraldGreen else AmberGold
                        )
                        Text(
                            text = "System-Wide App Lock (Split-Screen Protection)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Text(
                        text = if (isAccessibilityEnabled) {
                            "✅ Real-Time App Lock Service is ACTIVE. Blocked apps cannot be opened anywhere (including split-screen, Samsung DeX, pop-up view, or intent links from other apps) without the 8-digit password."
                        } else {
                            "⚠️ System-Wide App Lock requires Accessibility permission to intercept blocked apps when launched in split-screen or from other apps. Tap below to enable Kiosk App Lock Service in Android Settings."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAccessibilityEnabled) EmeraldGreen else AmberGold
                    )

                    Button(
                        onClick = onOpenAccessibilitySettings,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccessibilityEnabled) CockpitSurface else AmberGold,
                            contentColor = if (isAccessibilityEnabled) HudCyan else CockpitBlack
                        )
                    ) {
                        Text(
                            text = if (isAccessibilityEnabled) "Accessibility Service Active (Tap to Configure)" else "Enable System-Wide App Lock in Settings",
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
