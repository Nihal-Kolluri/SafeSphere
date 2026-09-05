package com.safesphere.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.safesphere.data.model.EmergencyState
import com.safesphere.data.model.EscalationTier
import com.safesphere.ui.MainActivity

/**
 * Builds and manages the high-priority ongoing foreground notification for active emergencies.
 */
class ServiceNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "safesphere_emergency_channel"
        const val NOTIFICATION_ID = 9110

        const val ACTION_ESCALATE_NOW = "com.safesphere.action.ESCALATE_NOW"
        const val ACTION_TOGGLE_POWER_SAVER = "com.safesphere.action.TOGGLE_POWER_SAVER"
        const val ACTION_CANCEL_EMERGENCY = "com.safesphere.action.CANCEL_EMERGENCY"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeSphere Emergency Dispatch",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority rescue dispatch, live tracking, and tiered escalation alerts"
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(state: EmergencyState): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_EMERGENCY_ACTIVE", true)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Escalate Immediately
        val escalateIntent = Intent(context, EmergencyForegroundService::class.java).apply {
            action = ACTION_ESCALATE_NOW
        }
        val escalatePendingIntent = PendingIntent.getService(
            context,
            1,
            escalateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Toggle Power Saver
        val powerSaverIntent = Intent(context, EmergencyForegroundService::class.java).apply {
            action = ACTION_TOGGLE_POWER_SAVER
        }
        val powerSaverPendingIntent = PendingIntent.getService(
            context,
            2,
            powerSaverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tierText = when (state.activeTier) {
            EscalationTier.TIER_1_FAMILY -> "Step 1: Family Alerted (${state.tierRemainingSeconds}s to volunteers)"
            EscalationTier.TIER_2_VOLUNTEERS -> "Step 2: Volunteers Notified (${state.tierRemainingSeconds}s to 911/112)"
            EscalationTier.TIER_3_AUTHORITIES -> "Step 3: Authorities Dispatched (Police/Fire)"
        }

        val batteryText = state.latestTelemetry?.let { " • Bat: ${it.batteryPercentage}%" } ?: ""

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 SafeSphere Active Emergency")
            .setContentText("$tierText$batteryText")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)

        if (state.activeTier != EscalationTier.TIER_3_AUTHORITIES) {
            builder.addAction(
                android.R.drawable.ic_media_next,
                "Escalate Now",
                escalatePendingIntent
            )
        }

        val pwrLabel = if (state.isRescuePowerSaverActive) "Normal Mode" else "Power Saver"
        builder.addAction(
            android.R.drawable.ic_lock_power_off,
            pwrLabel,
            powerSaverPendingIntent
        )

        return builder.build()
    }

    fun updateNotification(state: EmergencyState) {
        if (state.isActive) {
            notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
        } else {
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }
}
