package com.kylecorry.trail_sense.astronomy.domain.sun

// Ported from AOSP's TwilightCalculate file
/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.text.format.DateUtils
import com.kylecorry.trail_sense.shared.domain.Coordinate
import com.kylecorry.trail_sense.shared.math.sinDegrees
import com.kylecorry.trail_sense.shared.toEpochMillis
import com.kylecorry.trail_sense.shared.toZonedDateTime
import java.time.*
import kotlin.math.*

abstract class BaseSunTimesCalculator(private val sunAngleDeg: Double) :
    ISunTimesCalculator {

    override fun calculate(coordinate: Coordinate, date: LocalDate): SunTimes {
        val current = date.atTime(12, 0, 0).toEpochMillis()

        val daysSince2000 = (current - UTC_2000) / DateUtils.DAY_IN_MILLIS.toDouble() //Duration.between(JANUARY_1_2000, current).toDays()
        val meanAnomaly = 6.240059968f + daysSince2000 * 0.01720197f
        val trueAnomaly =
            meanAnomaly + C1 * sin(meanAnomaly) + C2 * sin((2 * meanAnomaly)) + C3 * sin(
                (3 * meanAnomaly)
            )
        // ecliptic longitude
        val solarLng = trueAnomaly + 1.796593063 + Math.PI
        // solar transit in days since 2000
        val arcLongitude = -coordinate.longitude / 360
        val n = round(daysSince2000 - J0 - arcLongitude)
        val solarTransitJ2000 =
            (n + J0 + arcLongitude + 0.0053 * sin(meanAnomaly)
                    + -0.0069 * sin(2 * solarLng))
        // declination of sun
        val solarDec = asin(sin(solarLng) * sin(OBLIQUITY))
        val latRad = Math.toRadians(coordinate.latitude)
        val cosHourAngle =
            (sinDegrees(sunAngleDeg) - sin(latRad) * sin(
                solarDec
            )) / (cos(latRad) * cos(solarDec))
        // The day or night never ends for the given date and location, if this value is out of
        // range.
        // TODO: Handle when sun rises / sets but not both
        if (cosHourAngle >= 1) {
            return SunTimes.alwaysDown()
        } else if (cosHourAngle <= -1) {
            return SunTimes.alwaysUp()
        }
        val hourAngle = acos(cosHourAngle) / (2 * Math.PI)
        val up = Instant.ofEpochMilli(
            ((solarTransitJ2000 - hourAngle) * DateUtils.DAY_IN_MILLIS).roundToLong() + UTC_2000
        )
        val down = Instant.ofEpochMilli(
            ((solarTransitJ2000 + hourAngle) * DateUtils.DAY_IN_MILLIS).roundToLong() + UTC_2000
        )

        val upTime = up.toZonedDateTime().toLocalDateTime()
        val downTime = down.toZonedDateTime().toLocalDateTime()

        return SunTimes(upTime, downTime)
    }

    companion object {
        private const val J0 = 0.0009
        private const val C1 = 0.0334196
        private const val C2 = 0.000349066
        private const val C3 = 0.000005236
        private const val OBLIQUITY = 0.40927971
        private val UTC_2000 = 946728000000L
        private val JANUARY_1_2000 = LocalDateTime.of(2000, Month.JANUARY, 1, 12, 0)
    }

}