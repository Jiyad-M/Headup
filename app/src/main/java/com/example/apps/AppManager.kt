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

enum class SplitLaunchResult {
    SUCCESS_BOTH,
    MAPS_ONLY_UBER_MISSING,
    UBER_ONLY_MAPS_MISSING,
    NEITHER_INSTALLED
}

class AppManager(
    private val context: Context,
    private val preferences: KioskPreferences
) {
    companion object {
        const val PKG_UBER_DRIVER = "com.ubercab.driver"
        const val PKG_GOOGLE_MAPS = "com.google.android.apps.maps"
    }

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

        val apps = mutableListOf<AppItem>()
        for (resolveInfo in resolveList) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == selfPkg) continue // skip kiosk launcher from child app list

            val name = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            apps.add(
                AppItem(
                    packageName = pkg,
                    name = name,
                    isAllowed = allowedSet.contains(pkg),
                    isSystem = isSystem,
                    icon = icon
                )
            )
        }
        apps.sortedBy { it.name.lowercase() }
    }

    /**
     * Returns only the permitted (whitelisted) applications.
     */
    suspend fun getPermittedApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val all = loadAllInstalledApps()
        val allowedSet = preferences.allowedPackages
        // If allowed set is empty on first boot, initialize with default useful apps
        if (allowedSet.isEmpty()) {
            val defaultAllowed = all.filter {
                it.packageName.contains("maps") ||
                it.packageName.contains("uber") ||
                it.packageName.contains("dialer") ||
                it.packageName.contains("phone") ||
                it.packageName.contains("chrome") ||
                it.packageName.contains("browser") ||
                it.packageName.contains("calculator") ||
                it.packageName.contains("clock") ||
                it.packageName.contains("camera") ||
                !it.isSystem
            }.take(8).map { it.packageName }.toSet()

            if (defaultAllowed.isNotEmpty()) {
                preferences.allowedPackages = defaultAllowed
                return@withContext all.filter { defaultAllowed.contains(it.packageName) }
            }
        }
        all.filter { allowedSet.contains(it.packageName) }
    }

    fun toggleAppAllowed(packageName: String, isAllowed: Boolean) {
        val current = preferences.allowedPackages.toMutableSet()
        if (isAllowed) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        preferences.allowedPackages = current
    }

    fun launchApp(packageName: String): Boolean {
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

    /**
     * Programmatically launches both the Uber Driver app and Google Maps into
     * split-screen mode simultaneously using FLAG_ACTIVITY_LAUNCH_ADJACENT.
     */
    fun launchSplitScreenUberAndMaps(): SplitLaunchResult {
        val pm = context.packageManager
        val uberIntent = pm.getLaunchIntentForPackage(PKG_UBER_DRIVER)
        val mapsIntent = pm.getLaunchIntentForPackage(PKG_GOOGLE_MAPS)

        val splitFlags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK

        return when {
            uberIntent != null && mapsIntent != null -> {
                uberIntent.addFlags(splitFlags)
                context.startActivity(uberIntent)

                mapsIntent.addFlags(splitFlags)
                context.startActivity(mapsIntent)
                SplitLaunchResult.SUCCESS_BOTH
            }
            mapsIntent != null -> {
                mapsIntent.addFlags(splitFlags)
                context.startActivity(mapsIntent)
                SplitLaunchResult.MAPS_ONLY_UBER_MISSING
            }
            uberIntent != null -> {
                uberIntent.addFlags(splitFlags)
                context.startActivity(uberIntent)
                SplitLaunchResult.UBER_ONLY_MAPS_MISSING
            }
            else -> {
                SplitLaunchResult.NEITHER_INSTALLED
            }
        }
    }

    fun openPlayStoreForUberDriver() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PKG_UBER_DRIVER")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$PKG_UBER_DRIVER")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun openMapsNavigationFallback() {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {}
    }
}
