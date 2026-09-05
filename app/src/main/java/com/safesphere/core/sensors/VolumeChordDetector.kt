package com.safesphere.core.sensors

import android.view.KeyEvent

/**
 * Detects rapid sequence of volume key events (e.g. 3 consecutive volume-down taps)
 * to trigger Silent / Discreet Emergency SOS.
 */
class VolumeChordDetector(
    private val onSilentSosTriggered: () -> Unit
) {
    private var pressCount = 0
    private var lastPressTime: Long = 0

    companion object {
        private const val MAX_INTERVAL_MS = 1500L // 1.5 seconds window
        private const val REQUIRED_PRESSES = 3
    }

    /**
     * Intercepts key down events from the Activity or accessibility window.
     * Returns true if the key event was consumed as part of an SOS sequence.
     */
    fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val now = System.currentTimeMillis()
            if (now - lastPressTime < MAX_INTERVAL_MS) {
                pressCount++
                if (pressCount >= REQUIRED_PRESSES) {
                    pressCount = 0
                    onSilentSosTriggered()
                    return true
                }
            } else {
                pressCount = 1
            }
            lastPressTime = now
        }
        return false
    }

    fun reset() {
        pressCount = 0
        lastPressTime = 0
    }
}
