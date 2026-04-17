package com.redmik80.lightmeter.util

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

object ExposureCalculator {

    fun calculateEV(lux: Float): Float {
        if (lux <= 0f) return 0f
        return log2(lux / 2.5f)
    }

    fun luxToEV(lux: Float): Float {
        return calculateEV(lux)
    }

    fun evToLux(ev: Float): Float {
        return 2.5f * 2f.pow(ev)
    }

    fun calculateShutterSpeed(ev: Float, iso: Int, aperture: Float): Double {
        if (aperture <= 0f || iso <= 0) return 0.0

        val ev100 = ev + log2(iso / 100f)
        val shutterSpeed = (aperture * aperture) / 2f.pow(ev100)

        return shutterSpeed
    }

    fun calculateEVFromShutter(shutterSpeed: Double, iso: Int, aperture: Float): Float {
        if (shutterSpeed <= 0 || aperture <= 0f || iso <= 0) return 0f

        val ev100 = log2((aperture * aperture) / shutterSpeed)
        return ev100 - log2(iso / 100f)
    }

    fun calculateAperture(ev: Float, iso: Int, shutterSpeed: Double): Float {
        if (shutterSpeed <= 0 || iso <= 0) return 0f

        val ev100 = ev + log2(iso / 100f)
        return kotlin.math.sqrt(shutterSpeed * 2f.pow(ev100)).toFloat()
    }

    fun calculateISO(ev: Float, aperture: Float, shutterSpeed: Double): Int {
        if (aperture <= 0f || shutterSpeed <= 0) return 100

        val ev100 = log2((aperture * aperture) / shutterSpeed)
        val iso = 100 * 2f.pow(ev100 - ev)
        return iso.roundToInt()
    }

    fun formatShutterSpeed(shutterSpeed: Double): String {
        if (shutterSpeed <= 0) return "N/A"

        return when {
            shutterSpeed >= 1.0 -> "${shutterSpeed.roundToInt()}s"
            shutterSpeed >= 0.5 -> "1/${(1 / shutterSpeed).roundToInt()}"
            shutterSpeed >= 0.125 -> "1/${(1 / shutterSpeed).roundToInt()}"
            shutterSpeed >= 0.001 -> "1/${(1 / shutterSpeed).roundToInt()}"
            shutterSpeed >= 0.0001 -> "1/${(10000 / shutterSpeed).roundToInt()}"
            else -> "1/10000+"
        }
    }

    fun getApertureValues(): List<Float> {
        return listOf(
            1.0f, 1.2f, 1.4f, 1.8f, 2.0f, 2.2f, 2.5f, 2.8f,
            3.2f, 3.5f, 4.0f, 4.5f, 5.0f, 5.6f, 6.3f, 7.1f,
            8.0f, 9.0f, 10f, 11f, 13f, 14f, 16f, 18f, 20f, 22f
        )
    }

    fun getISOValues(): List<Int> {
        return listOf(
            25, 50, 100, 200, 400, 800, 1600, 3200, 6400, 12800
        )
    }

    fun getShutterSpeedValues(): List<Double> {
        return listOf(
            30.0, 15.0, 8.0, 4.0, 2.0, 1.0,
            0.5, 0.25, 0.125, 0.0625,
            0.03333333, 0.01666667, 0.00833333,
            0.00416667, 0.00208333, 0.00104167,
            0.00052083, 0.00026042, 0.00013021,
            0.00006510, 0.00003255, 0.00001628
        )
    }

    fun formatEV(ev: Float): String {
        return String.format("%.1f", ev)
    }

    fun formatLux(lux: Float): String {
        return when {
            lux >= 10000 -> String.format("%.0f lx", lux)
            lux >= 1000 -> String.format("%.1f lx", lux)
            lux >= 100 -> String.format("%.1f lx", lux)
            lux >= 10 -> String.format("%.2f lx", lux)
            else -> String.format("%.3f lx", lux)
        }
    }
}
