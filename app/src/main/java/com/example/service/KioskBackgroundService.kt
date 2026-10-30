package com.example.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.apps.AppManager
import com.example.data.KioskPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KioskBackgroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var monitorJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var preferences: KioskPreferences
    private lateinit var appManager: AppManager

    companion object {
        private const val TAG = "KioskBgService"
        const val CHANNEL_ID = "kiosk_persistent_channel"
        const val NOTIFICATION_ID = 9901

        fun start(context: Context) {
            try {
                val intent = Intent(context, KioskBackgroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start KioskBackgroundService", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, KioskBackgroundService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferences = KioskPreferences(applicationContext)
        appManager = AppManager(applicationContext, preferences)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        acquireWakeLock()
        startAppRestrictionMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        releaseWakeLock()
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KioskLauncher:BgCpuLock")
            wakeLock?.setReferenceCounted(false)
            wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24 hours keep awake in background
        } catch (e: Exception) {
            Log.w(TAG, "Error acquiring wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    /**
     * Actively restricts unauthorized app usage when Kiosk is activated:
     * "App is active, restrict other app use. If activate, deny all access of other blacklist app. Anyway cant open"
     */
    private fun startAppRestrictionMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                try {
                    if (preferences.isActivated) {
                        val topPackage = getTopRunningPackage()
                        if (!topPackage.isNullOrEmpty() &&
                            topPackage != packageName &&
                            !isSystemEssentialPackage(topPackage)
                        ) {
                            if (!appManager.isPackageAllowed(topPackage)) {
                                Log.w(TAG, "Unauthorized app detected in foreground: $topPackage. Enforcing lockdown...")
                                bringKioskToFront(topPackage)
                            }
                        }
                    }
                } catch (_: Exception) {}
                delay(600) // Poll every 600ms
            }
        }
    }

    private fun isSystemEssentialPackage(pkg: String): Boolean {
        return pkg.startsWith("com.android.systemui") ||
                pkg == "android" ||
                pkg.contains("emergency") ||
                pkg.contains("telecom") ||
                pkg.contains("incallui")
    }

    private fun getTopRunningPackage(): String? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                val time = System.currentTimeMillis()
                val stats = usageStatsManager?.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 10,
                    time
                )
                if (!stats.isNullOrEmpty()) {
                    var recentStat = stats[0]
                    for (stat in stats) {
                        if (stat.lastTimeUsed > recentStat.lastTimeUsed) {
                            recentStat = stat
                        }
                    }
                    return recentStat.packageName
                }
            }
        } catch (_: Exception) {}

        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            @Suppress("DEPRECATION")
            val tasks = am?.getRunningTasks(1)
            if (!tasks.isNullOrEmpty()) {
                return tasks[0].topActivity?.packageName
            }
        } catch (_: Exception) {}

        return null
    }

    private fun bringKioskToFront(blockedPkg: String) {
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("BLOCKED_APP_PKG", blockedPkg)
        }
        startActivity(launchIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kiosk Launcher Background Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Kiosk security, power management and payment announcer running in background"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kiosk Launcher Protected")
            .setContentText("Background protection & Soundbox active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
