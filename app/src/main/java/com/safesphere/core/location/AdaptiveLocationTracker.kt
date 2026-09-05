package com.safesphere.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.safesphere.data.model.LocationTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Battery-aware location manager that dynamically calibrates GPS polling
 * based on remaining device battery percentage and motion state.
 */
class AdaptiveLocationTracker(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentTelemetry = MutableStateFlow<LocationTelemetry?>(null)
    val currentTelemetry: StateFlow<LocationTelemetry?> = _currentTelemetry.asStateFlow()

    private var isTracking = false
    private var currentIntervalMs: Long = 5000L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            updateTelemetryFromLocation(location)
        }
    }

    /**
     * Starts adaptive tracking calibrated to current battery percentage.
     */
    @SuppressLint("MissingPermission")
    fun startTracking(batteryPercentage: Int, isCharging: Boolean) {
        if (isTracking) return
        isTracking = true

        val request = buildLocationRequestForBattery(batteryPercentage, isCharging)
        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                if (lastLoc != null) {
                    updateTelemetryFromLocation(lastLoc, batteryPercentage, isCharging)
                }
            }
        } catch (e: SecurityException) {
            // Handled when permissions are missing
        }
    }

    /**
     * Dynamically updates polling frequency if battery level drops during the rescue.
     */
    @SuppressLint("MissingPermission")
    fun updateBatteryProfile(batteryPercentage: Int, isCharging: Boolean) {
        if (!isTracking) return

        val newRequest = buildLocationRequestForBattery(batteryPercentage, isCharging)
        if (newRequest.intervalMillis != currentIntervalMs) {
            currentIntervalMs = newRequest.intervalMillis
            try {
                fusedClient.removeLocationUpdates(locationCallback)
                fusedClient.requestLocationUpdates(newRequest, locationCallback, Looper.getMainLooper())
            } catch (_: SecurityException) {}
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (_: SecurityException) {}
    }

    /**
     * Builds calibrated LocationRequest according to battery life and movement strategy.
     */
    fun buildLocationRequestForBattery(batteryPercentage: Int, isCharging: Boolean): LocationRequest {
        val (intervalMs, minUpdateIntervalMs, priority) = when {
            isCharging || batteryPercentage > 50 -> {
                // High precision continuous tracking (3-5s)
                Triple(5000L, 3000L, Priority.PRIORITY_HIGH_ACCURACY)
            }
            batteryPercentage in 15..50 -> {
                // Dynamic adaptive burst tracking (15s moving, 60s idle)
                Triple(15000L, 10000L, Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            }
            else -> {
                // Critical Ultra-Survival power mode (<15% battery)
                Triple(120000L, 60000L, Priority.PRIORITY_LOW_POWER)
            }
        }
        currentIntervalMs = intervalMs

        return LocationRequest.Builder(priority, intervalMs).apply {
            setMinUpdateIntervalMillis(minUpdateIntervalMs)
            setWaitForAccurateLocation(batteryPercentage > 50)
        }.build()
    }

    private fun updateTelemetryFromLocation(
        location: Location,
        batteryPercent: Int = 100,
        isCharging: Boolean = false
    ) {
        _currentTelemetry.value = LocationTelemetry(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            speedMps = location.speed,
            altitudeMeters = location.altitude,
            bearingDegrees = location.bearing,
            batteryPercentage = batteryPercent,
            isCharging = isCharging,
            timestampMillis = location.time.takeIf { it > 0 } ?: System.currentTimeMillis()
        )
    }

    /**
     * For manual injection of telemetry in Simulation / Test mode.
     */
    fun injectSimulatedLocation(telemetry: LocationTelemetry) {
        _currentTelemetry.value = telemetry
    }
}
