package com.redmik80.lightmeter.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LightSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _lux = MutableStateFlow(0f)
    val lux: StateFlow<Float> = _lux.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (!isListening && lightSensor != null) {
            sensorManager.registerListener(
                this,
                lightSensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isListening = true
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_LIGHT) {
                _lux.value = it.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun isLightSensorAvailable(): Boolean {
        return lightSensor != null
    }

    fun getSensorInfo(): String {
        return lightSensor?.let {
            "Name: ${it.name}\n" +
            "Vendor: ${it.vendor}\n" +
            "Max Range: ${it.maximumRange} lux\n" +
            "Resolution: ${it.resolution} lux\n" +
            "Power: ${it.power} mA"
        } ?: "Light sensor not available"
    }
}
