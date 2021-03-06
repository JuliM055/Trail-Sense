package com.kylecorry.trail_sense.navigation.infrastructure.share

import com.kylecorry.trail_sense.shared.domain.Coordinate

interface ILocationSender {
    fun send(location: Coordinate)
}