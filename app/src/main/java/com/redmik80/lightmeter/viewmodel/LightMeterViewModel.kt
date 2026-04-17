package com.redmik80.lightmeter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.redmik80.lightmeter.sensor.LightSensorManager
import com.redmik80.lightmeter.util.ExposureCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LightMeterState(
    val lux: Float = 0f,
    val ev: Float = 0f,
    val iso: Int = 100,
    val aperture: Float = 2.8f,
    val shutterSpeed: Double = 0.0,
    val measurementMode: MeasurementMode = MeasurementMode.LIGHT_SENSOR,
    val calibrationOffset: Float = 0f,
    val isCalibrating: Boolean = false,
    val calibrationLux: Float = 0f,
    val calibrationEV: Float = 0f
)

enum class MeasurementMode {
    LIGHT_SENSOR,
    CAMERA
}

class LightMeterViewModel(application: Application) : AndroidViewModel(application) {

    private val lightSensorManager = LightSensorManager(application)

    private val _state = MutableStateFlow(LightMeterState())
    val state: StateFlow<LightMeterState> = _state.asStateFlow()

    private val prefs = application.getSharedPreferences("light_meter_prefs", Application.MODE_PRIVATE)

    init {
        loadCalibration()
        startSensorUpdates()
    }

    private fun loadCalibration() {
        val offset = prefs.getFloat("calibration_offset", 0f)
        _state.value = _state.value.copy(calibrationOffset = offset)
    }

    private fun saveCalibration(offset: Float) {
        prefs.edit().putFloat("calibration_offset", offset).apply()
        _state.value = _state.value.copy(calibrationOffset = offset)
    }

    private fun startSensorUpdates() {
        viewModelScope.launch {
            lightSensorManager.lux.collect { lux ->
                updateLux(lux)
            }
        }
        lightSensorManager.startListening()
    }

    private fun updateLux(lux: Float) {
        val calibratedLux = lux * kotlin.math.pow(2f, _state.value.calibrationOffset)
        val ev = ExposureCalculator.calculateEV(calibratedLux)
        val shutterSpeed = ExposureCalculator.calculateShutterSpeed(
            ev = ev,
            iso = _state.value.iso,
            aperture = _state.value.aperture
        )
        _state.value = _state.value.copy(
            lux = lux,
            ev = ev,
            shutterSpeed = shutterSpeed
        )
    }

    fun setISO(iso: Int) {
        val shutterSpeed = ExposureCalculator.calculateShutterSpeed(
            ev = _state.value.ev,
            iso = iso,
            aperture = _state.value.aperture
        )
        _state.value = _state.value.copy(iso = iso, shutterSpeed = shutterSpeed)
    }

    fun setAperture(aperture: Float) {
        val shutterSpeed = ExposureCalculator.calculateShutterSpeed(
            ev = _state.value.ev,
            iso = _state.value.iso,
            aperture = aperture
        )
        _state.value = _state.value.copy(aperture = aperture, shutterSpeed = shutterSpeed)
    }

    fun setShutterSpeed(shutterSpeed: Double) {
        val ev = ExposureCalculator.calculateEVFromShutter(
            shutterSpeed = shutterSpeed,
            iso = _state.value.iso,
            aperture = _state.value.aperture
        )
        _state.value = _state.value.copy(shutterSpeed = shutterSpeed, ev = ev)
    }

    fun setMeasurementMode(mode: MeasurementMode) {
        _state.value = _state.value.copy(measurementMode = mode)
    }

    fun updateEV(ev: Float) {
        val shutterSpeed = ExposureCalculator.calculateShutterSpeed(
            ev = ev,
            iso = _state.value.iso,
            aperture = _state.value.aperture
        )
        _state.value = _state.value.copy(ev = ev, shutterSpeed = shutterSpeed)
    }

    fun updateLuxDirect(lux: Float) {
        val calibratedLux = lux * kotlin.math.pow(2f, _state.value.calibrationOffset)
        val ev = ExposureCalculator.calculateEV(calibratedLux)
        val shutterSpeed = ExposureCalculator.calculateShutterSpeed(
            ev = ev,
            iso = _state.value.iso,
            aperture = _state.value.aperture
        )
        _state.value = _state.value.copy(
            lux = lux,
            ev = ev,
            shutterSpeed = shutterSpeed
        )
    }

    fun startCalibration() {
        _state.value = _state.value.copy(
            isCalibrating = true,
            calibrationLux = _state.value.lux,
            calibrationEV = _state.value.ev
        )
    }

    fun finishCalibration(referenceLux: Float?) {
        if (referenceLux != null && referenceLux > 0 && _state.value.calibrationLux > 0) {
            val offset = kotlin.math.log2(referenceLux / _state.value.calibrationLux)
            saveCalibration(offset)
        }
        _state.value = _state.value.copy(isCalibrating = false)
    }

    fun cancelCalibration() {
        _state.value = _state.value.copy(isCalibrating = false)
    }

    fun resetCalibration() {
        saveCalibration(0f)
    }

    override fun onCleared() {
        super.onCleared()
        lightSensorManager.stopListening()
    }
}
