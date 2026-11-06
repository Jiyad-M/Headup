package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.apps.AppManager
import com.example.data.KioskPreferences
import com.example.ui.AppLockOverlayActivity

/**
 * Real-time System-Wide App Lock Accessibility Service.
 * Intercepts unapproved applications opened from:
 * - Multi-window & Split-Screen panes
 * - Samsung DeX floating windows
 * - Pop-up view / PIP
 * - Recent apps switcher
 * - System intents launched from allowed apps (e.g. Map -> Gallery/Browser)
 */
class KioskAppLockAccessibilityService : AccessibilityService() {

    private lateinit var preferences: KioskPreferences
    private lateinit var appManager: AppManager
    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0L

    companion object {
        private const val TAG = "AppLockAccessibility"
        var isServiceRunning = false
            private set

        /**
         * Checks whether KioskAppLockAccessibilityService is enabled in Android Settings.
         */
        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, KioskAppLockAccessibilityService::class.java)
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)
            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                val enabledComponent = ComponentName.unflattenFromString(componentNameString)
                if (enabledComponent != null && enabledComponent == expectedComponentName) {
                    return true
                }
            }
            return false
        }

        /**
         * Direct intent to open Android Accessibility settings.
         */
        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open accessibility settings", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferences = KioskPreferences(applicationContext)
        appManager = AppManager(applicationContext, preferences)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.i(TAG, "KioskAppLockAccessibilityService connected and actively protecting apps.")

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        info.notificationTimeout = 50
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return

        // 1. Never intercept essential system services, framework components, keyboards, or settings
        if (appManager.isEssentialSystemPackage(pkgName)) return

        // 2. Never intercept if temporarily password-authorized
        if (AppManager.isTemporarilyAuthorized(pkgName)) return

        // 3. Only intercept normal user-facing apps (with launcher icon) that are currently blocked
        if (!appManager.shouldInterceptApp(pkgName)) return

        val now = System.currentTimeMillis()
        // Debounce rapid window changes for the same package within 1000ms
        if (pkgName == lastInterceptedPackage && (now - lastInterceptTime) < 1000L) {
            return
        }
        lastInterceptedPackage = pkgName
        lastInterceptTime = now

        Log.w(TAG, "SYSTEM LOCK INTERCEPTION: $pkgName is blocked! Launching AppLockOverlayActivity.")

        // Immediately launch the full-screen 8-digit PIN lock overlay over this unapproved app
        val lockIntent = Intent(this, AppLockOverlayActivity::class.java).apply {
            putExtra(AppLockOverlayActivity.EXTRA_LOCKED_PACKAGE, pkgName)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NO_ANIMATION or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )
        }
        startActivity(lockIntent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "KioskAppLockAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
