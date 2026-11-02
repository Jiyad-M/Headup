package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("09:20") }
    var currentDate by remember { mutableStateOf("Tue, 24 Feb") }
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
        while (true) {
            val now = Calendar.getInstance()
            currentTime = timeFormat.format(now.time)
            currentDate = dateFormat.format(now.time)
            currentHour = now.get(Calendar.HOUR)
            currentMinute = now.get(Calendar.MINUTE)
            currentSecond = now.get(Calendar.SECOND)
            delay(1000)
        }
    }

    val filteredApps = remember(permittedApps, searchQuery) {
        if (searchQuery.isBlank()) permittedApps
        else permittedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Exact Cockpit Gradient from Uploaded Image: Luminous dark teal glowing backdrop
    val cockpitGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F473E), // Upper radiant teal glow
            Color(0xFF082C25), // Mid automotive teal
            Color(0xFF031612), // Deep dark cockpit base
            Color(0xFF010A08)  // Solid black bezel
        )
    )

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
                        color = Color.White
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TataEvTextMuted
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
                            .background(CockpitTealGlow.copy(alpha = 0.2f))
                            .border(1.dp, CockpitTealGlow, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = CockpitTealGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = lastAnnouncedText.ifEmpty { "Payment Received" },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Right: Bluetooth, Battery, Wi-Fi, Cellular, Test chime, Settings Lock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CockpitStatusBarIcons(powerState = powerState)

                    // Quick Audio Chime test button
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Test Audio Chime",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onQuickChimeTest(110) }
                    )

                    // Admin Settings Lock
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Lock",
                        tint = CockpitTealGlow,
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
                        // Left Panel: SPEEDOMETER (100% Real GPS Hardware)
                        SpeedometerCockpitCard(
                            speed = gpsSpeed,
                            hasGpsFix = hasGpsFix,
                            gpsStatusText = gpsStatusText,
                            onRequestGpsPermission = onRequestGpsPermission,
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
                            modifier = Modifier
                                .weight(1.35f)
                                .fillMaxHeight()
                        )

                        // Right Panel: TIME/CLOCK (Authentic Analog Clock Dial from Uploaded Image)
                        AnalogClockCockpitCard(
                            hour = currentHour,
                            minute = currentMinute,
                            second = currentSecond,
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
                            modifier = Modifier
                                .width(360.dp)
                                .fillMaxHeight()
                        )

                        AnalogClockCockpitCard(
                            hour = currentHour,
                            minute = currentMinute,
                            second = currentSecond,
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
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { searchQuery = ""; isSearchOpen = false }
                    )

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchOpen) CockpitTealGlow else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { isSearchOpen = !isSearchOpen }
                    )

                    // Page Indicator Dots
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.width(16.dp).height(5.dp).clip(RoundedCornerShape(2.5.dp)).background(CockpitTealGlow))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.85f),
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
                    .background(CockpitBottomBarBg)
                    .clickable { onPromptSetDefaultLauncher() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Launcher Home",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PANEL 1: SPEEDOMETER CARD (100% Real Hardware GPS)
// -------------------------------------------------------------
@Composable
fun SpeedometerCockpitCard(
    speed: Int,
    hasGpsFix: Boolean = false,
    gpsStatusText: String = "GPS Ready",
    onRequestGpsPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F473E), // Upper radiant teal
            Color(0xFF09312A), // Mid dark teal
            Color(0xFF05221C)  // Deep cockpit base
        )
    )

    Card(
        modifier = modifier.testTag("speedometer_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1B6B5C).copy(alpha = 0.85f))
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
                color = TataEvTextLight.copy(alpha = 0.9f)
            )

            // Speedometer Circular Dial
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedSpeed by animateFloatAsState(
                    targetValue = speed.toFloat(),
                    animationSpec = tween(durationMillis = 300),
                    label = "speed"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val diameter = size.minDimension - strokeWidth - 6.dp.toPx()
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Background track circle
                    drawArc(
                        color = Color(0xFF051C17),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Glowing Speed Arc (0 to 180 km/h)
                    val sweepProgress = (animatedSpeed / 180f).coerceIn(0.02f, 1f) * 270f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(TataPunchJade, CockpitTealGlow, TataPunchMint)
                        ),
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
                        text = "$speed",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "km/h",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        color = TataEvTextMuted
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
                        .background(if (hasGpsFix) TataPunchMint else Color(0xFFFFB300))
                )
                Text(
                    text = if (hasGpsFix) "GPS: Real-time ($gpsStatusText)" else "$gpsStatusText • Tap to allow",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (hasGpsFix) TataPunchMint else TataEvTextMuted
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
    modifier: Modifier = Modifier
) {
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F473E),
            Color(0xFF09312A),
            Color(0xFF05221C)
        )
    )

    Card(
        modifier = modifier.testTag("allowed_apps_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1B6B5C).copy(alpha = 0.85f))
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
                color = TataEvTextLight.copy(alpha = 0.9f)
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
                        .background(Color.White.copy(alpha = 0.45f))
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
                        color = if (isAcActive) Color.White else TataEvTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (isAcActive) CockpitTealGlow else Color.Transparent)
                    )
                }

                // Airflow icon
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = "Airflow",
                    tint = Color.White.copy(alpha = 0.85f),
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
                        color = if (isAutoActive) Color.White else TataEvTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(if (isAutoActive) CockpitTealGlow else Color.Transparent)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANEL 3: TIME/CLOCK CARD (Authentic Analog Clock Dial from Uploaded Image)
