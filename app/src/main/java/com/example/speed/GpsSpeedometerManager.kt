package com.example.speed

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class GpsSpeedometerManager(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _speedKmH = MutableStateFlow(0)
    val speedKmH: StateFlow<Int> = _speedKmH.asStateFlow()

    private val _hasGpsFix = MutableStateFlow(false)
    val hasGpsFix: StateFlow<Boolean> = _hasGpsFix.asStateFlow()

    private val _gpsStatusText = MutableStateFlow("GPS Ready")
    val gpsStatusText: StateFlow<String> = _gpsStatusText.asStateFlow()

    private var lastLocation: Location? = null
    private var isTracking = false
    private var isSimulating = false

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        val lm = locationManager ?: run {
            _gpsStatusText.value = "GPS Unavailable"
            return
        }

        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) {
            _gpsStatusText.value = "GPS Permission Needed"
            return
        }

        try {
            val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                _gpsStatusText.value = "GPS Disabled in Settings"
                return
            }

            if (isGpsEnabled) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500L, // 500ms for responsive speedometer
                    0f,
                    this,
                    Looper.getMainLooper()
                )
            }

            if (isNetworkEnabled) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    0f,
                    this,
                    Looper.getMainLooper()
                )
            }

            // Check last known location
            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastGps?.let { onLocationChanged(it) }

            isTracking = true
            _gpsStatusText.value = if (isGpsEnabled) "GPS Active" else "Network Location"
        } catch (e: SecurityException) {
            _gpsStatusText.value = "GPS Permission Needed"
            Log.w("GpsSpeedometer", "Location permission not granted yet", e)
        } catch (e: Exception) {
            _gpsStatusText.value = "GPS Error"
            Log.e("GpsSpeedometer", "Error starting GPS", e)
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {}
        isTracking = false
        _hasGpsFix.value = false
    }

    override fun onLocationChanged(location: Location) {
        _hasGpsFix.value = true
        _gpsStatusText.value = "GPS Locked"

        var speedMps = 0f
        if (location.hasSpeed()) {
            speedMps = location.speed
        } else {
            val prev = lastLocation
            if (prev != null) {
                val timeDeltaSec = (location.time - prev.time) / 1000.0
                if (timeDeltaSec in 0.2..10.0) {
                    val distanceMeters = location.distanceTo(prev)
                    speedMps = (distanceMeters / timeDeltaSec).toFloat()
                }
            }
        }
        lastLocation = location

        val kmh = (speedMps * 3.6f).roundToInt().coerceIn(0, 220)
        _speedKmH.value = kmh
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {
        _gpsStatusText.value = "$provider Enabled"
    }

    override fun onProviderDisabled(provider: String) {
        _gpsStatusText.value = "$provider Disabled"
        if (provider == LocationManager.GPS_PROVIDER) {
            _hasGpsFix.value = false
        }
    }

    /**
     * Simulation toggle for test drives when vehicle is stationary or indoors
     */
    fun toggleSimulation() {
        isSimulating = !isSimulating
        if (isSimulating) {
            _hasGpsFix.value = true
            _gpsStatusText.value = "GPS Drive Simulated"
            simulateSpeedStep(0, 1)
        } else {
            _speedKmH.value = 0
            _gpsStatusText.value = "GPS Active"
        }
    }

    private fun simulateSpeedStep(current: Int, step: Int) {
        if (!isSimulating) return
        val next = (current + step).coerceIn(0, 120)
        _speedKmH.value = next
        val nextStep = when {
            next >= 85 -> -2
            next <= 10 -> 2
            else -> step
        }
        mainHandler.postDelayed({
            simulateSpeedStep(next, nextStep)
        }, 120)
    }
}
