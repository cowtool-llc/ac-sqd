package com.canadiancow.aqd.distance

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun getSegmentDistance(origin: String, destination: String): DistanceResult {
    val aeroplanDistance = aeroplanDistances["$origin$destination"] ?: aeroplanDistances["$destination$origin"]

    if (aeroplanDistance != null) {
        return DistanceResult(aeroplanDistance, "Aeroplan", null)
    }

    val haversineResult = calculateHaversine(origin, destination)

    return DistanceResult(haversineResult.distance, "Haversine", haversineResult.error)
}

class DistanceResult(
    val distance: Int?,
    val source: String?,
    val error: String?
)

private fun calculateHaversine(origin: String, destination: String): HaversineResult {
    val originAirport = airports[origin.toUpperCase()]
        ?: return HaversineResult(null, "Origin airport ($origin) not found")
    val destinationAirport = airports[destination.toUpperCase()]
        ?: return HaversineResult(null, "Destination airport ($destination) not found")

    return HaversineResult(calculateHaversineDistance(originAirport, destinationAirport).toInt())
}

private fun calculateHaversineDistance(originAirport: Airport, destinationAirport: Airport): Double {
    val dLat = Math.toRadians(destinationAirport.latitude - originAirport.latitude)
    val dLon = Math.toRadians(destinationAirport.longitude - originAirport.longitude)
    val originLat = Math.toRadians(originAirport.latitude)
    val destinationLat = Math.toRadians(destinationAirport.latitude)

    val a = sin(dLat / 2).pow(2.toDouble()) + sin(dLon / 2).pow(2.toDouble()) * cos(originLat) * cos(destinationLat)
    val c = 2 * asin(sqrt(a))
    return earthRadiusMi * c
}

private const val earthRadiusMi = 3959.toDouble()

data class HaversineResult(
    val distance: Int?,
    val error: String? = null
)
