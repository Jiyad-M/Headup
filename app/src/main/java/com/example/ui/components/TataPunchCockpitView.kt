package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.PowerState
import com.example.ui.theme.CockpitBottomBarBg
import com.example.ui.theme.CockpitTealCardBorder
import com.example.ui.theme.CockpitTealGlow
import com.example.ui.theme.CockpitTealTrack
import com.example.ui.theme.TataEvTextLight
import com.example.ui.theme.TataEvTextMuted
import com.example.ui.theme.TataPunchJade
import com.example.ui.theme.TataPunchMint
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TataPunchCockpitView(
    permittedApps: List<AppItem>,
    powerState: PowerState,
    isDefaultHome: Boolean,
    isAnnouncing: Boolean,
    lastAnnouncedText: String,
    gpsSpeed: Int = 0,
    hasGpsFix: Boolean = false,
    gpsStatusText: String = "GPS Ready",
    onAppClick: (String) -> Unit,
    onPromptSetDefaultLauncher: () -> Unit,
    onOpenSettings: () -> Unit,
    onQuickChimeTest: (Int) -> Unit,
    onRequestGpsPermission: () -> Unit = {},
    displayWakeTrigger: Long = 0L,
    powerPlugSweepTrigger: Long = 0L,
    allInstalledApps: List<AppItem> = emptyList(),
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("09:20") }
    var currentDate by remember { mutableStateOf("Tue, 24 Feb") }
    var currentAmPm by remember { mutableStateOf("AM/PM") }
    var currentHour by remember { mutableIntStateOf(9) }
    var currentMinute by remember { mutableIntStateOf(20) }
    var currentSecond by remember { mutableIntStateOf(0) }

    var isAcActive by remember { mutableStateOf(true) }
    var isAutoActive by remember { mutableStateOf(true) }

    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
        while (true) {
            val now = Calendar.getInstance()
            currentTime = timeFormat.format(now.time)
            currentDate = dateFormat.format(now.time)
            currentAmPm = amPmFormat.format(now.time).uppercase()
            currentHour = now.get(Calendar.HOUR)
            currentMinute = now.get(Calendar.MINUTE)
            currentSecond = now.get(Calendar.SECOND)
            delay(1000)
        }
    }

    // Car Ignition Meter Sweep Animation (Speedo & Clock go full then return back on display on from charger plug)
    val animationScope = rememberCoroutineScope()
    val ignitionSweep = remember { Animatable(0f) }

    fun triggerIgnitionSweep() {
        animationScope.launch {
            ignitionSweep.snapTo(0f)
            ignitionSweep.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1350, easing = FastOutSlowInEasing)
            )
            delay(280) // Standard automotive pause at peak redline
            ignitionSweep.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
    }

    // Restrict gauge sweep strictly to when power cable is plugged into the tablet and screen turns on
    var wasPlugged by remember { mutableStateOf(powerState.isPlugged) }
    LaunchedEffect(powerState.isPlugged) {
        if (powerState.isPlugged && !wasPlugged) {
            triggerIgnitionSweep()
        }
        wasPlugged = powerState.isPlugged
    }

    LaunchedEffect(powerPlugSweepTrigger) {
        if (powerPlugSweepTrigger > 0L) {
            triggerIgnitionSweep()
        }
    }

    val filteredApps = remember(permittedApps, allInstalledApps, searchQuery) {
        if (searchQuery.isBlank()) {
            permittedApps
        } else {
            val source = if (allInstalledApps.isNotEmpty()) allInstalledApps else permittedApps
            source.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Cockpit Gradient: Luminous dark teal in Dark Mode, Crisp automotive frost/silver teal in Light Mode
    val cockpitGradient = if (isLightMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE5F1EC), // Crisp radiant daylight frost
                Color(0xFFD4E7E1), // Mid clean automotive silver-teal
                Color(0xFFC3DDD6), // Clean daylight foundation
                Color(0xFFB5D4CC)  // Base
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F473E), // Upper radiant teal glow
                Color(0xFF082C25), // Mid automotive teal
                Color(0xFF031612), // Deep dark cockpit base
                Color(0xFF010A08)  // Solid black bezel
            )
        )
    }

    val primaryTextColor = if (isLightMode) Color(0xFF0D2822) else Color.White
    val mutedTextColor = if (isLightMode) Color(0xFF426860) else TataEvTextMuted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cockpitGradient)
            .testTag("tata_punch_cockpit_view")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. TOP STATUS BAR (Exact layout from uploaded image: Time & Date on left, Icons on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Time (09:20) and Date (Tue, 24 Feb)
                Column {
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = primaryTextColor
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = mutedTextColor
                    )
                }

                // Middle: Floating Soundbox Payment Status (if active)
                AnimatedVisibility(
                    visible = isAnnouncing,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isLightMode) Color(0xFFD7ECE5) else CockpitTealGlow.copy(alpha = 0.2f))
                            .border(1.dp, if (isLightMode) Color(0xFF00796B) else CockpitTealGlow, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isLightMode) Color(0xFF00796B) else CockpitTealGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = lastAnnouncedText.ifEmpty { "Payment Received" },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryTextColor
                        )
                    }
                }

                // Right: Bluetooth, Battery, Wi-Fi, Cellular, Test chime, Settings Lock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CockpitStatusBarIcons(powerState = powerState, tint = primaryTextColor)

                    // Quick Audio Chime test button
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Test Audio Chime",
                        tint = primaryTextColor.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onQuickChimeTest(110) }
                    )

                    // Admin Settings Lock
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Lock",
                        tint = if (isLightMode) Color(0xFF00796B) else CockpitTealGlow,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onOpenSettings() }
                    )
                }
            }

            // Quick App Search Bar
            AnimatedVisibility(visible = isSearchOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cockpit_app_search"),
                        placeholder = { Text("Search allowed apps...", color = TataEvTextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CockpitTealGlow)
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchOpen = false
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CockpitTealGlow,
                            unfocusedBorderColor = CockpitTealCardBorder,
                            focusedContainerColor = Color(0xFF09312A),
                            unfocusedContainerColor = Color(0xFF09312A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }

            // 2. THE 3 ICONIC PANELS (SPEEDOMETER, ALLOWED APPS, TIME/CLOCK)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                val isWide = maxWidth >= 680.dp

                if (isWide) {
                    // Landscape / Automotive widescreen side-by-side layout (Exact image layout)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Panel: SPEEDOMETER (100% Real GPS Hardware + Ignition Gauge Sweep)
                        SpeedometerCockpitCard(
                            speed = gpsSpeed,
                            hasGpsFix = hasGpsFix,
                            gpsStatusText = gpsStatusText,
                            onRequestGpsPermission = onRequestGpsPermission,
                            ignitionProgress = ignitionSweep.value,
                            onRetriggerIgnition = {},
                            isLightMode = isLightMode,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        // Center Panel: ALLOWED APPS + Climate controls
                        AllowedAppsCockpitCard(
                            apps = filteredApps,
                            isAcActive = isAcActive,
                            isAutoActive = isAutoActive,
                            onToggleAc = { isAcActive = !isAcActive },
                            onToggleAuto = { isAutoActive = !isAutoActive },
                            onAppClick = onAppClick,
                            isLightMode = isLightMode,
                            modifier = Modifier
                                .weight(1.35f)
                                .fillMaxHeight()
                        )

                        // Right Panel: DIGITAL CLOCK (Matching photo 1: 09:20 AM/PM with illuminated hour ring)
                        DigitalClockCockpitCard(
                            timeText = currentTime,
                            amPm = currentAmPm,
                            hour = currentHour,
                            minute = currentMinute,
                            ignitionProgress = ignitionSweep.value,
                            onRetriggerIgnition = {},
                            isLightMode = isLightMode,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    // Compact / Portrait: Horizontal scroll row preserving the exact 3 cards side-by-side
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpeedometerCockpitCard(
                            speed = gpsSpeed,
                            hasGpsFix = hasGpsFix,
                            gpsStatusText = gpsStatusText,
                            onRequestGpsPermission = onRequestGpsPermission,
                            ignitionProgress = ignitionSweep.value,
                            onRetriggerIgnition = {},
                            isLightMode = isLightMode,
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                        )

                        AllowedAppsCockpitCard(
                            apps = filteredApps,
                            isAcActive = isAcActive,
                            isAutoActive = isAutoActive,
                            onToggleAc = { isAcActive = !isAcActive },
                            onToggleAuto = { isAutoActive = !isAutoActive },
                            onAppClick = onAppClick,
                            isLightMode = isLightMode,
                            modifier = Modifier
                                .width(360.dp)
                                .fillMaxHeight()
                        )

                        DigitalClockCockpitCard(
                            timeText = currentTime,
                            amPm = currentAmPm,
                            hour = currentHour,
                            minute = currentMinute,
                            ignitionProgress = ignitionSweep.value,
                            onRetriggerIgnition = {},
                            isLightMode = isLightMode,
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            // 3. SUB-NAVIGATION DOCK (Home, Search, Pager dots, Settings gear)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = primaryTextColor.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { searchQuery = ""; isSearchOpen = false }
                    )

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchOpen) (if (isLightMode) Color(0xFF00796B) else CockpitTealGlow) else primaryTextColor.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { isSearchOpen = !isSearchOpen }
                    )

                    // Page Indicator Dots
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val dotInactive = if (isLightMode) Color(0xFF8FB3AB) else Color.White.copy(alpha = 0.4f)
                        val dotActive = if (isLightMode) Color(0xFF00796B) else CockpitTealGlow
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotInactive))
                        Box(modifier = Modifier.width(16.dp).height(5.dp).clip(RoundedCornerShape(2.5.dp)).background(dotActive))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotInactive))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(dotInactive))
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = primaryTextColor.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onOpenSettings() }
                    )
                }
            }

            // 4. BOTTOM HOME BAR (Dedicated center home button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(if (isLightMode) Color(0xFFDCECE6) else CockpitBottomBarBg)
                    .clickable { onPromptSetDefaultLauncher() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Launcher Home",
                    tint = primaryTextColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PANEL 1: SPEEDOMETER CARD (100% Real Hardware GPS + Ignition Sweep)
// -------------------------------------------------------------
@Composable
fun SpeedometerCockpitCard(
    speed: Int,
    hasGpsFix: Boolean = false,
    gpsStatusText: String = "GPS Ready",
    onRequestGpsPermission: () -> Unit = {},
    ignitionProgress: Float = 0f,
    onRetriggerIgnition: () -> Unit = {},
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isIgnitionSweeping = ignitionProgress > 0.005f
    val displaySpeed = if (isIgnitionSweeping) {
        (180f * ignitionProgress).toInt()
    } else {
        speed
    }

    val animatedSpeed by animateFloatAsState(
        targetValue = displaySpeed.toFloat(),
        animationSpec = tween(durationMillis = if (isIgnitionSweeping) 40 else 300),
        label = "speed"
    )

    val cardGradient = if (isLightMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF2F8F5),
                Color(0xFFE6F0EC)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F473E), // Upper radiant teal
                Color(0xFF09312A), // Mid dark teal
                Color(0xFF05221C)  // Deep cockpit base
            )
        )
    }

    val cardBorderColor = if (isLightMode) {
        if (isIgnitionSweeping) Color(0xFF00897B) else Color(0xFF7CB8AB)
    } else {
        if (isIgnitionSweeping) CockpitTealGlow else Color(0xFF1B6B5C).copy(alpha = 0.85f)
    }

    val headerTextColor = if (isLightMode) Color(0xFF144137) else TataEvTextLight.copy(alpha = 0.9f)
    val digitsTextColor = if (isLightMode) Color(0xFF0D2822) else Color.White
    val unitTextColor = if (isLightMode) {
        if (isIgnitionSweeping) Color(0xFF00796B) else Color(0xFF426860)
    } else {
        if (isIgnitionSweeping) TataPunchMint else TataEvTextMuted
    }
    val trackBgColor = if (isLightMode) Color(0xFFD3E7E1) else Color(0xFF051C17)
    val sweepBrushColors = if (isLightMode) {
        listOf(Color(0xFF00796B), Color(0xFF009688), Color(0xFF26A69A))
    } else {
        listOf(TataPunchJade, CockpitTealGlow, TataPunchMint)
    }

    Card(
        modifier = modifier
            .testTag("speedometer_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: SPEEDOMETER
            Text(
                text = "SPEEDOMETER",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 13.sp
                ),
                color = headerTextColor
            )

            // Speedometer Circular Dial
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val diameter = size.minDimension - strokeWidth - 6.dp.toPx()
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Background track circle
                    drawArc(
                        color = trackBgColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Glowing Speed Arc (Sweeps to 270° on ignition, then returns to live GPS speed)
                    val sweepProgress = if (isIgnitionSweeping) {
                        (ignitionProgress * 270f).coerceIn(2f, 270f)
                    } else {
                        (animatedSpeed / 180f).coerceIn(0.02f, 1f) * 270f
                    }

                    drawArc(
                        brush = Brush.sweepGradient(sweepBrushColors),
                        startAngle = 135f,
                        sweepAngle = sweepProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center readout: "65" + "km/h"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$displaySpeed",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = digitsTextColor
                    )
                    Text(
                        text = if (isIgnitionSweeping) "SWEEP" else "km/h",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isIgnitionSweeping) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        color = unitTextColor
                    )
                }
            }

            // Real-Time GPS Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { onRequestGpsPermission() }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (hasGpsFix) (if (isLightMode) Color(0xFF00796B) else TataPunchMint) else Color(0xFFFFB300))
                )
                Text(
                    text = if (hasGpsFix) "GPS: Real-time ($gpsStatusText)" else "$gpsStatusText • Tap to allow",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (hasGpsFix) (if (isLightMode) Color(0xFF00796B) else TataPunchMint) else (if (isLightMode) Color(0xFF6B8E86) else TataEvTextMuted)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PANEL 2: ALLOWED APPS + CLIMATE CONTROLS CARD
// -------------------------------------------------------------
@Composable
fun AllowedAppsCockpitCard(
    apps: List<AppItem>,
    isAcActive: Boolean,
    isAutoActive: Boolean,
    onToggleAc: () -> Unit,
    onToggleAuto: () -> Unit,
    onAppClick: (String) -> Unit,
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardGradient = if (isLightMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF2F8F5),
                Color(0xFFE6F0EC)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F473E),
                Color(0xFF09312A),
                Color(0xFF05221C)
            )
        )
    }

    val cardBorderColor = if (isLightMode) Color(0xFF7CB8AB) else Color(0xFF1B6B5C).copy(alpha = 0.85f)
    val headerTextColor = if (isLightMode) Color(0xFF144137) else TataEvTextLight.copy(alpha = 0.9f)
    val appTextColor = if (isLightMode) Color(0xFF0D2822) else Color.White
    val controlActiveColor = if (isLightMode) Color(0xFF0D2822) else Color.White
    val controlInactiveColor = if (isLightMode) Color(0xFF6B8E86) else TataEvTextMuted
    val controlBarColor = if (isLightMode) Color(0xFF00796B) else CockpitTealGlow

    Card(
        modifier = modifier.testTag("allowed_apps_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: ALLOWED APPS
            Text(
                text = "ALLOWED APPS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 13.sp
                ),
                color = headerTextColor
            )

            // Middle: 2-Row x 4-Col Apps Grid with sleek right scrollbar indicator
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Apps Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (apps.isEmpty()) {
                        items(defaultVehicleApps) { app ->
                            AppIconItemView(
                                name = app.name,
                                iconRes = null,
                                iconBitmap = null,
                                textColor = appTextColor,
                                isLightMode = isLightMode,
                                onClick = { onAppClick(app.packageName) }
                            )
                        }
                    } else {
                        items(apps) { app ->
                            val bitmap = rememberDrawableBitmap(app.icon)
                            AppIconItemView(
                                name = app.name,
                                iconRes = null,
                                iconBitmap = bitmap,
                                isBlocked = !app.isAllowed || app.isBlacklisted,
                                textColor = appTextColor,
                                isLightMode = isLightMode,
                                onClick = { onAppClick(app.packageName) }
                            )
                        }
                    }
                }

                // Sleek vertical scrollbar pill (Matching uploaded image)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isLightMode) Color(0xFF7CB8AB).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.45f))
                )
            }

            // Bottom: Climate row (A/C, airflow icon, AUTO) matching uploaded image
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // A/C Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onToggleAc() }
                ) {
                    Text(
                        text = "A/C",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = if (isAcActive) controlActiveColor else controlInactiveColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (isAcActive) controlBarColor else Color.Transparent)
                    )
                }

                // Airflow icon
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = "Airflow",
                    tint = if (isLightMode) Color(0xFF0D2822).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )

                // AUTO Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onToggleAuto() }
                ) {
                    Text(
                        text = "AUTO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = if (isAutoActive) controlActiveColor else controlInactiveColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (isAutoActive) controlBarColor else Color.Transparent)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANEL 3: DIGITAL CLOCK CARD (Matching Photo 1: 09:20 AM/PM with Illuminated Hour Ring + Ignition Sweep)
// -------------------------------------------------------------
@Composable
fun DigitalClockCockpitCard(
    timeText: String,
    amPm: String,
    hour: Int,
    minute: Int,
    ignitionProgress: Float = 0f,
    onRetriggerIgnition: () -> Unit = {},
    isLightMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isIgnitionSweeping = ignitionProgress > 0.005f
    val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val progressFraction = (hour12 % 12 + minute / 60f) / 12f

    // When ignition is active, ring sweeps full 360 degrees then returns smoothly to real hour!
    val sweepAngle = if (isIgnitionSweeping) {
        (ignitionProgress * 360f).coerceAtLeast(10f)
    } else {
        (progressFraction * 360f).coerceAtLeast(20f)
    }

    val cardGradient = if (isLightMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF2F8F5),
                Color(0xFFE6F0EC)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F473E),
                Color(0xFF09312A),
                Color(0xFF05221C)
            )
        )
    }

    val cardBorderColor = if (isLightMode) {
        if (isIgnitionSweeping) Color(0xFF00897B) else Color(0xFF7CB8AB)
    } else {
        if (isIgnitionSweeping) CockpitTealGlow else Color(0xFF1B6B5C).copy(alpha = 0.85f)
    }

    val headerTextColor = if (isLightMode) Color(0xFF144137) else TataEvTextLight.copy(alpha = 0.9f)
    val digitsTextColor = if (isLightMode) Color(0xFF0D2822) else Color.White
    val amPmTextColor = if (isLightMode) {
        if (isIgnitionSweeping) Color(0xFF00796B) else Color(0xFF426860)
    } else {
        if (isIgnitionSweeping) TataPunchMint else TataPunchMint.copy(alpha = 0.9f)
    }
    val dialBackgroundColors = if (isLightMode) {
        listOf(Color(0xFFFFFFFF), Color(0xFFE6F0EC))
    } else {
        listOf(Color(0xFF0C463C), Color(0xFF041F1A))
    }
    val trackRingColor = if (isLightMode) Color(0xFFD3E7E1) else Color(0xFF0B342C)
    val outerBezelColor = if (isLightMode) Color(0xFF7CB8AB).copy(alpha = 0.6f) else Color(0xFF1B6B5C).copy(alpha = 0.6f)
    val clockSweepColors = if (isLightMode) {
        if (isIgnitionSweeping) {
            listOf(Color(0xFF00796B), Color(0xFF009688), Color(0xFF26A69A))
        } else {
            listOf(Color(0xFF00796B), Color(0xFF009688), Color(0xFF26A69A))
        }
    } else {
        if (isIgnitionSweeping) {
            listOf(CockpitTealGlow, TataPunchMint, CockpitTealGlow)
        } else {
            listOf(TataPunchJade, TataPunchMint, TataPunchJade)
        }
    }

    Card(
        modifier = modifier
            .testTag("digital_clock_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: DIGITAL CLOCK
            Text(
                text = "DIGITAL CLOCK",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 13.sp
                ),
                color = headerTextColor
            )

            // Round Circular Gauge Ring with Digital Time Inside (Exact from photo 1)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (size.minDimension / 2f) - 6.dp.toPx()

                    // Dial background: radial gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = dialBackgroundColors,
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // Track background ring
                    drawCircle(
                        color = trackRingColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6.dp.toPx())
                    )

                    // Outer fine bezel
                    drawCircle(
                        color = outerBezelColor,
                        radius = radius + 3.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Active 12-hour progression arc (Sweeps 360° on ignition, then returns to current hour)
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = clockSweepColors,
                            center = center
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Digital Clock Digits (09:20 AM/PM)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            fontSize = 32.sp
                        ),
                        color = digitsTextColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isIgnitionSweeping) "READY" else amPm.ifEmpty { "AM/PM" },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            fontSize = 12.sp
                        ),
                        color = amPmTextColor
                    )
                }
            }

            // Real-Time Clock Status
            Text(
                text = if (isIgnitionSweeping) "Meter Sweep Check Active" else "12-Hour Active Digital Clock",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isIgnitionSweeping) (if (isLightMode) Color(0xFF00796B) else TataPunchMint) else (if (isLightMode) Color(0xFF426860) else TataEvTextMuted)
            )
        }
    }
}

