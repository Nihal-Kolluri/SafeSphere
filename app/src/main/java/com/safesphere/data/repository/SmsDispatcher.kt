package com.safesphere.data.repository

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.safesphere.data.model.LocationTelemetry

/**
 * Handles cellular SMS transmission to emergency contacts and authorities,
 * with fallback formatting and simulation support.
 */
class SmsDispatcher(private val context: Context) {

    companion object {
        private const val TAG = "SmsDispatcher"
    }

    /**
     * Builds standard emergency SMS message text.
     */
    fun buildEmergencyMessage(
        userName: String,
        incidentId: String,
        telemetry: LocationTelemetry?,
        tierName: String
    ): String {
        val lat = telemetry?.latitude ?: 0.0
        val lng = telemetry?.longitude ?: 0.0
        val bat = telemetry?.batteryPercentage ?: 100
        val mapsLink = "https://maps.google.com/?q=$lat,$lng"
        val livePortal = "https://safesphere.app/track/$incidentId?lat=$lat&lng=$lng&bat=$bat"

        return "🚨 [SafeSphere SOS - $tierName]\n" +
                "$userName triggered an emergency alert!\n" +
                "Battery: $bat%\n" +
                "Location: $lat, $lng\n" +
                "Google Maps: $mapsLink\n" +
                "Live Rescue Track: $livePortal"
    }

    /**
     * Dispatches SMS to a recipient. If in simulation mode, only logs the event without sending.
     */
    fun sendEmergencySms(
        phoneNumber: String,
        message: String,
        isSimulated: Boolean = false
    ): Boolean {
        if (isSimulated) {
            Log.d(TAG, "[SIMULATED SMS] To: $phoneNumber | Content: $message")
            return true
        }

        return try {
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
                ?: @Suppress("DEPRECATION") SmsManager.getDefault()

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
            Log.i(TAG, "Successfully dispatched emergency SMS to $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send emergency SMS to $phoneNumber: ${e.message}", e)
            false
        }
    }
}
