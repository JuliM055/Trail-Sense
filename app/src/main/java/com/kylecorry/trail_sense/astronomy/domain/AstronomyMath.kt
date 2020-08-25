package com.kylecorry.trail_sense.astronomy.domain

import com.kylecorry.trail_sense.astronomy.domain.moon.JulianDayCalculator
import com.kylecorry.trail_sense.astronomy.domain.sun.SunTimes
import com.kylecorry.trail_sense.shared.domain.Coordinate
import com.kylecorry.trail_sense.shared.math.cosDegrees
import com.kylecorry.trail_sense.shared.math.sinDegrees
import com.kylecorry.trail_sense.shared.math.toRadians
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object AstronomyMath {

    /**
     * Converts an hour to an angle in radians
     */
    fun timeToAngle(hours: Number, minutes: Number, seconds: Number): Double {
        val decimalTime = hours.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
        val angle = decimalTime * 15
        return angle.toRadians()
    }

    /**
     * Converts an angle to between 0 and 360 degrees
     */
    fun reduceAngleDegrees(angle: Double): Double {
        return wrap(angle, 0.0, 360.0)
    }

    fun canInterpolate(y1: Double, y2: Double, y3: Double, threshold: Double): Boolean {
        val a = y2 - y1
        val b = y3 - y2
        return abs(b - a) < threshold
    }

    fun interpolate(n: Double, y1: Double, y2: Double, y3: Double): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return y2 + (n / 2.0) * (a + b + n * c)
    }

    fun interpolateExtremum(y1: Double, y2: Double, y3: Double): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return y2 - square(a + b) / (8 * c)
    }

    fun interpolateExtremumX(y1: Double, y2: Double, y3: Double): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        return -(a + b) / (2 * c)
    }

    fun interpolateZeroCrossing(y1: Double, y2: Double, y3: Double): Double {
        val a = y2 - y1
        val b = y3 - y2
        val c = b - a

        val negligibleThreshold = 1e-12
        val iterations = 20

        var estimate = 0.0

        for (i in 0 until iterations) {
            val lastEstimate = estimate

            val gradient =
                -(2 * y2 + estimate * (a + b + c * estimate)) / (a + b + 2 * c * estimate)
            estimate += gradient

            if (abs(estimate - lastEstimate) <= negligibleThreshold) {
                break
            }
        }

        return estimate
    }


    fun siderealTime(julianDate: Double, hours: Double = 0.0): Double {
        val T = (julianDate - 2451545.0) / 36525.0
        val theta0 = 100.46061837 + 36000.770053608 * T + 0.000387933 * square(T) + cube(T) / 38710000.0
        // TODO: Add delta T
        return theta0
    }

    fun riseSetTransitTimes(coordinate: Coordinate, date: LocalDate, standardAltitude: Double, declination: Double, rightAscension: Double): Pair<Double, Double> {
        val siderealTime = siderealTime(JulianDayCalculator.calculate(date.atTime(12, 0))) // TODO: Double check this
        val lngDeg = coordinate.longitude
        val cosH = (sinDegrees(standardAltitude) - sinDegrees(coordinate.latitude) * sinDegrees(declination)) / (cosDegrees(coordinate.latitude) * cosDegrees(declination))

        if (cosH >= 1) {
            //return SunTimes.alwaysDown()
        } else if (cosH <= -1) {
//            return SunTimes.alwaysUp()
        }

        val H = acos(cosH)

        val m0 = wrap((rightAscension + lngDeg - siderealTime) / 360.0, 0.0, 1.0)
        var m1 = wrap(m0 - H / 360, 0.0, 1.0)
        var m2 = wrap(m0 + H / 360, 0.0, 1.0)

        val iterations = 20
        val negligibleThreshold = 1e-12

        val thetaTransit = siderealTime + 360.985647 * m0
        var thetaRise = siderealTime + 360.985647 * m1
        var thetaSet = siderealTime + 360.985647 * m2

        for (i in 0 until iterations){
            val deltaM1 = (thetaRise - lngDeg - standardAltitude) / 360
            val deltaM2 = (thetaSet - lngDeg - standardAltitude) / 360

            m1 -= deltaM1
            m2 -= deltaM2

            m1 =  wrap(m1, 0.0, 1.0)
            m2 =  wrap(m2, 0.0, 1.0)

            thetaRise = siderealTime + 360.985647 * m1
            thetaSet = siderealTime + 360.985647 * m2

            if (abs(deltaM1) < negligibleThreshold && abs(deltaM2) < negligibleThreshold){
                break
            }
        }

        // TODO: Return transit also
        return Pair(m1 * 24, m2 * 24)
    }

    private fun wrap(value: Double, min: Double, max: Double): Double {
        val range = max - min

        var newValue = value

        while (newValue > max){
            newValue -= range
        }

        while (newValue < min){
            newValue += range
        }

        return newValue
    }

    private fun cube(a: Double): Double {
        return a * a * a
    }

    private fun square(a: Double): Double {
        return a * a
    }


}