package com.kylecorry.trail_sense.weather.altimeter

import com.kylecorry.trail_sense.shared.AltitudeReading
import com.kylecorry.trail_sense.shared.PressureAltitudeReading

class GPSAltitudeCalculator : IAltitudeCalculator {
    override fun convert(readings: List<PressureAltitudeReading>): List<AltitudeReading> {
        return readings.map {
            AltitudeReading(
                it.time,
                it.altitude
            )
        }
    }
}