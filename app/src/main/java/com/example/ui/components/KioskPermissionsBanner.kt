package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.CockpitTealCardBorder
import com.example.ui.theme.CockpitTealGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TataEvTextLight
import com.example.ui.theme.TataEvTextMuted

@Composable
fun KioskPermissionsBanner(
    hasLocationPermission: Boolean,
    isNotificationAccessGranted: Boolean,
    isDefaultHome: Boolean,
    hasSmsPermission: Boolean,
    isDeviceAdminActive: Boolean = true,
    isAccessibilityEnabled: Boolean = false,
    onRequestLocation: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onRequestDefaultLauncher: () -> Unit,
    onRequestSms: () -> Unit,
    onRequestDeviceAdmin: () -> Unit = {},
    onRequestAccessibility: () -> Unit = {},
    onRequestAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allGranted = hasLocationPermission && isNotificationAccessGranted && isDefaultHome && hasSmsPermission && isDeviceAdminActive && isAccessibilityEnabled
    var isExpanded by remember { mutableStateOf(!allGranted) }

    AnimatedVisibility(
        visible = !allGranted || isExpanded,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .border(
                    1.5.dp,
                    if (allGranted) EmeraldGreen.copy(alpha = 0.5f) else Color(0xFFFFB300),
                    RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF061E1A).copy(alpha = 0.96f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (allGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (allGranted) EmeraldGreen else Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (allGranted) "Real-Time Permissions Active" else "Required Permissions Setup (Real-Time)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    if (!allGranted) {
                        Button(
                            onClick = onRequestAll,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB300),
                                contentColor = CockpitBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Grant All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "This app runs real hardware GPS speedometer and real-time payment soundbox. Please allow all permissions for full functionality:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TataEvTextMuted
                )

                // Permission checklist
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PermissionStatusRow(
                        title = "1. Real GPS Location (Speedometer)",
                        subtitle = "Calculates vehicle speed in km/h from hardware GPS",
                        isGranted = hasLocationPermission,
                        icon = Icons.Default.GpsFixed,
                        onAction = onRequestLocation,
                        actionLabel = "Allow GPS"
                    )

                    PermissionStatusRow(
                        title = "2. Soundbox Notification Access",
                        subtitle = "Intercepts Google Pay, PhonePe & Paytm payment alerts",
                        isGranted = isNotificationAccessGranted,
                        icon = Icons.Default.NotificationsActive,
                        onAction = onRequestNotificationAccess,
                        actionLabel = "Enable"
                    )

                    PermissionStatusRow(
                        title = "3. Default Home Launcher",
                        subtitle = "Converts Samsung home button permanently to this app",
                        isGranted = isDefaultHome,
                        icon = Icons.Default.Apps,
                        onAction = onRequestDefaultLauncher,
                        actionLabel = "Set Default"
                    )

                    PermissionStatusRow(
                        title = "4. Bank Payment & Recovery SMS",
                        subtitle = "Detects bank credit SMS & enables 2-word password recovery",
                        isGranted = hasSmsPermission,
                        icon = Icons.Default.Sms,
                        onAction = onRequestSms,
                        actionLabel = "Allow SMS"
                    )

                    PermissionStatusRow(
                        title = "5. Hardware Screen Off (Device Admin)",
                        subtitle = "Turns display light completely off when power unplugged in any app",
                        isGranted = isDeviceAdminActive,
                        icon = Icons.Default.Security,
                        onAction = onRequestDeviceAdmin,
                        actionLabel = "Enable"
                    )

                    PermissionStatusRow(
                        title = "6. System-Wide App Lock (Split-Screen & Multi-Window)",
                        subtitle = "Locks unapproved apps with 8-digit PIN across split-screen, DeX & system intents",
                        isGranted = isAccessibilityEnabled,
                        icon = Icons.Default.Security,
                        onAction = onRequestAccessibility,
                        actionLabel = "Enable"
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    icon: ImageVector,
    onAction: () -> Unit,
    actionLabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF031411))
            .then(
                if (!isGranted) {
                    Modifier.clickable { onAction() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) EmeraldGreen else Color(0xFFFFB300),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isGranted) Color.White else TataEvTextLight
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TataEvTextMuted
                )
            }
        }

        if (isGranted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldGreen
                )
            }
        } else {
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = CockpitBlack
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
