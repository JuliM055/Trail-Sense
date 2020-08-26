package com.kylecorry.trail_sense.shared.math

import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.Temporal

fun getPercentOfDuration(start: Temporal, end: Temporal, current: Temporal): Float {
    val duration = Duration.between(start, end).seconds
    val elapsed = Duration.between(start, current).seconds
    return elapsed / duration.toFloat()
}

fun LocalDateTime.plusHours(hours: Double): LocalDateTime {
    val h = hours.toLong()
    val m = ((hours % 1) * 60).toLong()
    val s = (hours * 60) % 1
    val ns = (1e9 * s).toLong()
    return this.plusHours(h).plusMinutes(m).plusNanos(ns)
}

fun LocalDateTime.plusSeconds(seconds: Double): LocalDateTime {
    val ns = (1e9 * seconds).toLong()
    return this.plusNanos(ns)
}