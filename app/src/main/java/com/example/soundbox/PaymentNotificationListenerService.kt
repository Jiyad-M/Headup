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

        // Promotional / advertisement keywords that must NEVER trigger payment alerts
        val PROMOTIONAL_KEYWORDS = listOf(
            "cashback", "offer", "discount", "save", "coupon", "voucher", "deal", "promo",
            "sale", "win", "earn", "reward", "scratch card", "flat", "upto", "up to",
            "pre-approved", "preapproved", "loan", "apply now", "apply for", "interest rate",
            "recharge", "bill due", "pay bill", "overdue", "reminder", "pending", "expire",
            "order placed", "order confirmed", "delivering", "shipped", "out for delivery",
            "movie ticket", "flight", "bus booking", "hotel", "gift card", "refer", "claim", "spin"
        )

        // Debit / outgoing payment keywords that must be ignored
        val DEBIT_KEYWORDS = listOf(
            "debited", "paid to", "sent to", "transferred to", "spent", "withdrawn", "paid at",
            "payment to", "deducted from"
        )

        // Verified positive incoming credit keywords
        val CREDIT_KEYWORDS = listOf(
            "paid you", "received", "credited with", "credited by", "credited to", "has been credited",
            "payment received", "received payment", "sent you", "accepted payment", "money received",
            "payment from"
        )

        /**
         * Validates whether a notification text represents a real incoming payment
         * and not an advertisement, discount promo, or debit.
         */
        fun isGenuinePaymentInflow(text: String): Boolean {
            val lower = text.lowercase()

            // 1. Strictly exclude debits
            if (DEBIT_KEYWORDS.any { lower.contains(it) }) {
                return false
            }

            // 2. Strictly exclude advertisements, marketing and promos
            if (PROMOTIONAL_KEYWORDS.any { lower.contains(it) }) {
                return false
            }

            // 3. Require explicit positive credit inflow keywords
            return CREDIT_KEYWORDS.any { lower.contains(it) }
        }

        /**
         * Extracts amount from notification text.
         * Only returns amount if text represents genuine incoming money.
         */
        fun parsePaymentAmount(text: String): Double? {
            // Must be genuine incoming payment, not an ad or outgoing debit
            if (!isGenuinePaymentInflow(text)) {
                return null
            }

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

            // Check if notification is genuine incoming payment and extract amount
            val amount = parsePaymentAmount(fullContent)
            if (amount != null && amount > 0) {
                // Determine app name for announcement (e.g. Google Pay, PhonePe, Paytm)
                val appName = when {
                    pkg.contains("paisa", ignoreCase = true) || fullContent.contains("gpay", ignoreCase = true) || fullContent.contains("google", ignoreCase = true) -> "Google Pay"
                    pkg.contains("phonepe", ignoreCase = true) || fullContent.contains("phonepe", ignoreCase = true) -> "PhonePe"
                    pkg.contains("paytm", ignoreCase = true) || fullContent.contains("paytm", ignoreCase = true) -> "Paytm"
                    pkg.contains("bhim", ignoreCase = true) || fullContent.contains("bhim", ignoreCase = true) -> "BHIM UPI"
                    pkg.contains("bharatpe", ignoreCase = true) -> "BharatPe"
                    else -> "UPI Payment"
                }

                Log.i(TAG, "Verified genuine payment inflow: ₹$amount from $appName!")

                // Call the shared SoundboxManager singleton so pre-warmed TTS announces immediately
                val soundboxManager = PaymentSoundboxManager.getInstance(applicationContext)
                soundboxManager.announcePayment(
                    amount = amount,
                    currencySymbol = "₹",
                    appName = appName
                )

                // Notify UI listeners
                onPaymentReceivedListener?.invoke(amount, fullContent)
            } else {
                Log.d(TAG, "Ignored non-payment / promotional notification from [$pkg]: $fullContent")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling payment notification", e)
        }
    }
}
