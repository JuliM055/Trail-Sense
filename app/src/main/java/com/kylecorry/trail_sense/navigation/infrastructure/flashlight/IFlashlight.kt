package com.kylecorry.trail_sense.navigation.infrastructure.flashlight

import com.kylecorry.trail_sense.navigation.domain.FlashlightState

interface IFlashlight {
    fun on()
    fun off()
    fun sos()
    fun set(state: FlashlightState)
    fun getState(): FlashlightState
    fun getNextState(currentState: FlashlightState? = null): FlashlightState
    fun isAvailable(): Boolean
}