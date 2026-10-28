package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PowerState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KioskTopBar(
    powerState: PowerState,
    isAnnouncing: Boolean,
    onLockClick: () -> Unit,
    onSoundboxClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kiosk_top_bar"),
        color = Slate900.copy(alpha = 0.95f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Clock & Date
            Column {
                Text(
                    text = currentTime.ifEmpty { "--:--" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = NeonCyan
                )
                Text(
                    text = currentDate.ifEmpty { "Kiosk Launcher" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Center: Power Indicator Pill
            PowerStatusPill(powerState = powerState)

            // Right: Soundbox Quick Button + Admin Lock Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Soundbox Status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAnnouncing) EmeraldGreen.copy(alpha = 0.2f) else Slate800)
                        .border(
                            1.dp,
                            if (isAnnouncing) EmeraldGreen else Slate700,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSoundboxClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("soundbox_header_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAnnouncing) Icons.Default.RecordVoiceOver else Icons.Default.VolumeUp,
                            contentDescription = "Soundbox",
                            tint = if (isAnnouncing) EmeraldGreen else NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isAnnouncing) "Chime..." else "Soundbox",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isAnnouncing) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Kiosk Admin Lock
                IconButton(
                    onClick = onLockClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                        .border(1.dp, Slate700, CircleShape)
                        .testTag("admin_lock_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Unlock / Settings",
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PowerStatusPill(
    powerState: PowerState,
    modifier: Modifier = Modifier
) {
    val pillBgColor by animateColorAsState(
        targetValue = if (powerState.isPlugged) EmeraldGreen.copy(alpha = 0.18f) else Slate800,
        label = "pill_bg"
    )
    val pillBorderColor by animateColorAsState(
        targetValue = if (powerState.isPlugged) EmeraldGreen.copy(alpha = 0.8f) else Slate700,
        label = "pill_border"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(pillBgColor)
            .border(1.dp, pillBorderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("power_status_pill"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (powerState.isPlugged) Icons.Default.Power else if (powerState.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
            contentDescription = "Power Status",
            tint = if (powerState.isPlugged) EmeraldGreen else AmberGold,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (powerState.isPlugged) "PLUGGED • SCREEN ON" else "${powerState.batteryLevel}% • AUTO-SLEEP",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            ),
            color = if (powerState.isPlugged) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
