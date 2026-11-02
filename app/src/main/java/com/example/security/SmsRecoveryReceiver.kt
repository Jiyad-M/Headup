package com.example.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.data.KioskPreferences
import com.example.soundbox.PaymentNotificationListenerService
import com.example.soundbox.PaymentSoundboxManager

class SmsRecoveryReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
        var onSmsRecoveryVerified: ((sender: String, message: String) -> Unit)? = null

        /**
         * Checks whether a phone number matches the registered alternative number,
         * normalizing country codes, dashes, and spaces.
         */
        fun matchesPhoneNumber(phoneA: String, phoneB: String): Boolean {
            val cleanA = phoneA.filter { it.isDigit() }
            val cleanB = phoneB.filter { it.isDigit() }
            if (cleanA.isEmpty() || cleanB.isEmpty()) return false
            return cleanA.endsWith(cleanB) || cleanB.endsWith(cleanA) || cleanA == cleanB
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages: Array<SmsMessage>? = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val prefs = KioskPreferences(context)
            val word1 = prefs.recoveryWord1.trim().uppercase()
            val word2 = prefs.recoveryWord2.trim().uppercase()
            val altPhone = prefs.altPhone.trim()

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: ""
                val body = sms.displayMessageBody ?: ""
                val upperBody = body.uppercase()

                Log.d(TAG, "Incoming SMS from [$sender]: $body")

                // 1. Check for Kiosk 2-word emergency recovery
                if (word1.isNotEmpty() && word2.isNotEmpty()) {
                    val isFromAltPhone = matchesPhoneNumber(sender, altPhone)
                    val containsBothWords = upperBody.contains(word1) && upperBody.contains(word2)

                    if (isFromAltPhone && containsBothWords) {
                        Log.i(TAG, "Valid recovery SMS received from alt phone with words [$word1, $word2]!")
                        val threeHoursLater = System.currentTimeMillis() + (3 * 3600 * 1000L)
                        prefs.isRecoveryVerified = true
                        prefs.passwordResetUnlockTime = threeHoursLater
                        onSmsRecoveryVerified?.invoke(sender, body)
                        continue
                    }
                }

                // 2. Check for Bank / UPI Credit SMS payment announcement
                val isPaymentSms = upperBody.contains("CREDITED") ||
                        upperBody.contains("RECEIVED") ||
                        upperBody.contains("PAID YOU") ||
                        upperBody.contains("UPI REF")
                if (isPaymentSms) {
                    val amount = PaymentNotificationListenerService.parsePaymentAmount(body)
                    if (amount != null && amount > 0.0) {
                        Log.i(TAG, "Extracted payment amount: ₹$amount from SMS: $sender")
                        val soundboxManager = PaymentSoundboxManager.getInstance(context)
                        soundboxManager.announcePayment(amount, "₹", "UPI SMS")
                        PaymentNotificationListenerService.onPaymentReceivedListener?.invoke(amount, body)
                    }
                }
            }
        }
    }
}
