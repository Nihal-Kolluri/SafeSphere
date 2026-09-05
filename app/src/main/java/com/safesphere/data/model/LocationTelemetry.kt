package com.safesphere.data.model

/**
 * Encapsulates real-time telemetry captured during an emergency.
 */
data class LocationTelemetry(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0.0f,
    val speedMps: Float = 0.0f,
    val altitudeMeters: Double = 0.0,
    val bearingDegrees: Float = 0.0f,
    val batteryPercentage: Int = 100,
    val isCharging: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    /**
     * Converts coordinates to a standard Google Maps link.
     */
    fun toGoogleMapsUrl(): String =
        "https://maps.google.com/?q=$latitude,$longitude"

    /**
     * Generates a link to the SafeSphere Web Live Tracking Portal.
     */
    fun toLiveTrackingPortalUrl(incidentId: String): String =
        "https://safesphere.app/track/$incidentId?lat=$latitude&lng=$longitude&bat=$batteryPercentage"
}
