package com.example.security

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.data.KioskPreferences
import com.example.model.OtpChallenge
import java.security.SecureRandom

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
            // Reset recovery states upon successful password update
            preferences.isRecoveryVerified = false
            preferences.passwordResetUnlockTime = 0L
            return true
        }
        return false
    }

    fun updateSecondaryPhone(newPhone: String) {
        preferences.altPhone = newPhone
    }

    /**
     * Generates exactly two random words for the forgotten password recovery flow.
     * "When tap forget button a two random words displayed."
     */
    fun generateTwoRandomWords(): Pair<String, String> {
        val random = SecureRandom()
        val index1 = random.nextInt(wordDictionary.size)
        var index2 = random.nextInt(wordDictionary.size)
        while (index2 == index1) {
            index2 = random.nextInt(wordDictionary.size)
        }
        val w1 = wordDictionary[index1]
        val w2 = wordDictionary[index2]
        preferences.recoveryWord1 = w1
        preferences.recoveryWord2 = w2
        preferences.isRecoveryVerified = false
        preferences.passwordResetUnlockTime = 0L
        return Pair(w1, w2)
    }

    /**
     * Verifies that the two random words were sent from the registered alt number.
     * Starts the 3-hour timelock countdown.
     */
    fun verifyTwoWordsFromAlt(sender: String, messageText: String): Boolean {
        val w1 = preferences.recoveryWord1.trim().uppercase()
        val w2 = preferences.recoveryWord2.trim().uppercase()
        val altPhone = preferences.altPhone.trim()

        if (w1.isEmpty() || w2.isEmpty()) return false

        val isFromAlt = SmsRecoveryReceiver.matchesPhoneNumber(sender, altPhone)
        val upperMsg = messageText.uppercase()
        val hasWords = upperMsg.contains(w1) && upperMsg.contains(w2)

        if (isFromAlt && hasWords) {
            preferences.isRecoveryVerified = true
            // If get change passwd after 3hours
            preferences.passwordResetUnlockTime = System.currentTimeMillis() + (3 * 3600 * 1000L)
            return true
        }
        return false
    }

    fun isRecoveryVerified(): Boolean = preferences.isRecoveryVerified

    fun canChangePasswordNow(): Boolean {
        val unlockTime = preferences.passwordResetUnlockTime
        return unlockTime > 0L && System.currentTimeMillis() >= unlockTime
    }

    fun getRemainingLockTimeMillis(): Long {
        val unlockTime = preferences.passwordResetUnlockTime
        return if (unlockTime <= 0L) 0L else maxOf(0L, unlockTime - System.currentTimeMillis())
    }

    /**
     * Legacy OTP Challenge support with words + 6-digit OTP
     */
    fun createOtpChallenge(): OtpChallenge {
        val random = SecureRandom()
        val (w1, w2) = generateTwoRandomWords()
        val numericSalt = String.format("%04d", random.nextInt(10000))
        val words = "$w1 - $w2 - $numericSalt"

        val otp = String.format("%06d", random.nextInt(1000000))
        val challenge = OtpChallenge(
            challengeWords = "$w1 $w2",
            otpCode = otp,
            targetPhone = preferences.altPhone
        )
        currentChallenge = challenge
        return challenge
    }

    fun verifyOtp(enteredOtp: String): Boolean {
        val challenge = currentChallenge ?: return false
        if (System.currentTimeMillis() > challenge.expiresAt) return false
        val isValid = enteredOtp.trim() == challenge.otpCode
        if (isValid) {
            preferences.isRecoveryVerified = true
            preferences.passwordResetUnlockTime = System.currentTimeMillis() + (3 * 3600 * 1000L)
            currentChallenge = null
        }
        return isValid
    }
}