// -------------------------------------------------------------
@Composable
fun AnalogClockCockpitCard(
    hour: Int,
    minute: Int,
    second: Int,
    modifier: Modifier = Modifier
) {
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F473E),
            Color(0xFF09312A),
            Color(0xFF05221C)
        )
    )

    Card(
        modifier = modifier.testTag("digital_clock_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1B6B5C).copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: TIME/CLOCK
            Text(
                text = "TIME/CLOCK",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 13.sp
                ),
                color = TataEvTextLight.copy(alpha = 0.9f)
            )

            // Analog Clock Face (Exact design from uploaded image)
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
                            colors = listOf(Color(0xFF0C463C), Color(0xFF041F1A)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // Outer bezel ring
                    drawCircle(
                        color = Color(0xFF1B6B5C).copy(alpha = 0.9f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 60 Tick Marks around circumference
                    for (i in 0 until 60) {
                        val isHourTick = (i % 5 == 0)
                        val tickLen = if (isHourTick) 8.dp.toPx() else 4.dp.toPx()
                        val tickWidth = if (isHourTick) 2.dp.toPx() else 1.dp.toPx()
                        val tickColor = if (isHourTick) Color.White else Color(0xFF1B6B5C).copy(alpha = 0.7f)

                        rotate(degrees = i * 6f, pivot = center) {
                            drawLine(
                                color = tickColor,
                                start = Offset(center.x, center.y - radius + 3.dp.toPx()),
                                end = Offset(center.x, center.y - radius + 3.dp.toPx() + tickLen),
                                strokeWidth = tickWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Hour Numerals: 12, 3, 6, 9
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 15.sp.toPx()
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.SANS_SERIF,
                            android.graphics.Typeface.BOLD
                        )
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    val numeralOffset = radius * 0.72f
                    // 12
                    drawContext.canvas.nativeCanvas.drawText("12", center.x, center.y - numeralOffset + (paint.textSize / 3), paint)
                    // 3
                    drawContext.canvas.nativeCanvas.drawText("3", center.x + numeralOffset, center.y + (paint.textSize / 3), paint)
                    // 6
                    drawContext.canvas.nativeCanvas.drawText("6", center.x, center.y + numeralOffset + (paint.textSize / 3), paint)
                    // 9
                    drawContext.canvas.nativeCanvas.drawText("9", center.x - numeralOffset, center.y + (paint.textSize / 3), paint)

                    // Calculate Angles
                    val hourAngle = ((hour % 12) + (minute / 60f)) * 30f
                    val minuteAngle = (minute + (second / 60f)) * 6f
                    val secondAngle = second * 6f

                    // Hour Hand
                    rotate(degrees = hourAngle, pivot = center) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.95f),
                            start = center,
                            end = Offset(center.x, center.y - (radius * 0.52f)),
                            strokeWidth = 4.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Minute Hand
                    rotate(degrees = minuteAngle, pivot = center) {
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(center.x, center.y - (radius * 0.76f)),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Second Hand (Mint/Cyan Needle)
                    rotate(degrees = secondAngle, pivot = center) {
                        drawLine(
                            color = TataPunchMint,
                            start = Offset(center.x, center.y + (radius * 0.16f)), // Tail
                            end = Offset(center.x, center.y - (radius * 0.86f)),   // Needle tip
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Center Pivot Dot
                    drawCircle(
                        color = TataPunchMint,
                        radius = 4.5.dp.toPx(),
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF041F1A),
                        radius = 2.dp.toPx(),
                        center = center
                    )
                }
            }

            // Real-Time Clock Status
            Text(
                text = "Real-time System Clock",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TataEvTextMuted
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF145246), Color(0xFF092922))
                    )
                )
                .border(1.dp, Color(0xFF1D6F60).copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
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
                    tint = TataPunchMint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White,
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
