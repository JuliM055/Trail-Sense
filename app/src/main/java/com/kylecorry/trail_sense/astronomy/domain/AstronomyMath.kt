package com.kylecorry.trail_sense.astronomy.domain

import com.kylecorry.trail_sense.astronomy.domain.moon.JulianDayCalculator
import com.kylecorry.trail_sense.shared.domain.Coordinate
import com.kylecorry.trail_sense.shared.math.cosDegrees
import com.kylecorry.trail_sense.shared.math.sinDegrees
import com.kylecorry.trail_sense.shared.math.tanDegrees
import com.kylecorry.trail_sense.shared.toZonedDateTime
import java.time.*
import kotlin.math.*

object AstronomyMath {

    fun timeToAngle(hours: Number, minutes: Number, seconds: Number): Double {
        val decimalTime = hours.toDouble() + minutes.toDouble() / 60.0 + seconds.toDouble() / 3600.0
        return decimalTime * 15
    }

    fun degreesToHours(degrees: Double): Double {
        return degrees / 15.0
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


    fun meanSiderealTime(julianDate: Double): Double {
        val T = (julianDate - 2451545.0) / 36525.0
        val theta0 = 280.46061837 + 360.98564736629 * (julianDate - 2451545.0) + 0.000387933 * square(T) - cube(T) / 38710000.0
        return wrap(theta0 , 0.0, 360.0)
    }

    fun apparentSiderealTime(julianDate: Double, longitudeNutation: Double, eclipticObliquity: Double): Double {
        val meanSidereal = meanSiderealTime(julianDate)
        // TODO Add apparent sidereal time correction
        return meanSidereal + (longitudeNutation * cosDegrees(eclipticObliquity)) / 15.0
    }

    fun convertToUniversalTime(time: ZonedDateTime): LocalDateTime {
        val utcTime = time.withZoneSameInstant(ZoneId.of("UTC"))
        return utcTime.plusHours(12).toLocalDateTime()
    }

    fun utToLocal(time: LocalDateTime, zone: ZoneId): ZonedDateTime {
        val utc = time.minusHours(12)
        val zoned = ZonedDateTime.of(utc, ZoneId.of("UTC"))
        return zoned.withZoneSameInstant(zone)
    }

    fun ttToLocal(time: LocalDateTime, zone: ZoneId): ZonedDateTime {
        val diffSeconds = getDifferenceBetweenTTAndUT(time.year)
        val utc = time.minusHours(12).minusSeconds(diffSeconds.roundToLong())
        val zoned = ZonedDateTime.of(utc, ZoneId.of("UTC"))
        return zoned.withZoneSameInstant(zone)
    }

    fun convertToTerrestrialTime(time: ZonedDateTime): LocalDateTime {
        val universalTime = convertToUniversalTime(time)
        val diffSeconds = getDifferenceBetweenTTAndUT(universalTime.year)
        return universalTime.plusSeconds(diffSeconds.roundToLong())

    }

    fun getDifferenceBetweenTTAndUT(year: Int): Double {
        val t = (year - 2000) / 100.0
        return 102 + 102 * square(t) + 25.3 * cube(t) + 0.37 * (year - 2100)
    }

    fun hourAngle(sidereal: Double, longitude: Double, rightAscension: Double): Double {
        return sidereal + longitude - rightAscension
    }

    fun hourToDuration(hours: Double): Duration {
        return Duration
            .ofHours(hours.toLong())
            .plusMinutes(((hours % 1) * 60).toLong())
            .plusSeconds(((hours * 60) % 1).toLong())
    }

    fun hourToLocalTime(hours: Double): LocalTime {
        return LocalTime.of(hours.toInt(), ((hours % 1) * 60).toInt(), ((hours * 60) % 1).toInt())
    }

    fun azimuth(hourAngle: Double, latitude: Double, declination: Double): Double {
        return atan2(sinDegrees(hourAngle), cosDegrees(hourAngle) * sinDegrees(latitude) - tanDegrees(declination) * cosDegrees(latitude))
    }

    fun altitude(hourAngle: Double, latitude: Double, declination: Double): Double {
        return sinDegrees(latitude) * sinDegrees(declination) + cosDegrees(latitude) * sinDegrees(declination) * cosDegrees(hourAngle)
    }

    fun riseSetTransitTimes(coordinate: Coordinate, time: ZonedDateTime, standardAltitude: Double, declination: Double, rightAscension: Double): AstronomyRiseSetTransitTimes {
        val ut = convertToUniversalTime(time).toLocalDate().atStartOfDay()
        val siderealTime = apparentSiderealTime(JulianDayCalculator.calculate(ut), 0.0, 0.0) // TODO: Double check this
        val cosH = (sinDegrees(standardAltitude) - sinDegrees(coordinate.latitude) * sinDegrees(declination)) / (cosDegrees(coordinate.latitude) * cosDegrees(declination))

        if (cosH >= 1) {
            //return SunTimes.alwaysDown()
        } else if (cosH <= -1) {
//            return SunTimes.alwaysUp()
        }

        val H = wrap(Math.toDegrees(acos(cosH)), 0.0, 180.0)

        var m0 = wrap((rightAscension - coordinate.longitude - siderealTime) / 360.0, 0.0, 1.0)
        var m1 = wrap(m0 - H / 360, 0.0, 1.0)
        var m2 = wrap(m0 + H / 360, 0.0, 1.0)

        var set = utToLocal(ut.plus(hourToDuration(m2 * 24)), time.zone)

        if (set.toLocalDate().isBefore(time.toLocalDate())){
            m2 += 1.0
        }

        val iterations = 1
        val negligibleThreshold = 1e-12

        var thetaTransit = reduceAngleDegrees(siderealTime + 360.985647 * m0)
        var thetaRise = reduceAngleDegrees(siderealTime + 360.985647 * m1)
        var thetaSet = reduceAngleDegrees(siderealTime + 360.985647 * m2)

        for (i in 0 until iterations){
            val transitHourAngle = wrap(hourAngle(thetaTransit, coordinate.longitude, rightAscension), -180.0, 180.0)
            val deltaM0 = transitHourAngle / 360
            val riseHourAngle = wrap(hourAngle(thetaRise, coordinate.longitude, rightAscension), -180.0, 180.0)
            val setHourAngle = wrap(hourAngle(thetaSet, coordinate.longitude, rightAscension), -180.0, 180.0)
            val deltaM1 = (altitude(riseHourAngle, coordinate.latitude, declination) - standardAltitude) / (360 * cosDegrees(declination) * cosDegrees(coordinate.latitude) * sinDegrees(riseHourAngle))
            val deltaM2 = (altitude(setHourAngle, coordinate.latitude, declination) - standardAltitude) / (360 * cosDegrees(declination) * cosDegrees(coordinate.latitude) * sinDegrees(setHourAngle))

            m0 -= deltaM0
            m1 -= deltaM1
            m2 -= deltaM2

            m0 = wrap(m0, 0.0, 1.0)
            m1 =  wrap(m1, 0.0, 1.0)
            m2 =  wrap(m2, 0.0, 1.0)

            set = utToLocal(ut.plus(hourToDuration(m2 * 24)), time.zone)

            if (set.toLocalDate() < time.toLocalDate()){
                m2 += 1.0
            }

            thetaTransit = siderealTime + 360.985647 * m0
            thetaRise = siderealTime + 360.985647 * m1
            thetaSet = siderealTime + 360.985647 * m2

            if (abs(deltaM1) < negligibleThreshold && abs(deltaM2) < negligibleThreshold){
                break
            }
        }

        val riseHour = m1 * 24
        val transitHour = m0 * 24
        val setHour = m2 * 24

        var rise = utToLocal(ut.plus(hourToDuration(riseHour)), time.zone)
        var transit = utToLocal(ut.plus(hourToDuration(transitHour)), time.zone)
        set = utToLocal(ut.plus(hourToDuration(setHour)), time.zone)

        return AstronomyRiseSetTransitTimes(rise, set, transit)
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

data class AstronomyRiseSetTransitTimes(val rise: ZonedDateTime, val set: ZonedDateTime, val transit: ZonedDateTime)