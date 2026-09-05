package com.safesphere.core.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors the real-time device battery percentage and power profile.
 */
class BatteryMonitor(private val context: Context) {

    data class BatteryInfo(
        val percentage: Int,
        val isCharging: Boolean,
        val isLowPower: Boolean
    )

    private val _batteryState = MutableStateFlow(getCurrentBatteryInfo())
    val batteryState: StateFlow<BatteryInfo> = _batteryState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            _batteryState.value = getCurrentBatteryInfo()
        }
    }

    fun startListening() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: IllegalArgumentException) {
            // Already unregistered
        }
    }

    /**
     * Inspects current battery status from sticky broadcast.
     */
    fun getCurrentBatteryInfo(): BatteryInfo {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 100

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        return BatteryInfo(
            percentage = percent,
            isCharging = isCharging,
            isLowPower = percent <= 20
        )
    }
}
