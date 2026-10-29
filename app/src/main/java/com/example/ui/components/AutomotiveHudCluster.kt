package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PowerState
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.HudBronze
import com.example.ui.theme.HudCyan
import com.example.ui.theme.HudMint
import com.example.ui.theme.HudOrange
import com.example.ui.theme.TeslaRed
import kotlinx.coroutines.delay

enum class AutomotiveBrandTheme(val displayName: String, val primaryColor: Color, val accentColor: Color) {
    TESLA("Tesla Minimalist", HudCyan, Color.White),
    TATA_EV("Tata Curvv EV", HudOrange, HudMint),
    MONO_HUD("Night HUD Windshield", HudMint, HudMint)
}

@Composable
fun AutomotiveHudCluster(
    powerState: PowerState,
    currentTheme: AutomotiveBrandTheme,
    onThemeChange: (AutomotiveBrandTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var speed by remember { mutableIntStateOf(0) }
    var gear by remember { mutableStateOf("D") }
    var isSimulatingDrive by remember { mutableStateOf(false) }
    var isHudFullscreenOpen by remember { mutableStateOf(false) }
    var isMirrored by remember { mutableStateOf(false) }

    // Drive simulation effect for testing automotive HUD
    LaunchedEffect(isSimulatingDrive) {
        if (isSimulatingDrive) {
            val targetSpeeds = listOf(15, 34, 52, 68, 80, 75, 45, 20, 0)
            for (target in targetSpeeds) {
                while (speed != target) {
                    speed += if (speed < target) 2 else -2
                    delay(80)
                }
                delay(600)
            }
            gear = "P"
            isSimulatingDrive = false
        } else {
            speed = 0
            gear = if (powerState.isPlugged) "P" else "D"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("automotive_hud_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(currentTheme.primaryColor.copy(alpha = 0.7f), currentTheme.accentColor.copy(alpha = 0.4f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Brand & Theme Pills + HUD Mode Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Pill Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AutomotiveBrandTheme.values().forEach { theme ->
                        val isSelected = theme == currentTheme
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) theme.primaryColor.copy(alpha = 0.2f) else CockpitSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) theme.primaryColor else CockpitBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onThemeChange(theme) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = when (theme) {
                                    AutomotiveBrandTheme.TESLA -> "TESLA HUD"
                                    AutomotiveBrandTheme.TATA_EV -> "TATA EV"
                                    AutomotiveBrandTheme.MONO_HUD -> "NIGHT HUD"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (isSelected) theme.primaryColor else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Fullscreen HUD Windshield Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentTheme.primaryColor.copy(alpha = 0.15f))
                        .border(1.dp, currentTheme.primaryColor, RoundedCornerShape(12.dp))
                        .clickable { isHudFullscreenOpen = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("open_hud_mode_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "HUD",
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "WINDSHIELD HUD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = currentTheme.primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Cluster Display Area: PRND, Speedometer, and EV Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: PRND Selector & Drive Mode
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (currentTheme == AutomotiveBrandTheme.TATA_EV) "DRIVE MODE: SPORT" else "AUTOPILOT: READY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = currentTheme.primaryColor
                    )

                    // PRND Selector Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("P", "R", "N", "D").forEach { g ->
                            val isActive = g == gear
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isActive) currentTheme.primaryColor else CockpitSurface
                                    )
                                    .border(
                                        1.dp,
                                        if (isActive) currentTheme.primaryColor else CockpitBorder,
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isActive) CockpitBlack else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Test Drive Simulator button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CockpitSurface)
                            .border(1.dp, CockpitBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                isSimulatingDrive = !isSimulatingDrive
                                if (isSimulatingDrive) gear = "D"
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSimulatingDrive) "⏹ Stop Sim" else "▶ Test Speed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = currentTheme.accentColor
                        )
                    }
                }

                // Center: Big Futuristic Speedometer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$speed",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-1).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "KM / H",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 11.sp
                        ),
                        color = currentTheme.primaryColor
                    )
                }

                // Right: Battery & EV Range Meter
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val battery = powerState.batteryLevel
                    val estimatedRange = (battery * 4.2).toInt()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricCar,
                            contentDescription = "EV Range",
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$estimatedRange KM",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                    }

                    Text(
                        text = "ESTIMATED RANGE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Battery bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(64.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CockpitSurface)
                                .border(0.5.dp, CockpitBorder, RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (battery / 100f).coerceIn(0f, 1f))
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(currentTheme.primaryColor, currentTheme.accentColor)
                                        )
                                    )
                            )
                        }
                        Text(
                            text = "$battery%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = currentTheme.primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tata / Tesla Digital Horizon Line Arc
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            ) {
                val strokeW = 2.dp.toPx()
                drawLine(
                    color = currentTheme.primaryColor.copy(alpha = 0.6f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                // Center pip
                drawCircle(
                    color = currentTheme.accentColor,
                    radius = 4.dp.toPx(),
                    center = Offset(size.width / 2, size.height / 2)
                )
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
                    // Close & Mirror Controls (Unmirrored toggle helper)
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
                                tint = currentTheme.primaryColor
                            )
                        }

                        // Mirror Flip Windshield Reflection toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMirrored) currentTheme.primaryColor else CockpitSurface)
                                .border(1.dp, currentTheme.primaryColor, RoundedCornerShape(12.dp))
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
                                    tint = if (isMirrored) CockpitBlack else currentTheme.primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isMirrored) "MIRROR (ON GLASS)" else "STANDARD HUD",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isMirrored) CockpitBlack else currentTheme.primaryColor
                                )
                            }
                        }
                    }

                    // Huge Central Speed Readout for Glass Projection
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$speed",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 120.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-2).sp
                            ),
                            color = currentTheme.primaryColor
                        )

                        Text(
                            text = "KM / H",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("P", "R", "N", "D").forEach { g ->
                                val isActive = g == gear
                                Text(
                                    text = g,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Light,
                                        fontSize = if (isActive) 26.sp else 20.sp
                                    ),
                                    color = if (isActive) currentTheme.primaryColor else Color.White.copy(alpha = 0.3f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${powerState.batteryLevel}% • ${(powerState.batteryLevel * 4.2).toInt()} KM REMAINING",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            ),
                            color = currentTheme.accentColor
                        )
                    }

                    // Bottom instruction
                    Text(
                        text = if (isMirrored) "Reflected Mode: Place phone on car dashboard to read off windshield" else "Direct Mode: HUD Cluster active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
