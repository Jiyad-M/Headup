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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentRecord
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentSoundboxCard(
    currency: String,
    isAnnouncing: Boolean,
    lastAnnouncedText: String,
    recentPayments: List<PaymentRecord>,
    onAnnounce: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var customAmountText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val presetAmounts = listOf(68.0, 110.0, 182.0, 500.0, 1000.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_soundbox_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAnnouncing) EmeraldGreen else Slate800
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .border(1.dp, EmeraldGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAnnouncing) Icons.Default.GraphicEq else Icons.Default.Campaign,
                            contentDescription = "Payment Soundbox",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "PAYMENT SOUNDBOX",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = EmeraldGreen
                        )
                        Text(
                            text = "Voice Tone Chime • Real-time",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chime Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "{Tone} {amount} received",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Live Announcing Banner
            AnimatedVisibility(
                visible = isAnnouncing && lastAnnouncedText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .border(1.dp, EmeraldGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = EmeraldGreenLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "🔊 Playing: \"[Tone] $lastAnnouncedText\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenLight
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Quick Announce Presets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Preset Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetAmounts.forEach { amt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate800)
                            .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                            .clickable { onAnnounce(amt) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("quick_amt_${amt.toInt()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$currency${amt.toInt()}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            customAmountText = input
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_payment_input"),
                    placeholder = { Text("Custom amount...", fontSize = 13.sp) },
                    prefix = { Text(currency, color = EmeraldGreen, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val amt = customAmountText.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onAnnounce(amt)
                                customAmountText = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = Slate700
                    )
                )

                Button(
                    onClick = {
                        val amt = customAmountText.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            onAnnounce(amt)
                            customAmountText = ""
                            keyboardController?.hide()
                        }
                    },
                    enabled = customAmountText.toDoubleOrNull() != null,
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("announce_custom_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Slate900
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Chime",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Announce", fontWeight = FontWeight.Bold)
                }
            }

            // Recent Announcement Snippet
            if (recentPayments.isNotEmpty()) {
                val latest = recentPayments.first()
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Last: ${latest.announcementText} (${latest.formattedTime})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Replay",
                        tint = EmeraldGreen,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onAnnounce(latest.amount) }
                    )
                }
            }
        }
    }
}
