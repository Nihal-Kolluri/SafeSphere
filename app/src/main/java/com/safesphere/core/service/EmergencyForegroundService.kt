package com.safesphere.core.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.safesphere.SafeSphereApp
import kotlinx.coroutines.*

/**
 * Foreground Service that keeps SafeSphere alive in the background during active emergencies,
 * managing location telemetry broadcast, wake lock, and notification actions.
 */
class EmergencyForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()

        val app = SafeSphereApp.instance
        val notification = app.notificationHelper.buildNotification(app.repository.emergencyState.value)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                ServiceNotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(ServiceNotificationHelper.NOTIFICATION_ID, notification)
        }

        // Listen for state changes to refresh notification
        serviceScope.launch {
            app.repository.emergencyState.collect { state ->
                if (state.isActive || state.isDuressActive) {
                    app.notificationHelper.updateNotification(state)
                } else {
                    stopSelf()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val app = SafeSphereApp.instance

        when (action) {
            ServiceNotificationHelper.ACTION_ESCALATE_NOW -> {
                app.repository.escalateImmediatelyToAuthorities()
            }
            ServiceNotificationHelper.ACTION_TOGGLE_POWER_SAVER -> {
                app.repository.toggleRescuePowerSaver()
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SafeSphere::EmergencyWakeLock"
        )?.apply {
            acquire(4 * 60 * 60 * 1000L) // 4 hours maximum timeout for rescue safety
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }
}
