package com.cepmuvakkit.times.posAlgo

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Equatorial(sunRightAscension: Double, sunDeclination: Double, radius: Double) {
    var α = sunRightAscension //right ascension (α) -also RA-, or hour angle (H) -also HA-
    var δ = sunDeclination //declination (δ)
    var Δ = radius //distance to the earth(Δ) in km

    fun equ2Topocentric(
        longitude: Double, latitude: Double, Height: Double, jd: Double, ΔT: Double
    ): Horizontal {
        val ϕ = Math.toRadians(latitude)
        val ρsinϕPr = ρsinϕPrime(ϕ, Height)
        val ρCosϕPr = ρCosϕPrime(ϕ, Height)

        //Calculate the Sidereal time

        //double ΔT = AstroLib.calculateTimeDifference(jd);
        val theta = SolarPosition.calculateGreenwichSiderealTime(jd, ΔT)

        //Convert to radians
        val δrad = Math.toRadians(δ)
        val cosδ = cos(δrad)
        //  4.26345151167726E-5
        //Calculate the Parallax
        val π = getHorizontalParallax(Δ)
        val sinπ = sin(π)

        //Calculate the hour angle
        val H = Math.toRadians(AstroLib.limitDegrees(theta + longitude - α))
        val cosH = cos(H)
        val sinH = sin(H)

        //Calculate the adjustment in right ascension
        val Δα = MATH.atan2(-ρCosϕPr * sinπ * sinH, cosδ - ρCosϕPr * sinπ * cosH)
        //  CAA2DCoordinate Topocentric;
        //    double αPrime =Math.toRadians(α)+Δα;
        val δPrime = MATH.atan2(
            (sin(δrad) - ρsinϕPr * sinπ) * cos(Δα),
            cosδ - ρCosϕPr * sinπ * cosH
        )
        val HPrime = H - Δα
        return Horizontal(
            azimuth = Math.toDegrees(
                MATH.atan2(sin(HPrime), cos(HPrime) * sin(ϕ) - tan(δPrime) * cos(ϕ)) + Math.PI
            ),
            altitude = Math.toDegrees(
                MATH.asin(sin(ϕ) * sin(δPrime) + cos(ϕ) * cos(δPrime) * cos(HPrime))
            )
        )
    }

    private fun ρsinϕPrime(ϕ: Double, Height: Double): Double {
        val U = MATH.atan(0.99664719 * tan(ϕ))
        return 0.99664719 * sin(U) + Height / 6378149 * sin(ϕ)
    }

    private fun ρCosϕPrime(ϕ: Double, Height: Double): Double {
        //Convert from degress to radians
        val U = MATH.atan(0.99664719 * tan(ϕ))
        return cos(U) + Height / 6378149 * cos(ϕ)
    }

    private fun getHorizontalParallax(RadiusVector: Double) = MATH.asin(6378.14 / RadiusVector)
}
