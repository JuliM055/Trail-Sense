package com.kylecorry.trail_sense.astronomy.domain

import org.junit.Test

import org.junit.Assert.*
import java.time.Duration

class AstronomyMathTest {

    @Test
    fun reduceAngleDegrees() {
        assertEquals(0.0, AstronomyMath.reduceAngleDegrees(0.0), 0.0)
        assertEquals(180.0, AstronomyMath.reduceAngleDegrees(180.0), 0.0)
        assertEquals(0.0, AstronomyMath.reduceAngleDegrees(0.0), 0.0)
        assertEquals(1.0, AstronomyMath.reduceAngleDegrees(361.0), 0.0)
        assertEquals(359.0, AstronomyMath.reduceAngleDegrees(-1.0), 0.0)
        assertEquals(180.0, AstronomyMath.reduceAngleDegrees(-180.0), 0.0)
        assertEquals(360.0, AstronomyMath.reduceAngleDegrees(720.0), 0.0)
    }

    @Test
    fun hourToAngle(){
        assertEquals(2.4213389045, AstronomyMath.timeToAngle(9, 14, 55.8), 0.00000001)
    }

    @Test
    fun interpolate(){
        assertEquals(0.876125, AstronomyMath.interpolate(0.18125, 0.884226, 0.877366, 0.870531), 0.0000005)
    }

    @Test
    fun interpolateExtremum(){
        assertEquals(1.3812030, AstronomyMath.interpolateExtremum(1.3814294, 1.3812213, 1.3812453), 0.0000005)
    }

    @Test
    fun interpolateExtremumX(){
        assertEquals(0.3966, AstronomyMath.interpolateExtremumX(1.3814294, 1.3812213, 1.3812453), 0.00005)
    }

    @Test
    fun interpolateZeroCrossing(){
        assertEquals(-0.20127, AstronomyMath.interpolateZeroCrossing(-1693.4, 406.3, 2303.2), 0.000005)
    }

    @Test
    fun canInterpolate(){
        assertTrue(AstronomyMath.canInterpolate(-1693.4, 406.3, 2303.2, 203.0))
        assertFalse(AstronomyMath.canInterpolate(-1693.4, 406.3, 2303.2, 201.0))
    }
}