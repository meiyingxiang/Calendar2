package com.cepmuvakkit.times.posAlgo

/**
 * @author mehmetrg
 */
class Ecliptic(
    longitude: Double, // λ: the ecliptic longitude
    latitude: Double, // β: the ecliptic latitude
    radius: Double = 0.0 // Δ: distance in km
) {
    val λ = longitude
    val β = latitude
    val Δ = radius
}
