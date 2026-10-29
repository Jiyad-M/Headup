package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PowerState
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.TataEvBlack
import com.example.ui.theme.TataEvBorder
import com.example.ui.theme.TataEvCardDark
import com.example.ui.theme.TataEvCardMid
import com.example.ui.theme.TataEvSurface
import com.example.ui.theme.TataEvTextLight
import com.example.ui.theme.TataEvTextMuted
import com.example.ui.theme.TataPunchDarkJade
import com.example.ui.theme.TataPunchEmerald
import com.example.ui.theme.TataPunchJade
import com.example.ui.theme.TataPunchLightGreen
import com.example.ui.theme.TataPunchMint
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

enum class AutomotiveBrandTheme(val displayName: String, val primaryColor: Color, val accentColor: Color) {
    TATA_PUNCH_EV("Tata Punch EV 2026", TataPunchJade, TataPunchMint),
    TESLA("Tesla Minimalist", Color(0xFF00D2FF), Color.White),
    MONO_HUD("Night HUD Windshield", TataPunchMint, TataPunchJade)
}

@Composable
fun AutomotiveHudCluster(
    powerState: PowerState,
    currentTheme: AutomotiveBrandTheme,
    onThemeChange: (AutomotiveBrandTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialValue by remember { mutableIntStateOf(2) }
    var speed by remember { mutableIntStateOf(0) }
    var isSimulatingDrive by remember { mutableStateOf(false) }
    var isHudFullscreenOpen by remember { mutableStateOf(false) }
    var isMirrored by remember { mutableStateOf(false) }
    var isAcActive by remember { mutableStateOf(true) }
    var isAutoActive by remember { mutableStateOf(true) }

    var currentTime by remember { mutableStateOf("09:20") }
    var currentDate by remember { mutableStateOf("Tue, 24 Feb") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    LaunchedEffect(isSimulatingDrive) {
        if (isSimulatingDrive) {
            val targetSpeeds = listOf(15, 35, 50, 72, 85, 40, 20, 0)
            for (target in targetSpeeds) {
                while (speed != target) {
                    speed += if (speed < target) 2 else -2
                    dialValue = (speed / 15).coerceIn(1, 6)
                    delay(70)
                }
                delay(500)
            }
            isSimulatingDrive = false
        } else {
            speed = 0
            dialValue = 2
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("automotive_hud_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = TataEvBlack),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(TataPunchJade.copy(alpha = 0.6f), TataEvBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 1. Tata Punch EV Top Infotainment Status Bar (Matching photo: 09:20 Tue 24 Feb | Fan | Vol | Bell | 9-dots | Search | Return | 24.0°C | Signal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Time & Date
                Column {
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TataEvTextMuted
                    )
                }

                // Center Icons: Climate Fan, Volume, Notification, App Grid, Search, Back
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = "$dialValue", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Icon(imageVector = Icons.Default.Air, contentDescription = null, tint = TataPunchJade, modifier = Modifier.size(14.dp))
                    }
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = TataPunchJade, modifier = Modifier.size(16.dp))
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }

                // Right: Temperature, Windshield HUD button, Battery/Power
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(imageVector = Icons.Default.Thermostat, contentDescription = null, tint = TataPunchJade, modifier = Modifier.size(14.dp))
                        Text(text = "24.0°C", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TataPunchDarkJade)
                            .clickable { isHudFullscreenOpen = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("HUD", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = TataPunchMint)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 3-Panel Signature Tata Punch EV Facelift Card Layout (As seen in 2026 Tata Punch EV interior photo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LEFT PANEL: Media / Radio Panel with 104.2 MHz and Controls
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TataEvCardMid),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TataEvBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header with radio icon
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Icon(imageVector = Icons.Default.Radio, contentDescription = null, tint = TataPunchJade, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Circular Jade Radio Badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(TataPunchJade, TataPunchDarkJade)
                                    )
                                )
                                .border(1.dp, TataPunchMint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (speed > 0) "$speed KM/H" else "104.2 MHz",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls: Previous, Star (Favorite), Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null, tint = TataEvTextMuted, modifier = Modifier.size(18.dp))
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = TataPunchMint, modifier = Modifier.size(16.dp))
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, tint = TataEvTextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // CENTER PANEL: Tata Punch EV Signature Rotary Gauge Dial (+ / - and A/C, AUTO)
                Card(
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TataEvCardMid),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TataPunchJade.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Rotary Gauge with - on left and + on right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Minus Button
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TataPunchJade,
                                modifier = Modifier
                                    .clickable { if (dialValue > 1) dialValue-- }
                                    .padding(4.dp)
                            )

                            // Center Dial Circular Canvas
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clickable {
                                        isSimulatingDrive = !isSimulatingDrive
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeW = 4.dp.toPx()
                                    // Background track
                                    drawCircle(
                                        color = TataPunchDarkJade.copy(alpha = 0.6f),
                                        radius = size.minDimension / 2 - strokeW / 2,
                                        style = Stroke(width = strokeW)
                                    )
                                    // Active Arc
                                    val sweep = (dialValue / 6f) * 360f
                                    drawArc(
                                        color = TataPunchJade,
                                        startAngle = -90f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                    )

                                    // Dial ticks
                                    val tickCount = 12
                                    for (i in 0 until tickCount) {
                                        val angle = (i * (360f / tickCount) - 90f) * (Math.PI / 180f)
                                        val innerR = size.minDimension / 2 - strokeW * 2
                                        val outerR = size.minDimension / 2 - strokeW * 1.2f
                                        val start = Offset(
                                            center.x + (innerR * cos(angle)).toFloat(),
                                            center.y + (innerR * sin(angle)).toFloat()
                                        )
                                        val end = Offset(
                                            center.x + (outerR * cos(angle)).toFloat(),
                                            center.y + (outerR * sin(angle)).toFloat()
                                        )
                                        drawLine(
                                            color = if (i <= (dialValue * 2)) TataPunchMint else TataPunchDarkJade,
                                            start = start,
                                            end = end,
                                            strokeWidth = 1.5.dp.toPx()
                                        )
                                    }
                                }

                                Text(
                                    text = if (speed > 0) "$speed" else "$dialValue",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 26.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            // Plus Button
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TataPunchJade,
                                modifier = Modifier
                                    .clickable { if (dialValue < 6) dialValue++ }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Indicators: A/C, Fan Airflow icon, AUTO (with active green underline pills)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { isAcActive = !isAcActive }
                            ) {
                                Text(
                                    text = "A/C",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = if (isAcActive) Color.White else TataEvTextMuted
                                )
                                if (isAcActive) {
                                    Box(modifier = Modifier.width(16.dp).height(2.dp).background(TataPunchJade))
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                tint = TataPunchMint,
                                modifier = Modifier.size(16.dp)
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { isAutoActive = !isAutoActive }
                            ) {
                                Text(
                                    text = "AUTO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = if (isAutoActive) Color.White else TataEvTextMuted
                                )
                                if (isAutoActive) {
                                    Box(modifier = Modifier.width(18.dp).height(2.dp).background(TataPunchJade))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Carousel Dots: ••••• with center active
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(TataEvTextMuted))
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(TataEvTextMuted))
                            Box(modifier = Modifier.width(10.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(TataPunchJade))
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(TataEvTextMuted))
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(TataEvTextMuted))
                        }
                    }
                }

                // RIGHT PANEL: Device Pair & Dual Driver Link Shortcut
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TataEvCardMid),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TataEvBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tap to Pair a Device",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Circular Chain-Link Badge (As in Tata Punch EV screen)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(TataPunchJade, TataPunchDarkJade)
                                    )
                                )
                                .border(1.dp, TataPunchMint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${powerState.batteryLevel}% • ${(powerState.batteryLevel * 4.2).toInt()} KM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = TataPunchMint
                        )
                    }
                }
            }
        }
    }

    // Fullscreen Windshield Projection HUD Dialog
    if (isHudFullscreenOpen) {
        Dialog(
            onDismissRequest = { isHudFullscreenOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("windshield_hud_screen"),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .scale(scaleX = if (isMirrored) -1f else 1f, scaleY = 1f)
                ) {
                    // Close & Mirror Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isHudFullscreenOpen = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close HUD",
                                tint = TataPunchJade
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMirrored) TataPunchJade else TataEvCardDark)
                                .border(1.dp, TataPunchJade, RoundedCornerShape(12.dp))
                                .clickable { isMirrored = !isMirrored }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Mirror",
                                    tint = if (isMirrored) TataEvBlack else TataPunchJade,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isMirrored) "MIRROR (ON GLASS)" else "STANDARD HUD",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = if (isMirrored) TataEvBlack else TataPunchJade
                                )
                            }
                        }
                    }

                    // Huge Speed Readout in Tata EV Jade Emerald
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$speed",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 120.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TataPunchJade
                        )

                        Text(
                            text = "KM / H",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "${powerState.batteryLevel}% • TATA PUNCH EV",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.5.sp
                            ),
                            color = TataPunchMint
                        )
                    }

                    Text(
                        text = if (isMirrored) "Place device on car dashboard to read off windshield glass" else "Direct Display Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