// App Icon Item Component
@Composable
fun AppIconItemView(
    name: String,
    iconRes: Int?,
    iconBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    isBlocked: Boolean = false,
    textColor: Color = Color.White,
    isLightMode: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        val iconBoxBg = if (isLightMode) {
            Brush.verticalGradient(
                listOf(Color(0xFFE2EFEA), Color(0xFFD4E7E1))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0xFF145246), Color(0xFF092922))
            )
        }
        val iconBoxBorder = if (isBlocked) {
            Color(0xFFFFB300)
        } else if (isLightMode) {
            Color(0xFF7CB8AB).copy(alpha = 0.8f)
        } else {
            Color(0xFF1D6F60).copy(alpha = 0.6f)
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBoxBg)
                .border(1.dp, iconBoxBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = name,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = name,
                    tint = if (isLightMode) Color(0xFF00796B) else TataPunchMint,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isBlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Protected",
                        tint = Color.Black,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

val defaultVehicleApps = listOf(
    AppItem(packageName = "com.google.android.apps.maps", name = "Maps"),
    AppItem(packageName = "com.google.android.apps.youtube.music", name = "Music"),
    AppItem(packageName = "com.ubercab.driver", name = "Delivery"),
    AppItem(packageName = "com.whatsapp", name = "WhatsApp"),
    AppItem(packageName = "com.google.android.youtube", name = "YouTube"),
    AppItem(packageName = "com.spotify.music", name = "Spotify")
)
