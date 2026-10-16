package com.example.data.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * Device sensor listener detecting shake gestures via the device accelerometer.
 *
 * When the user shakes their phone with sufficient acceleration force, triggers [onShakeListener].
 */
class ShakeDetector(
    private val onShakeListener: () -> Unit
) : SensorEventListener {

    companion object {
        private const val TAG = "ShakeDetector"

        // Acceleration threshold in Gs required to trigger a shake event
        private const val SHAKE_THRESHOLD_GRAVITY = 2.5f

        // Minimum milliseconds between consecutive shake triggers to avoid spam
        private const val SHAKE_COOLDOWN_MS = 1400L
    }

    private var lastShakeTimestamp: Long = 0
    private var isRegistered: Boolean = false

    fun start(sensorManager: SensorManager?): Boolean {
        if (sensorManager == null) return false
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return false

        isRegistered = sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        Log.d(TAG, "ShakeDetector registered: $isRegistered")
        return isRegistered
    }

    fun stop(sensorManager: SensorManager?) {
        if (!isRegistered || sensorManager == null) return
        try {
            sensorManager.unregisterListener(this)
            isRegistered = false
            Log.d(TAG, "ShakeDetector unregistered")
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering ShakeDetector: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Compute acceleration magnitude normalized to g-force
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // Net gForce including gravity vector
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp >= SHAKE_COOLDOWN_MS) {
                lastShakeTimestamp = now
                Log.d(TAG, "Shake gesture detected! gForce = $gForce")
                onShakeListener()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for shake detection
    }

    /**
     * Allows manual or programmatic trigger of the shake event (ideal for emulators
     * or automated testing where physical acceleration cannot be applied).
     */
    fun simulateShake() {
        val now = System.currentTimeMillis()
        if (now - lastShakeTimestamp >= 500L) {
            lastShakeTimestamp = now
            onShakeListener()
        }
    }
}
