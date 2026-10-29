package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CockpitBlack
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.HudCyan
import com.example.ui.theme.HudMint

@Composable
fun AccountActivationScreen(
    onActivate: (name: String, phone: String, altPhone: String, password: String) -> Unit,
    onSkipIfTesting: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var altPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("activation_screen"),
        color = CockpitBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(HudCyan.copy(alpha = 0.15f))
                    .border(1.5.dp, HudCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = HudCyan,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "KIOSK APP ACTIVATION",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )

            Text(
                text = "Register driver account to activate kiosk interface on this Samsung One UI device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Activation Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_name_field"),
                        label = { Text("Driver / Merchant Name") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = HudCyan)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = HudCyan,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    // Primary Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_phone_field"),
                        label = { Text("Primary Phone Number") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = HudCyan)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = HudCyan,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    // Alt Phone
                    OutlinedTextField(
                        value = altPhone,
                        onValueChange = { altPhone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_alt_phone_field"),
                        label = { Text("Alternative Phone Number (Password Recovery)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = AmberGold)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = AmberGold,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    // 8-Digit Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_pwd_field"),
                        label = { Text("8-Digit Password (PIN)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = HudCyan)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = HudCyan,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    // Confirm 8-Digit Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) confirmPassword = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_confirm_pwd_field"),
                        label = { Text("Confirm 8-Digit Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = HudCyan)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CockpitSurface,
                            unfocusedContainerColor = CockpitSurface,
                            focusedBorderColor = HudCyan,
                            unfocusedBorderColor = CockpitBorder
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Activate Button
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Please enter your name"
                            } else if (phone.isBlank()) {
                                errorMessage = "Please enter your phone number"
                            } else if (altPhone.isBlank()) {
                                errorMessage = "Please enter an alternative phone number for recovery"
                            } else if (password.length != 8) {
                                errorMessage = "Password must be exactly 8 digits"
                            } else if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                            } else {
                                errorMessage = null
                                onActivate(name, phone, altPhone, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("activate_app_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HudCyan, contentColor = CockpitBlack)
                    ) {
                        Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Activate App & Set as Default Launcher", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Samsung One UI Info Callout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CockpitSurface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = HudCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = "Pressing Activate switches original Samsung One UI to this Kiosk app. You can go back anytime via the 8-digit password.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
