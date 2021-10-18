package com.cepmuvakkit.times.posAlgo

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * @author mgeden
 */
class EarthPosition(
    val latitude: Double, val longitude: Double,
    val timezone: Double = round(longitude / 15.0),
    val altitude: Int = 0, val temperature: Int = 10, val pressure: Int = 1010
) {
    fun toEarthHeading(target: EarthPosition): EarthHeading {
        // great circle formula from:
        // http://williams.best.vwh.net/avform.htm
        val lat1 = Math.toRadians(latitude) //7155849931833333333e-19 0.71
        val lat2 = Math.toRadians(target.latitude) //3737913479489224943e-19 0.373
        val lon1 = Math.toRadians(-longitude) //-5055637064497558276 e-19 -0.505
        val lon2 = Math.toRadians(-target.longitude) //-69493192920839161e-17  -0.69
        val a = sin((lat1 - lat2) / 2)
        val b = sin((lon1 - lon2) / 2)
        val d = 2 * MATH.asin(sqrt(a * a + cos(lat1) * cos(lat2) * b * b)) //3774840207564380360e-19
        //d=2*asin(sqrt((sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
        // double c=a*a+Math.cos(lat1)*Math.cos(lat2))*b*b
        val tc1 = if (d > 0) {
            //tc1=acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
            val x = MATH.acos((sin(lat2) - sin(lat1) * cos(d)) / (sin(d) * cos(lat1)))
            /*2646123918118404228e-18*/
            if (sin(lon2 - lon1) < 0) x else 2 * PI - x
        } else 0.0
        //  tc1=2*pi-acos((sin(lat2)-sin(lat1)*cos(d))/(sin(d)*cos(lat1)))
        val radPerDeg = PI / 180
        return EarthHeading(tc1 / radPerDeg, (d * 6371000).toLong())
    }
}
