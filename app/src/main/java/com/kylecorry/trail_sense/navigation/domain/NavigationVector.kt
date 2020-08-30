package com.kylecorry.trail_sense.navigation.domain

import com.kylecorry.trail_sense.navigation.domain.compass.Bearing

data class NavigationVector(val direction: Bearing, val distance: Float)


data class NavigationDelta(val bearing: Float, val distance: Float, val elevationChange: Float)