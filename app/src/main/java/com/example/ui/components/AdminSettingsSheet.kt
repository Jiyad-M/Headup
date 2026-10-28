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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
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
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun AdminSettingsSheet(
    allApps: List<AppItem>,
    currentSecondaryPhone: String,
    autoDisplayOnPlugged: Boolean,
    sleepOnUnplugged: Boolean,
    currency: String,
    paymentHistory: List<PaymentRecord>,
    onToggleAppAllowed: (String, Boolean) -> Unit,
    onUpdatePin: (String) -> Boolean,
    onUpdateSecondaryPhone: (String) -> Unit,
    onUpdatePowerSettings: (Boolean, Boolean) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onTestAnnouncement: (Double) -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Allowed Apps", "Power & Screen", "Security & PIN", "Soundbox")
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("admin_settings_screen"),
            color = Slate950
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
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
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Admin Kiosk Controls",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Authenticated Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGreen
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
                    containerColor = Slate900,
                    contentColor = NeonCyan,
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

                HorizontalDivider(color = Slate800)

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> AllowedAppsTab(allApps = allApps, onToggleAppAllowed = onToggleAppAllowed)
                        1 -> PowerSettingsTab(
                            autoDisplayOnPlugged = autoDisplayOnPlugged,
                            sleepOnUnplugged = sleepOnUnplugged,
                            onUpdatePowerSettings = onUpdatePowerSettings
                        )
                        2 -> SecuritySettingsTab(
                            currentSecondaryPhone = currentSecondaryPhone,
                            onUpdatePin = onUpdatePin,
                            onUpdateSecondaryPhone = onUpdateSecondaryPhone
                        )
                        3 -> SoundboxSettingsTab(
                            currency = currency,
                            paymentHistory = paymentHistory,
                            onUpdateCurrency = onUpdateCurrency,
                            onTestAnnouncement = onTestAnnouncement
                        )
                    }
                }

                // Bottom Exit Kiosk Bar
                Surface(
                    color = Slate900,
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
                            onClick = {
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(Intent.createChooser(intent, "Select Home Launcher"))
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exit Kiosk Launcher", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onClose,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Slate900)
                        ) {
                            Text("Done / Save", fontWeight = FontWeight.Bold)
                        }
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
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isAllowed) NeonCyan.copy(alpha = 0.5f) else Slate800
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
                        .background(Slate800),
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
                        Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = NeonCyan)
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
                    checkedThumbColor = Slate900,
                    checkedTrackColor = NeonCyan,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = Slate800
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
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
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
                        colors = SwitchDefaults.colors(checkedTrackColor = EmeraldGreen)
                    )
                }

                HorizontalDivider(color = Slate800)

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
fun SecuritySettingsTab(
    currentSecondaryPhone: String,
    onUpdatePin: (String) -> Boolean,
    onUpdateSecondaryPhone: (String) -> Unit
) {
    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<String?>(null) }
    var phoneText by remember { mutableStateOf(currentSecondaryPhone) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kiosk Security & 8-Digit Password",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Change 8-Digit PIN",
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
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Slate700
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
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Slate700
                    )
                )

                if (pinMessage != null) {
                    Text(
                        text = pinMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (pinMessage?.contains("success", ignoreCase = true) == true) EmeraldGreen else CrimsonRed
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
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Slate900)
                ) {
                    Text("Update 8-Digit PIN", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Secondary Phone (Emergency OTP Recovery)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "If you forget the 8-digit password, the kiosk locks and generates random challenge words. The emergency OTP is dispatched to this secondary phone.",
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
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Slate700
                    )
                )

                Button(
                    onClick = { onUpdateSecondaryPhone(phoneText) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = Slate900)
                ) {
                    Text("Save Secondary Phone", fontWeight = FontWeight.Bold)
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
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
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
                                .background(if (isSelected) EmeraldGreen else Slate800)
                                .border(1.dp, if (isSelected) EmeraldGreen else Slate700, RoundedCornerShape(10.dp))
                                .clickable { onUpdateCurrency(curr) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = curr,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Slate900 else Color.White
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
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
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
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Slate900)
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
                            .background(Slate900)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = record.announcementText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = EmeraldGreen
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
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
