package com.example.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.example.data.KioskPreferences
import com.example.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManager(
    private val context: Context,
    private val preferences: KioskPreferences
) {

    /**
     * Loads all launchable apps on the device.
     */
    suspend fun loadAllInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveList = pm.queryIntentActivities(intent, 0)
        val selfPkg = context.packageName
        val allowedSet = preferences.allowedPackages
        val blacklistSet = preferences.blacklistedPackages

        val apps = mutableListOf<AppItem>()
        for (resolveInfo in resolveList) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == selfPkg) continue // skip kiosk launcher from child app list

            val name = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            val isBlacklisted = blacklistSet.contains(pkg)
            val isAllowed = allowedSet.contains(pkg) && !isBlacklisted

            apps.add(
                AppItem(
                    packageName = pkg,
                    name = name,
                    isAllowed = isAllowed,
                    isBlacklisted = isBlacklisted,
                    isSystem = isSystem,
                    icon = icon
                )
            )
        }
        apps.sortedBy { it.name.lowercase() }
    }

    /**
     * Returns only the permitted (whitelisted and non-blacklisted) applications.
     */
    suspend fun getPermittedApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val all = loadAllInstalledApps()
        val allowedSet = preferences.allowedPackages
        val blacklistSet = preferences.blacklistedPackages

        // If allowed set is empty on first boot, initialize with default useful apps
        if (allowedSet.isEmpty()) {
            val defaultAllowed = all.filter {
                (it.packageName.contains("maps") ||
                it.packageName.contains("uber") ||
                it.packageName.contains("dialer") ||
                it.packageName.contains("phone") ||
                it.packageName.contains("chrome") ||
                it.packageName.contains("browser") ||
                it.packageName.contains("calculator") ||
                it.packageName.contains("clock") ||
                it.packageName.contains("camera") ||
                !it.isSystem) && !blacklistSet.contains(it.packageName)
            }.take(8).map { it.packageName }.toSet()

            if (defaultAllowed.isNotEmpty()) {
                preferences.allowedPackages = defaultAllowed
                return@withContext all.filter { defaultAllowed.contains(it.packageName) && !blacklistSet.contains(it.packageName) }
            }
        }
        all.filter { allowedSet.contains(it.packageName) && !blacklistSet.contains(it.packageName) }
    }

    companion object {
        private val temporaryAuthorizedPackages = java.util.concurrent.ConcurrentHashMap<String, Long>()
        private val temporaryAuthorizedTimestamps = java.util.concurrent.ConcurrentHashMap<String, Long>()

        /**
         * Authorizes an app temporarily after 8-digit password entry.
         * Default duration is 15 minutes of uninterrupted use.
         */
        fun authorizeTemporarily(packageName: String, durationMillis: Long = 15 * 60 * 1000L) {
            val now = System.currentTimeMillis()
            temporaryAuthorizedPackages[packageName] = now + durationMillis
            temporaryAuthorizedTimestamps[packageName] = now
        }

        /**
         * Checks if an app is currently authorized via password match.
         */
        fun isTemporarilyAuthorized(packageName: String): Boolean {
            val expiry = temporaryAuthorizedPackages[packageName] ?: return false
            if (System.currentTimeMillis() < expiry) {
                return true
            } else {
                temporaryAuthorizedPackages.remove(packageName)
                temporaryAuthorizedTimestamps.remove(packageName)
                return false
            }
        }

        /**
         * Clears temporary password authorizations.
         * Grace period (default 10s) ensures an app that was JUST authorized will not be wiped
         * by transient onResume transitions while switching to the unlocked app.
         */
        fun clearTemporaryAuthorizations(minimumAgeMillis: Long = 10_000L) {
            val now = System.currentTimeMillis()
            val iterator = temporaryAuthorizedPackages.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val authorizedAt = temporaryAuthorizedTimestamps[entry.key] ?: 0L
                if (now - authorizedAt > minimumAgeMillis) {
                    iterator.remove()
                    temporaryAuthorizedTimestamps.remove(entry.key)
                }
            }
        }
    }

    /**
     * Instance delegation for backward compatibility
     */
    fun authorizeTemporarily(packageName: String, durationMillis: Long = 15 * 60 * 1000L) {
        Companion.authorizeTemporarily(packageName, durationMillis)
    }

    fun clearTemporaryAuthorizations(minimumAgeMillis: Long = 10_000L) {
        Companion.clearTemporaryAuthorizations(minimumAgeMillis)
    }

    /**
     * Set of core Android system and framework packages that must NEVER be intercepted
     * so that Android services, dialogs, keyboards, settings, and phone calls function without interruption.
     */
    val essentialSystemPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.samsung.android.settings",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.feedback",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.samsung.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.phone",
        "com.android.server.telecom",
        "com.samsung.android.incallui",
        "com.android.incallui",
        "com.android.certinstaller",
        "com.android.keyguard",
        "com.sec.android.app.launcher",
        "com.sec.android.app.myfiles",
        "com.google.android.documentsui",
        context.packageName
    )

    /**
     * Checks if a package is an essential Android system service or component that should never be locked.
     */
    fun isEssentialSystemPackage(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        if (packageName == context.packageName) return true
        if (essentialSystemPackages.contains(packageName)) return true
        if (packageName.startsWith("com.android.inputmethod") ||
            packageName.startsWith("com.google.android.inputmethod") ||
            packageName.startsWith("com.samsung.android.honeyboard") ||
            packageName.contains(".ime") ||
            packageName.contains("inputmethod") ||
            packageName.contains("keyboard") ||
            packageName.contains("permissioncontroller")
        ) {
            return true
        }
        return false
    }

    /**
     * Determines whether a package is a normal user-facing app (i.e. has a launcher activity icon).
     * Background services, internal frameworks, and headless providers return false.
     */
    fun isNormalUserApp(packageName: String): Boolean {
        if (isEssentialSystemPackage(packageName)) return false
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            launchIntent != null
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Determines if a package should be intercepted by App Lock.
     * ONLY returns true if:
     * 1. Kiosk is activated
     * 2. It is a NORMAL USER APP (has a launch intent, not a system service or IME)
     * 3. It is NOT currently allowed (toggle is OFF in Admin Settings)
     * 4. It is NOT temporarily password-authorized
     */
    fun shouldInterceptApp(packageName: String): Boolean {
        if (!preferences.isActivated) return false
        if (isEssentialSystemPackage(packageName)) return false
        if (!isNormalUserApp(packageName)) return false
        return !isPackageAllowed(packageName)
    }

    /**
     * Validates whether an app package is permitted.
     * When kiosk is activated:
     * - Temporarily password-unlocked apps are ALLOWED.
     * - Blacklisted apps are STRICTLY DENIED unless temporarily password-unlocked.
     * - Non-whitelisted apps are STRICTLY DENIED unless temporarily password-unlocked.
     */
    fun isPackageAllowed(packageName: String): Boolean {
        if (packageName == context.packageName) return true
        if (isEssentialSystemPackage(packageName)) return true
        if (!preferences.isActivated) return true

        // 1. Password-unlocked temporary authorization
        if (Companion.isTemporarilyAuthorized(packageName)) {
            return true
        }

        // 2. Strict Blacklist Check: cannot open anyway
        if (preferences.blacklistedPackages.contains(packageName)) {
            return false
        }

        // 3. Whitelist Check: only permitted apps can open
        return preferences.allowedPackages.contains(packageName)
    }

    fun toggleAppAllowed(packageName: String, isAllowed: Boolean) {
        val currentAllowed = preferences.allowedPackages.toMutableSet()
        val currentBlacklist = preferences.blacklistedPackages.toMutableSet()
        if (isAllowed) {
            currentAllowed.add(packageName)
            currentBlacklist.remove(packageName) // remove from blacklist if allowed
        } else {
            currentAllowed.remove(packageName)
            Companion.temporaryAuthorizedPackages.remove(packageName) // immediately revoke any temporary pass
            Companion.temporaryAuthorizedTimestamps.remove(packageName)
        }
        preferences.allowedPackages = currentAllowed
        preferences.blacklistedPackages = currentBlacklist
    }

    fun toggleAppBlacklisted(packageName: String, isBlacklisted: Boolean) {
        val currentAllowed = preferences.allowedPackages.toMutableSet()
        val currentBlacklist = preferences.blacklistedPackages.toMutableSet()
        if (isBlacklisted) {
            currentBlacklist.add(packageName)
            currentAllowed.remove(packageName) // cannot be allowed if blacklisted
        } else {
            currentBlacklist.remove(packageName)
        }
        preferences.blacklistedPackages = currentBlacklist
        preferences.allowedPackages = currentAllowed
    }

    /**
     * Programmatically launches the app, enforcing strict Kiosk denial
     * if the app is blacklisted or unapproved.
     */
    fun launchApp(packageName: String): Boolean {
        if (!isPackageAllowed(packageName)) {
            Log.w("AppManager", "ACCESS DENIED: $packageName is blocked by Kiosk security policy.")
            return false
        }

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                preferences.lastOpenedPackage = packageName
                true
            } else false
        } catch (e: Exception) {
            Log.e("AppManager", "Failed to launch $packageName", e)
            false
        }
    }

    /**
     * Directly launches an app once authorized via 8-digit password.
     */
    fun launchAppDirectly(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                preferences.lastOpenedPackage = packageName
                true
            } else {
                Log.w("AppManager", "No launch intent found for $packageName")
                false
            }
        } catch (e: Exception) {
            Log.e("AppManager", "Failed to launch directly $packageName", e)
            false
        }
    }
}
