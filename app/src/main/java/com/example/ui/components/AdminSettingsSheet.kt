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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
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
    onThemeChange: (AutomotiveBrandTheme) -> Unit,
    onToggleAppAllowed: (String, Boolean) -> Unit,
    onUpdatePin: (String) -> Boolean,
    onUpdateSecondaryPhone: (String) -> Unit,
    onUpdatePowerSettings: (Boolean, Boolean) -> Unit,
    onUpdateCurrency: (String) -> Unit,
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
                        0 -> AllowedAppsTab(allApps = allApps, onToggleAppAllowed = onToggleAppAllowed)
                        1 -> HudThemeTab(currentTheme = currentTheme, onThemeChange = onThemeChange)
                        2 -> PowerSettingsTab(
                            autoDisplayOnPlugged = autoDisplayOnPlugged,
                            sleepOnUnplugged = sleepOnUnplugged,
                            onUpdatePowerSettings = onUpdatePowerSettings
                        )
                        3 -> SecurityAndExitTab(
                            currentSecondaryPhone = currentSecondaryPhone,
                            onUpdatePin = onUpdatePin,
                            onUpdateSecondaryPhone = onUpdateSecondaryPhone,
                            onRequestExit = onRequestExit,
                            onRequestUninstall = onRequestUninstall
                        )
                        4 -> SoundboxSettingsTab(
                            currency = currency,
                            paymentHistory = paymentHistory,
                            onUpdateCurrency = onUpdateCurrency,
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
                                AutomotiveBrandTheme.TESLA -> "Cyber-slate minimalist theme with electric cyan telemetry and autopilot status."
                                AutomotiveBrandTheme.TATA_EV -> "Modern Indian EV sports theme with Curvv/Nexon EV orange power and teal regen."
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
    onToggleAppAllowed: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "App Whitelist Configuration",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Only apps enabled below will appear on the kiosk home screen. All unwanted or unapproved apps remain hidden.",
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
                    onToggle = { isAllowed -> onToggleAppAllowed(app.packageName, isAllowed) }
                )
            }
        }
    }
}

@Composable
fun AppWhitelistRow(
    app: AppItem,
    onToggle: (Boolean) -> Unit
) {
    val bitmap = rememberDrawableBitmap(app.icon)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isAllowed) HudCyan.copy(alpha = 0.5f) else CockpitBorder
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

                Column {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = app.isAllowed,
                onCheckedChange = onToggle,
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
                text = "Kiosk Security & Passwords",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
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
    paymentHistory: List<PaymentRecord>,
    onUpdateCurrency: (String) -> Unit,
    onTestAnnouncement: (Double) -> Unit
) {
    val currencies = listOf("$", "₹", "€", "£", "¥", "₩")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Payment Soundbox & Voice Chime",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        // Currency Selector
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

        // Test Voice Announcement
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, HudMint.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Test Voice Announcement",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Formula: {Tone} {payment amount} received",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { onTestAnnouncement(25.0) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HudMint, contentColor = CockpitBlack)
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Test: \"[Tone] 25 ${if (currency == "$") "dollars" else "units"} received\"", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Recent Payments Log
        Text(
            text = "Payment Announcement History (${paymentHistory.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (paymentHistory.isEmpty()) {
                item {
                    Text(
                        text = "No payments announced yet.",
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
                        Column {
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
}
