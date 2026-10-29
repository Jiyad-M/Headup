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
}
