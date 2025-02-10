package com.corps.healthmate.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthSensorManager @Inject constructor(
    private val context: Context
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking

    private val _isPhoneInUse = MutableStateFlow(false)
    val isPhoneInUse: StateFlow<Boolean> = _isPhoneInUse

    private var lastStepCount = 0
    private var lastUpdateTime = 0L
    private val MOVEMENT_THRESHOLD = 1.0f
    private val PHONE_USE_THRESHOLD = 2.0f

    init {
        registerSensors()
    }

    private fun registerSensors() {
        stepSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        gyroscope?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> handleStepCount(event)
            Sensor.TYPE_ACCELEROMETER -> detectWalking(event)
            Sensor.TYPE_GYROSCOPE -> detectPhoneUse(event)
        }
    }

    private fun handleStepCount(event: SensorEvent) {
        val steps = event.values[0].toInt()
        if (lastStepCount == 0) {
            lastStepCount = steps
        }
        val stepsDelta = steps - lastStepCount
        _stepCount.value += stepsDelta
        lastStepCount = steps
    }

    private fun detectWalking(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime > 100) { // Check every 100ms
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())
            _isWalking.value = acceleration > MOVEMENT_THRESHOLD
            lastUpdateTime = currentTime
        }
    }

    private fun detectPhoneUse(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val rotation = Math.sqrt((x * x + y * y + z * z).toDouble())
        _isPhoneInUse.value = rotation > PHONE_USE_THRESHOLD
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }
}
