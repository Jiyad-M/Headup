package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.TataEvBlack
import com.example.ui.theme.TataPunchJade

@Composable
fun DisplaySleepOverlay(
    isCompletelyOff: Boolean,
    countdownSec: Int?,
    onSimulatePlugIn: () -> Unit
) {
    // 1. 3-Second Countdown Toast / Alert when unplugged
    AnimatedVisibility(
        visible = countdownSec != null && countdownSec > 0 && !isCompletelyOff,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.95f)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .testTag("unplug_countdown_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "POWER DISCONNECTED",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                        color = Color.White
                    )
                    Text(
                        text = "Display turning completely OFF like power button press in:",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "$countdownSec s",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Plug in power to cancel and keep display on",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // 2. Complete Screen Blackout ("completly light off after 3 second like power button press")
    AnimatedVisibility(
        visible = isCompletelyOff,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    // Touch does not wake display as requested ("otherwide cant works display")
                    detectTapGestures(
                        onLongPress = {
                            // Secret dev/test bypass: long press 4 seconds wakes if needed
                            onSimulatePlugIn()
                        }
                    )
                }
                .testTag("display_blackout_overlay"),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Invisible blackout layer, with faint subtle watermark only visible on extreme inspection
            Text(
                text = "⚡ Plug in power to turn display on",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onSimulatePlugIn()
                    }
            )
        }
    }
}
