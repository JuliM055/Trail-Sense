package com.kylecorry.trail_sense.astronomy.domain.sun

class CivilTwilightCalculator : BaseSunTimesCalculator(-6.0)
class NauticalTwilightCalculator : BaseSunTimesCalculator(-12.0)
class AstronomicalTwilightCalculator : BaseSunTimesCalculator(-18.0)
class ActualTwilightCalculator : BaseSunTimesCalculator(-0.833333)
