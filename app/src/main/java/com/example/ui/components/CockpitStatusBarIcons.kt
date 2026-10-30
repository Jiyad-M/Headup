package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.PowerState
import com.example.ui.theme.CockpitTealGlow

@Composable
fun CockpitStatusBarIcons(
    powerState: PowerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Bluetooth Icon
        BluetoothIcon(color = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))

        // 2. Battery Icon with charging bolt / level
        CockpitBatteryIcon(
            isCharging = powerState.isPlugged || powerState.isCharging,
            batteryPercent = powerState.batteryLevel,
            modifier = Modifier.size(width = 22.dp, height = 13.dp)
        )

        // 3. Wi-Fi Icon
        CockpitWifiIcon(color = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))

        // 4. Cellular 4-Bar Signal Icon
        CockpitCellularIcon(color = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun BluetoothIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            // Center vertical line
            moveTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.5f, h * 0.9f)
            // Top right branch & back
            lineTo(w * 0.82f, h * 0.65f)
            lineTo(w * 0.22f, h * 0.35f)
            // Center cross
            moveTo(w * 0.22f, h * 0.65f)
            lineTo(w * 0.82f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.1f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CockpitBatteryIcon(
    isCharging: Boolean,
    batteryPercent: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = 1.5.dp.toPx()
        val bodyW = w - 3.5.dp.toPx()

        // Battery outer pill
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(0f, 0f),
            size = Size(bodyW, h),
            cornerRadius = CornerRadius(2.5.dp.toPx()),
            style = Stroke(width = strokeW)
        )

        // Battery positive nipple
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(bodyW + 1.dp.toPx(), h * 0.3f),
            size = Size(2.dp.toPx(), h * 0.4f),
            cornerRadius = CornerRadius(1.dp.toPx())
        )

        // Fill inner charge
        val clampedPct = (batteryPercent.coerceIn(5, 100)) / 100f
        val innerPadding = 2.5.dp.toPx()
        val fillW = (bodyW - innerPadding * 2) * clampedPct
        val fillColor = if (isCharging) CockpitTealGlow else Color.White

        drawRoundRect(
            color = fillColor,
            topLeft = Offset(innerPadding, innerPadding),
            size = Size(fillW, h - innerPadding * 2),
            cornerRadius = CornerRadius(1.dp.toPx())
        )

        // Charging Lightning bolt if plugged in
        if (isCharging) {
            val boltPath = Path().apply {
                moveTo(bodyW * 0.55f, h * 0.15f)
                lineTo(bodyW * 0.35f, h * 0.52f)
                lineTo(bodyW * 0.52f, h * 0.52f)
                lineTo(bodyW * 0.42f, h * 0.85f)
                lineTo(bodyW * 0.65f, h * 0.45f)
                lineTo(bodyW * 0.48f, h * 0.45f)
                close()
            }
            drawPath(boltPath, color = Color.Black)
        }
    }
}

@Composable
fun CockpitWifiIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h * 0.85f)

        // Dot
        drawCircle(color = color, radius = 1.6.dp.toPx(), center = center)

        // Arc 1 (inner)
        drawArc(
            color = color,
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(center.x - w * 0.25f, center.y - h * 0.35f),
            size = Size(w * 0.5f, h * 0.5f),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Arc 2 (outer)
        drawArc(
            color = color,
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(center.x - w * 0.45f, center.y - h * 0.65f),
            size = Size(w * 0.9f, h * 0.9f),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CockpitCellularIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barWidth = 2.4.dp.toPx()
        val spacing = (w - barWidth * 4) / 3f

        for (i in 0..3) {
            val barHeight = h * (0.35f + (i * 0.2f))
            val x = i * (barWidth + spacing)
            val y = h - barHeight
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(1.dp.toPx())
            )
        }
    }
}
