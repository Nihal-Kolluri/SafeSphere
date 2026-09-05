package com.safesphere.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects physical phone shake gestures to initiate emergency SOS.
 */
class ShakeDetector(
    context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastUpdateTime: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f

    private var shakeCount = 0
    private var lastShakeTimestamp: Long = 0

    companion object {
        private const val SHAKE_THRESHOLD = 800 // Acceleration threshold
        private const val TIME_THRESHOLD = 100 // Minimum time between checks (ms)
        private const val SHAKE_TIMEOUT = 1200 // Max time to register multiple shakes (ms)
        private const val REQUIRED_SHAKES = 3 // Shakes needed to confirm distress
    }

    fun start() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastUpdateTime

        if (timeDifference > TIME_THRESHOLD) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            val speed = (sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / timeDifference) * 10000

            if (speed > SHAKE_THRESHOLD) {
                if (currentTime - lastShakeTimestamp < SHAKE_TIMEOUT) {
                    shakeCount++
                    if (shakeCount >= REQUIRED_SHAKES) {
                        shakeCount = 0
                        onShakeDetected()
                    }
                } else {
                    shakeCount = 1
                }
                lastShakeTimestamp = currentTime
            }

            lastX = x
            lastY = y
            lastZ = z
            lastUpdateTime = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
