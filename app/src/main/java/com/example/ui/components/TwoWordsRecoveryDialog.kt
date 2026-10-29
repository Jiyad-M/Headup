package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.HudCyan
import com.example.ui.theme.HudMint
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TwoWordsRecoveryDialog(
    word1: String,
    word2: String,
    altPhone: String,
    isVerified: Boolean,
    remainingTimeMillis: Long,
    onDismiss: () -> Unit,
    onManualSimulateSms: (sender: String, message: String) -> Boolean,
    onRegenerateWords: () -> Unit,
    onProceedUnlock: () -> Unit
) {
    var testSmsText by remember { mutableStateOf("$word1 $word2") }
    var testSender by remember { mutableStateOf(altPhone.ifEmpty { "+91 9876543210" }) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var liveRemaining by remember { mutableLongStateOf(remainingTimeMillis) }

    LaunchedEffect(remainingTimeMillis, isVerified) {
        liveRemaining = remainingTimeMillis
        while (isVerified && liveRemaining > 0) {
            delay(1000)
            liveRemaining = maxOf(0L, liveRemaining - 1000L)
        }
    }

    val hours = (liveRemaining / (1000 * 60 * 60))
    val minutes = (liveRemaining % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (liveRemaining % (1000 * 60)) / 1000
    val timeFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("two_words_recovery_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isVerified) HudMint else HudCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background((if (isVerified) HudMint else HudCyan).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isVerified) HudMint else HudCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isVerified) "Recovery Verified!" else "Forgot Password Recovery",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = if (isVerified)
                        "SMS from alternative number confirmed. Emergency kiosk access is granted."
                    else
                        "Send an SMS containing these 2 random words from your registered Alt Phone to unlock.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // The 2 Random Words Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CockpitSurface)
                            .border(1.5.dp, HudCyan, RoundedCornerShape(14.dp))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = word1,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CockpitSurface)
                            .border(1.5.dp, HudCyan, RoundedCornerShape(14.dp))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = word2,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Registered Alt Phone Info Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CockpitSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = HudCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Alt Phone: ${altPhone.ifEmpty { "Not registered yet" }}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isVerified) {
                    // 3-Hour Time-Lock Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "3-HOUR PASSWORD RESET TIMELOCK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = AmberGold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (liveRemaining > 0)
                                    "Password can be changed once the 3-hour security cooldown elapses."
                                else
                                    "3 hours elapsed! You can now set a new 8-digit password in settings.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onProceedUnlock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HudMint, contentColor = CockpitBlack)
                    ) {
                        Text("Unlock & Enter Kiosk", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Manual Simulation / Verification Card for test or live backup
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = HudCyan, modifier = Modifier.size(16.dp))
                                Text("Test / Simulate Incoming SMS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = HudCyan)
                            }

                            OutlinedTextField(
                                value = testSender,
                                onValueChange = { testSender = it },
                                label = { Text("Sender Phone", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HudCyan,
                                    unfocusedBorderColor = CockpitBorder
                                )
                            )

                            OutlinedTextField(
                                value = testSmsText,
                                onValueChange = { testSmsText = it },
                                label = { Text("SMS Content (must contain both words)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HudCyan,
                                    unfocusedBorderColor = CockpitBorder
                                )
                            )

                            if (verificationError != null) {
                                Text(
                                    text = verificationError ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = CrimsonRed
                                )
                            }

                            Button(
                                onClick = {
                                    val ok = onManualSimulateSms(testSender, testSmsText)
                                    if (!ok) {
                                        verificationError = "Sender does not match Alt Phone ($altPhone) or words missing!"
                                    } else {
                                        verificationError = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HudCyan, contentColor = CockpitBlack)
                            ) {
                                Text("Verify Words From Alt Number", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = {
                        onRegenerateWords()
                        testSmsText = "$word1 $word2"
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Generate 2 New Random Words", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
