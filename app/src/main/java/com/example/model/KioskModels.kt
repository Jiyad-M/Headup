package com.example.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val name: String,
    val isAllowed: Boolean = true,
    val isBlacklisted: Boolean = false,
    val isSystem: Boolean = false,
    val icon: Drawable? = null
)

data class PowerState(
    val isPlugged: Boolean = false,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val plugType: String = "Battery"
)

data class PaymentRecord(
    val id: String,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val formattedTime: String,
    val announcementText: String
)

data class OtpChallenge(
    val challengeWords: String,
    val otpCode: String,
    val targetPhone: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 10 * 60 * 1000L
)
