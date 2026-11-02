package com.example.soundbox

import android.app.Notification
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
            "com.whatsapp",                           // WhatsApp Pay
            "com.fampay.in",                          // FamPay
            "com.axis.mobile",                        // Axis Mobile
            "com.hdfcbank.payzapp",                   // HDFC PayZapp
            "com.icicibank.pockets"                   // ICICI Pockets
        )

        /**
         * Extracts amount from notification text.
         * Matches:
         * "SURANYA G paid you ₹110.00" -> 110.0
         * "SHIJIMOL NELLENGARA paid you ₹182.00 Aysha" -> 182.0
         * "TANAY SARKAR paid you ₹68.00" -> 68.0
         * "Received ₹500.00 on Paytm" -> 500.0
         * "A/C credited with Rs 150.00 by UPI" -> 150.0
         */
        fun parsePaymentAmount(text: String): Double? {
            // Pattern 1: ₹ or Rs. or INR followed by number (e.g. ₹110.00, Rs 500, INR 1,200)
            val regex = Pattern.compile("(?:[₹]|Rs\\.?|INR)\\s*([0-9]+(?:[,.][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
            val matcher = regex.matcher(text)
            if (matcher.find()) {
                val numStr = matcher.group(1)?.replace(",", "")
                val amt = numStr?.toDoubleOrNull()
                if (amt != null && amt > 0.0) return amt
            }

            // Pattern 2: "paid you 110" or "received 110" or "credited with 110"
            val fallbackRegex = Pattern.compile("(?:paid you|received|credited with|credited by|payment of)\\s*(?:[₹]|Rs\\.?|INR)?\\s*([0-9]+(?:[,.][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
            val fallbackMatcher = fallbackRegex.matcher(text)
            if (fallbackMatcher.find()) {
                val numStr = fallbackMatcher.group(1)?.replace(",", "")
                val amt = numStr?.toDoubleOrNull()
                if (amt != null && amt > 0.0) return amt
            }

            // Pattern 3: Number followed by rupees / INR (e.g. "150 rupees received")
            val postfixRegex = Pattern.compile("([0-9]+(?:[,.][0-9]{1,2})?)\\s*(?:rupees|rs|inr)", Pattern.CASE_INSENSITIVE)
            val postfixMatcher = postfixRegex.matcher(text)
            if (postfixMatcher.find()) {
                val numStr = postfixMatcher.group(1)?.replace(",", "")
                val amt = numStr?.toDoubleOrNull()
                if (amt != null && amt > 0.0) return amt
            }

            return null
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.i(TAG, "Notification Listener connected successfully.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.w(TAG, "Notification Listener disconnected.")
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

            // Determine app name for announcement (e.g. Google Pay, PhonePe, Paytm)
            val appName = when {
                pkg.contains("paisa", ignoreCase = true) || fullContent.contains("gpay", ignoreCase = true) || fullContent.contains("google", ignoreCase = true) -> "Google Pay"
                pkg.contains("phonepe", ignoreCase = true) || fullContent.contains("phonepe", ignoreCase = true) -> "PhonePe"
                pkg.contains("paytm", ignoreCase = true) || fullContent.contains("paytm", ignoreCase = true) -> "Paytm"
                pkg.contains("bhim", ignoreCase = true) || fullContent.contains("bhim", ignoreCase = true) -> "BHIM UPI"
                pkg.contains("bharatpe", ignoreCase = true) -> "BharatPe"
                else -> "UPI Payment"
            }

            // Check if notification contains payment indicators
            val isPaymentApp = PAYMENT_PACKAGES.contains(pkg)
            val hasPaymentKeywords = fullContent.contains("paid you", ignoreCase = true) ||
                    fullContent.contains("received", ignoreCase = true) ||
                    fullContent.contains("credited", ignoreCase = true) ||
                    fullContent.contains("payment from", ignoreCase = true) ||
                    fullContent.contains("upi ref", ignoreCase = true) ||
                    fullContent.contains("₹")

            if (isPaymentApp || hasPaymentKeywords) {
                val amount = parsePaymentAmount(fullContent)
                if (amount != null && amount > 0) {
                    Log.i(TAG, "Extracted payment amount: ₹$amount from $appName!")

                    // Call the shared SoundboxManager singleton so pre-warmed TTS announces immediately
                    val soundboxManager = PaymentSoundboxManager.getInstance(applicationContext)
                    soundboxManager.announcePayment(
                        amount = amount,
                        currencySymbol = "₹",
                        appName = appName
                    )

                    // Notify UI listeners
                    onPaymentReceivedListener?.invoke(amount, fullContent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling payment notification", e)
        }
    }
}
