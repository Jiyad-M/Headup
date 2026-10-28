package com.example.security

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.data.KioskPreferences
import com.example.model.OtpChallenge
import java.security.SecureRandom
import kotlin.random.Random

class KioskSecurityManager(
    private val context: Context,
    private val preferences: KioskPreferences
) {
    private val wordDictionary = listOf(
        "ALPHA", "APOLLO", "AURORA", "BEACON", "BLAZE", "COBALT",
        "COSMIC", "DELTA", "EAGLE", "FALCON", "GALAXY", "HARBOR",
        "HORIZON", "INFINITY", "JUPITER", "METEOR", "NEBULA", "NOVA",
        "ORBIT", "PHOENIX", "PULSAR", "QUANTUM", "RADAR", "RIVER",
        "SHIELD", "SOLAR", "SUMMIT", "TITAN", "VALLEY", "VORTEX", "ZENITH"
    )

    private var currentChallenge: OtpChallenge? = null

    fun verifyPin(enteredPin: String): Boolean {
        return enteredPin == preferences.pin
    }

    fun updatePin(newPin: String): Boolean {
        if (newPin.length == 8 && newPin.all { it.isDigit() }) {
            preferences.pin = newPin
            return true
        }
        return false
    }

    fun updateSecondaryPhone(newPhone: String) {
        preferences.secondaryPhone = newPhone
    }

    /**
     * Generates a new OTP Challenge containing random words and a 6-digit numeric OTP.
     */
    fun createOtpChallenge(): OtpChallenge {
        val random = SecureRandom()
        val w1 = wordDictionary[random.nextInt(wordDictionary.size)]
        val w2 = wordDictionary[random.nextInt(wordDictionary.size)]
        val w3 = wordDictionary[random.nextInt(wordDictionary.size)]
        val numericSalt = String.format("%04d", random.nextInt(10000))
        val words = "$w1 - $w2 - $w3 - $numericSalt"

        val otp = String.format("%06d", random.nextInt(1000000))
        val challenge = OtpChallenge(
            challengeWords = words,
            otpCode = otp,
            targetPhone = preferences.secondaryPhone
        )
        currentChallenge = challenge

        // Try dispatching SMS if possible
        sendOtpSms(challenge)
        return challenge
    }

    fun getCurrentChallenge(): OtpChallenge? = currentChallenge

    fun verifyOtp(enteredOtp: String): Boolean {
        val challenge = currentChallenge ?: return false
        if (System.currentTimeMillis() > challenge.expiresAt) return false
        val isValid = enteredOtp.trim() == challenge.otpCode
        if (isValid) {
            currentChallenge = null // clear upon success
        }
        return isValid
    }

    private fun sendOtpSms(challenge: OtpChallenge) {
        try {
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
                ?: @Suppress("DEPRECATION") SmsManager.getDefault()
            val message = "Kiosk Emergency Unlock Code: ${challenge.otpCode}\nChallenge: ${challenge.challengeWords}"
            smsManager.sendTextMessage(challenge.targetPhone, null, message, null, null)
            Log.d("KioskSecurity", "Sent unlock SMS to ${challenge.targetPhone}")
        } catch (e: Exception) {
            Log.w("KioskSecurity", "Could not send SMS directly (sim not present or permission denied): ${e.message}")
        }
    }
}
