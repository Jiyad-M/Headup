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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.model.PowerState
import com.example.ui.theme.CockpitBottomBarBg
import com.example.ui.theme.CockpitTealBgBottom
import com.example.ui.theme.CockpitTealBgTop
import com.example.ui.theme.CockpitTealCardBg
import com.example.ui.theme.CockpitTealCardBorder
import com.example.ui.theme.CockpitTealGlow
import com.example.ui.theme.CockpitTealTrack
import com.example.ui.theme.TataEvTextLight
import com.example.ui.theme.TataEvTextMuted
import com.example.ui.theme.TataPunchDarkJade
import com.example.ui.theme.TataPunchJade
import com.example.ui.theme.TataPunchMint
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
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
    onToggleGpsSimulation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("09:20") }
    var currentDate by remember { mutableStateOf("Tue, 24 Feb") }
    var currentAmPm by remember { mutableStateOf("AM") }
    var currentHour12 by remember { mutableIntStateOf(9) }
    var currentMinute by remember { mutableIntStateOf(20) }

    var isAcActive by remember { mutableStateOf(true) }
    var isAutoActive by remember { mutableStateOf(true) }

    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
        val hourFormat = SimpleDateFormat("hh", Locale.getDefault())
        val minuteFormat = SimpleDateFormat("mm", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            currentAmPm = amPmFormat.format(now).uppercase()
            currentHour12 = hourFormat.format(now).toIntOrNull() ?: 9
            currentMinute = minuteFormat.format(now).toIntOrNull() ?: 20
            delay(1000)
        }
    }

    val filteredApps = remember(permittedApps, searchQuery) {
        if (searchQuery.isBlank()) permittedApps
        else permittedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CockpitTealBgTop, CockpitTealBgBottom)
                )
            )
            .testTag("tata_punch_cockpit_view")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. TOP STATUS BAR (Exact layout from Image 3: Time & Date on left, Icons on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
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

                // Middle: Floating Payment Chime Status (if speaking or received)
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
                            .padding(horizontal = 12.dp, vertical = 4.dp),
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

                // Right: Bluetooth, Battery, Wi-Fi, Cellular, Settings/Lock
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CockpitStatusBarIcons(powerState = powerState)

                    // Quick Audio / Chime test button
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

            // Optional Quick Search Bar
            AnimatedVisibility(visible = isSearchOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
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
                            focusedContainerColor = CockpitTealCardBg,
                            unfocusedContainerColor = CockpitTealCardBg,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }

            // Samsung Home conversion notification banner if not default
            if (!isDefaultHome) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TataPunchDarkJade.copy(alpha = 0.5f))
                        .border(1.dp, TataPunchJade.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable { onPromptSetDefaultLauncher() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = CockpitTealGlow, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Convert Samsung Home Button to Kiosk Launcher",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "SET DEFAULT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = CockpitTealGlow
                        )
                    }
                }
            }

            // 2. THE 3 ICONIC PANELS (SPEEDOMETER, ALLOWED APPS, DIGITAL CLOCK)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                val isWide = maxWidth >= 680.dp

                if (isWide) {
                    // Landscape / Automotive widescreen side-by-side layout (Exact image 3 layout)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Panel: SPEEDOMETER (Driven by real GPS)
                        SpeedometerCockpitCard(
                            speed = gpsSpeed,
                            hasGpsFix = hasGpsFix,
                            gpsStatusText = gpsStatusText,
                            onToggleDrive = onToggleGpsSimulation,
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

                        // Right Panel: DIGITAL CLOCK (12h format + Round hour indicator ring)
                        DigitalClockCockpitCard(
                            timeString = currentTime,
                            amPm = currentAmPm,
                            hour12 = currentHour12,
                            minute = currentMinute,
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
                            onToggleDrive = onToggleGpsSimulation,
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

                        DigitalClockCockpitCard(
                            timeString = currentTime,
                            amPm = currentAmPm,
                            hour12 = currentHour12,
                            minute = currentMinute,
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            // 3. SUB-NAVIGATION ROW (Home, Search, Pager dots, Settings gear)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Home outline icon
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { searchQuery = ""; isSearchOpen = false }
                    )

                    // Search icon
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchOpen) CockpitTealGlow else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { isSearchOpen = !isSearchOpen }
                    )

                    // Pagination dots (• • • • •) with center dot active
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(width = 12.dp, height = 5.dp).clip(RoundedCornerShape(3.dp)).background(CockpitTealGlow))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.4f)))
                    }

                    // Settings gear icon (calls Admin Settings / PIN prompt)
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

            // 4. BOTTOM DOCK BAR (Black bar with central Home button as shown in Image 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(CockpitBottomBarBg)
                    .clickable {
                        searchQuery = ""
                        isSearchOpen = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Device Home Return",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PANEL 1: SPEEDOMETER CARD
// -------------------------------------------------------------
@Composable
fun SpeedometerCockpitCard(
    speed: Int,
    hasGpsFix: Boolean = false,
    gpsStatusText: String = "GPS Ready",
    onToggleDrive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("speedometer_card")
            .clickable { onToggleDrive() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitTealCardBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CockpitTealCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 14.dp),
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
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedSpeed by animateFloatAsState(
                    targetValue = speed.toFloat(),
                    animationSpec = tween(durationMillis = 280),
                    label = "speed"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 8.dp.toPx()
                    val diameter = size.minDimension - strokeWidth - 6.dp.toPx()
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Background track circle
                    drawArc(
                        color = CockpitTealTrack,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Glowing Speed Arc (0 to 160 km/h)
                    val sweepProgress = (animatedSpeed / 160f).coerceIn(0.04f, 1f) * 270f
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

            // GPS Status Pill / Live Telemetry
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (hasGpsFix) TataPunchMint else Color(0xFFF59E0B))
                )
                Text(
                    text = if (hasGpsFix) "GPS: $gpsStatusText" else "$gpsStatusText • Tap to test",
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
    Card(
        modifier = modifier.testTag("allowed_apps_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitTealCardBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CockpitTealCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 12.dp),
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
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Apps Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (apps.isEmpty()) {
                        // Fallback Curated Vehicle Apps if no permissions yet
                        items(defaultVehicleApps) { app ->
                            AppIconItemView(
                                name = app.name,
                                iconRes = app.iconPlaceholderRes,
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

                // Sleek vertical scrollbar pill (As seen in Image 3 right side of center card)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }

            // Bottom: Climate row (A/C, airflow icon, AUTO) matching Image 3
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
// PANEL 3: DIGITAL CLOCK CARD (12H Dial Ring)
// -------------------------------------------------------------
@Composable
fun DigitalClockCockpitCard(
    timeString: String,
    amPm: String,
    hour12: Int = 12,
    minute: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("digital_clock_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitTealCardBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CockpitTealCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 14.dp),
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
                color = TataEvTextLight.copy(alpha = 0.9f)
            )

            // Clock Dial: 12-Hour circular ring indicating hours with ticks and glowing arc
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 6.dp.toPx()
                    val diameter = size.minDimension - strokeWidth - 10.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = diameter / 2f
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Inner background glow
                    drawCircle(
                        color = CockpitTealTrack.copy(alpha = 0.4f),
                        radius = radius,
                        center = center
                    )

                    // Outer base track circle
                    drawCircle(
                        color = CockpitTealTrack,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // 12 Hour Ticks around the circular ring (30 degrees each)
                    for (h in 0..11) {
                        val angleDeg = (h * 30.0) - 90.0
                        val angleRad = Math.toRadians(angleDeg)
                        val isQuarter = (h % 3 == 0) // 12, 3, 6, 9
                        val innerR = if (isQuarter) radius - 10.dp.toPx() else radius - 6.dp.toPx()
                        val outerR = radius - 1.5.dp.toPx()

                        val p1 = Offset(
                            (center.x + innerR * Math.cos(angleRad)).toFloat(),
                            (center.y + innerR * Math.sin(angleRad)).toFloat()
                        )
                        val p2 = Offset(
                            (center.x + outerR * Math.cos(angleRad)).toFloat(),
                            (center.y + outerR * Math.sin(angleRad)).toFloat()
                        )

                        val isPassedHour = (h <= (hour12 % 12))
                        drawLine(
                            color = if (isPassedHour) TataPunchMint else CockpitTealCardBorder,
                            start = p1,
                            end = p2,
                            strokeWidth = if (isQuarter) 2.5.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Active Glowing Hour Progress Arc around the ring (starts at top 12 o'clock / -90 deg)
                    val hourRatio = ((hour12 % 12) + (minute / 60f)) / 12f
                    val sweepAngle = (hourRatio * 360f).coerceIn(4f, 360f)

                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(TataPunchJade, CockpitTealGlow, TataPunchMint)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Glowing illuminated hour marker head
                    val tipAngleRad = Math.toRadians(-90.0 + sweepAngle)
                    val tipX = (center.x + radius * Math.cos(tipAngleRad)).toFloat()
                    val tipY = (center.y + radius * Math.sin(tipAngleRad)).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 4.5.dp.toPx(),
                        center = Offset(tipX, tipY)
                    )
                    drawCircle(
                        color = CockpitTealGlow.copy(alpha = 0.5f),
                        radius = 8.dp.toPx(),
                        center = Offset(tipX, tipY)
                    )
                }

                // Digital Clock Display: 12-hour format "09:20" + "AM/PM"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = Color.White
                    )
                    Text(
                        text = amPm,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        ),
                        color = TataPunchMint
                    )
                }
            }

            // Bottom Subtitle: 12H Ring indicator & Cockpit Temp
            Text(
                text = "12H Dial Ring • Temp 24.0°C",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TataEvTextMuted.copy(alpha = 0.85f)
            )
        }
    }
}

// -------------------------------------------------------------
// APP ICON ITEM COMPONENT
// -------------------------------------------------------------
@Composable
fun AppIconItemView(
    name: String,
    iconRes: Int?,
    iconBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = name,
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = name,
                    tint = CockpitTealGlow,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

data class DefaultVehicleApp(
    val name: String,
    val packageName: String,
    val iconPlaceholderRes: Int? = null
)

val defaultVehicleApps = listOf(
    DefaultVehicleApp("Maps", "com.google.android.apps.maps"),
    DefaultVehicleApp("Music", "com.google.android.apps.youtube.music"),
    DefaultVehicleApp("Delivery", "com.ubercab.driver"),
    DefaultVehicleApp("Messenger", "com.facebook.orca"),
    DefaultVehicleApp("WhatsApp", "com.whatsapp"),
    DefaultVehicleApp("YouTube", "com.google.android.youtube"),
    DefaultVehicleApp("GPay", "com.google.android.apps.nbu.paisa.user"),
    DefaultVehicleApp("Spotify", "com.spotify.music")
)
