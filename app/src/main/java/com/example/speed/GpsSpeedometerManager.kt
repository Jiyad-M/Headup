package com.example.speed

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class GpsSpeedometerManager(private val context: Context) : LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _speedKmH = MutableStateFlow(0)
    val speedKmH: StateFlow<Int> = _speedKmH.asStateFlow()

    private val _hasGpsFix = MutableStateFlow(false)
    val hasGpsFix: StateFlow<Boolean> = _hasGpsFix.asStateFlow()

    private val _gpsStatusText = MutableStateFlow("GPS Ready")
    val gpsStatusText: StateFlow<String> = _gpsStatusText.asStateFlow()

    private var lastLocation: Location? = null
    private var isTracking = false
    private var smoothedSpeedKmH = 0f

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        val lm = locationManager ?: run {
            _gpsStatusText.value = "GPS Hardware Unavailable"
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

            // Real-time GPS provider (500ms intervals, 0m distance for live speedometer)
            if (isGpsEnabled) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500L,
                    0f,
                    this,
                    Looper.getMainLooper()
                )
            }

            // Network provider fallback
            if (isNetworkEnabled) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    0f,
                    this,
                    Looper.getMainLooper()
                )
            }

            // Check initial last known location
            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastGps?.let { onLocationChanged(it) }

            isTracking = true
            _gpsStatusText.value = if (isGpsEnabled) "GPS Searching..." else "Network Location"
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
        _speedKmH.value = 0
    }

    override fun onLocationChanged(location: Location) {
        _hasGpsFix.value = true
        _gpsStatusText.value = "GPS Locked"

        var speedMps = 0f
        if (location.hasSpeed() && location.speed >= 0f) {
            speedMps = location.speed
        } else {
            val prev = lastLocation
            if (prev != null) {
                val timeDeltaSec = (location.time - prev.time) / 1000.0
                if (timeDeltaSec in 0.3..10.0) {
                    val distanceMeters = location.distanceTo(prev)
                    // Discard unrealistic GPS jumps > 250 km/h
                    val computedSpeed = (distanceMeters / timeDeltaSec).toFloat()
                    if (computedSpeed < 70f) { // ~250 km/h
                        speedMps = computedSpeed
                    }
                }
            }
        }
        lastLocation = location

        val rawKmh = (speedMps * 3.6f).coerceIn(0f, 220f)
        
        // Exponential smoothing for steady real-time needle movement without noise
        smoothedSpeedKmH = if (rawKmh < 1.5f) {
            0f // Dead-band filter for stationary vehicle
        } else {
            smoothedSpeedKmH * 0.4f + rawKmh * 0.6f
        }

        _speedKmH.value = smoothedSpeedKmH.roundToInt().coerceIn(0, 220)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {
        _gpsStatusText.value = "$provider Enabled"
        startTracking()
    }

    override fun onProviderDisabled(provider: String) {
        _gpsStatusText.value = "$provider Disabled"
        if (provider == LocationManager.GPS_PROVIDER) {
            _hasGpsFix.value = false
            _speedKmH.value = 0
        }
    }
}
