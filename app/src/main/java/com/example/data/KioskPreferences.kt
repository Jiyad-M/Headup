package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PaymentRecord
import org.json.JSONArray
import org.json.JSONObject

class KioskPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kiosk_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN = "kiosk_8_digit_pin"
        private const val KEY_SECONDARY_PHONE = "kiosk_secondary_phone"
        private const val KEY_ALLOWED_PACKAGES = "kiosk_allowed_packages"
        private const val KEY_POWER_AUTO_DISPLAY = "kiosk_power_auto_display"
        private const val KEY_SLEEP_ON_UNPLUG = "kiosk_sleep_on_unplug"
        private const val KEY_CURRENCY = "kiosk_currency"
        private const val KEY_PAYMENT_PHRASE = "kiosk_payment_phrase"
        private const val KEY_PAYMENT_HISTORY = "kiosk_payment_history"
        private const val KEY_KIOSK_LOCKED = "kiosk_mode_locked"
        private const val KEY_HUD_THEME = "kiosk_hud_theme"
        private const val KEY_HUD_MIRRORED = "kiosk_hud_mirrored"
        const val DEFAULT_PIN = "12345678"
    }

    var pin: String
        get() = prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    var secondaryPhone: String
        get() = prefs.getString(KEY_SECONDARY_PHONE, "+1 555-0199") ?: "+1 555-0199"
        set(value) = prefs.edit().putString(KEY_SECONDARY_PHONE, value).apply()

    var allowedPackages: Set<String>
        get() = prefs.getStringSet(KEY_ALLOWED_PACKAGES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_ALLOWED_PACKAGES, value).apply()

    var autoDisplayOnPlugged: Boolean
        get() = prefs.getBoolean(KEY_POWER_AUTO_DISPLAY, true)
        set(value) = prefs.edit().putBoolean(KEY_POWER_AUTO_DISPLAY, value).apply()

    var sleepOnUnplugged: Boolean
        get() = prefs.getBoolean(KEY_SLEEP_ON_UNPLUG, true)
        set(value) = prefs.edit().putBoolean(KEY_SLEEP_ON_UNPLUG, value).apply()

    var currency: String
        get() = prefs.getString(KEY_CURRENCY, "$") ?: "$"
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var paymentPhrase: String
        get() = prefs.getString(KEY_PAYMENT_PHRASE, "{Tone} {amount} received") ?: "{Tone} {amount} received"
        set(value) = prefs.edit().putString(KEY_PAYMENT_PHRASE, value).apply()

    var isKioskLocked: Boolean
        get() = prefs.getBoolean(KEY_KIOSK_LOCKED, true)
        set(value) = prefs.edit().putBoolean(KEY_KIOSK_LOCKED, value).apply()

    var hudTheme: String
        get() = prefs.getString(KEY_HUD_THEME, "TESLA") ?: "TESLA"
        set(value) = prefs.edit().putString(KEY_HUD_THEME, value).apply()

    var isHudMirrored: Boolean
        get() = prefs.getBoolean(KEY_HUD_MIRRORED, false)
        set(value) = prefs.edit().putBoolean(KEY_HUD_MIRRORED, value).apply()

    fun getPaymentHistory(): List<PaymentRecord> {
        val json = prefs.getString(KEY_PAYMENT_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<PaymentRecord>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PaymentRecord(
                        id = obj.getString("id"),
                        amount = obj.getDouble("amount"),
                        currency = obj.getString("currency"),
                        timestamp = obj.getLong("timestamp"),
                        formattedTime = obj.getString("formattedTime"),
                        announcementText = obj.getString("announcementText")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun addPaymentRecord(record: PaymentRecord) {
        val current = getPaymentHistory().toMutableList()
        current.add(0, record) // newest first
        if (current.size > 50) {
            current.removeAt(current.lastIndex)
        }
        val array = JSONArray()
        for (item in current) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("amount", item.amount)
            obj.put("currency", item.currency)
            obj.put("timestamp", item.timestamp)
            obj.put("formattedTime", item.formattedTime)
            obj.put("announcementText", item.announcementText)
            array.put(obj)
        }
        prefs.edit().putString(KEY_PAYMENT_HISTORY, array.toString()).apply()
    }
}
