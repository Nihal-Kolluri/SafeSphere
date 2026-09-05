package com.safesphere.core.battery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager

/**
 * Manages in-app Rescue Power Saver parameters and OS Battery Saver shortcuts.
 */
class PowerSaverController(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    /**
     * Checks whether the device is currently in system-wide battery saver mode.
     */
    fun isSystemPowerSaveMode(): Boolean {
        return powerManager?.isPowerSaveMode ?: false
    }

    /**
     * Creates an Intent to prompt the user to enable system OS Battery Saver.
     */
    fun createSystemBatterySaverIntent(): Intent {
        return Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Dims the window brightness to minimum (0.01f) on AMOLED/OLED screens to save max battery.
     */
    fun applyRescueBrightness(activity: Activity, enabled: Boolean) {
        val layoutParams = activity.window.attributes
        if (enabled) {
            layoutParams.screenBrightness = 0.01f
        } else {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        activity.window.attributes = layoutParams
    }
}
