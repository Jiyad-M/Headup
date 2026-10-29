package com.example.soundbox

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.KioskPreferences
import java.util.regex.Pattern

class PaymentNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "PaymentNotification"
        var instance: PaymentNotificationListenerService? = null
        var onPaymentReceivedListener: ((amount: Double, rawText: String) -> Unit)? = null

        // Known UPI and banking payment app packages in India
        val PAYMENT_PACKAGES = setOf(
            "com.google.android.apps.nbu.paisa.user", // Google Pay
            "com.phonepe.app",                       // PhonePe
            "net.one97.paytm",                        // Paytm
            "in.org.npci.upiapp",                     // BHIM UPI
            "com.bharatpe.app",                      // BharatPe
            "com.cred.club",                          // CRED
            "com.sbi.upi",                            // Yono SBI
            "com.whatsapp"                            // WhatsApp Pay
        )

        /**
         * Extracts amount from notification text.
         * Matches:
         * "SURANYA G paid you ₹110.00" -> 110.0
         * "Received ₹182.00 from..." -> 182.0
         * "TANAY SARKAR paid you ₹68.00" -> 68.0
         * "₹500 received" -> 500.0
         */
        fun parsePaymentAmount(text: String): Double? {
            // Pattern for ₹ or Rs. or INR followed by number
            val regex = Pattern.compile("(?:[₹]|Rs\\.?|INR)\\s*([0-9]+(?:[,.][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
            val matcher = regex.matcher(text)
            if (matcher.find()) {
                val numStr = matcher.group(1)?.replace(",", "")
                return numStr?.toDoubleOrNull()
            }

            // Pattern for "paid you 110" or "received 110"
            val fallbackRegex = Pattern.compile("(?:paid you|received|credited with)\\s*([0-9]+(?:[,.][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
            val fallbackMatcher = fallbackRegex.matcher(text)
            if (fallbackMatcher.find()) {
                val numStr = fallbackMatcher.group(1)?.replace(",", "")
                return numStr?.toDoubleOrNull()
            }

            return null
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "Notification Listener connected successfully.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "Notification Listener disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        try {
            val pkg = sbn.packageName ?: ""
            val extras = sbn.notification.extras ?: return

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val fullContent = "$title $text $bigText"

            Log.d(TAG, "Notification received from [$pkg]: $fullContent")

            // Check if notification contains payment indicators
            val isPaymentApp = PAYMENT_PACKAGES.contains(pkg)
            val hasPaymentKeywords = fullContent.contains("paid you", ignoreCase = true) ||
                    fullContent.contains("received", ignoreCase = true) ||
                    fullContent.contains("credited", ignoreCase = true) ||
                    fullContent.contains("payment from", ignoreCase = true)

            if (isPaymentApp || hasPaymentKeywords) {
                val amount = parsePaymentAmount(fullContent)
                if (amount != null && amount > 0) {
                    Log.i(TAG, "Extracted payment amount: ₹$amount from notification!")
                    
                    // Announce payment without customer name: chime beep first, then speech
                    val prefs = KioskPreferences(applicationContext)
                    val soundboxManager = PaymentSoundboxManager(applicationContext, prefs)
                    soundboxManager.announcePayment(amount, "₹")

                    // Notify UI if listening
                    onPaymentReceivedListener?.invoke(amount, fullContent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling payment notification", e)
        }
    }
}
