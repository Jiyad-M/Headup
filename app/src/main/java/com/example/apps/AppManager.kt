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

    /**
     * Validates whether an app package is permitted.
     * When kiosk is activated:
     * - Blacklisted apps are STRICTLY DENIED.
     * - Non-whitelisted apps are STRICTLY DENIED.
     */
    fun isPackageAllowed(packageName: String): Boolean {
        if (packageName == context.packageName) return true
        if (!preferences.isActivated) return true

        // Strict Blacklist Check: cannot open anyway
        if (preferences.blacklistedPackages.contains(packageName)) {
            return false
        }

        // Whitelist Check: only permitted apps can open
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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else false
        } catch (e: Exception) {
            Log.e("AppManager", "Failed to launch $packageName", e)
            false
        }
    }
}
