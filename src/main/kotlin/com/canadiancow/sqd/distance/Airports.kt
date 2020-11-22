package com.canadiancow.sqd.distance

import com.canadiancow.sqd.SqdCalculator
import com.canadiancow.sqd.parseResourceToCsv

class Airport(
    val iataCode: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

val airports: Map<String, Airport> by lazy {
    parseResourceToCsv("/airports.csv") { index, line, values ->
        if (values.size != 4) {
            throw IllegalStateException("Invalid line $index: $line")
        }

        val iataCode = values[0]
        val country = values[1]
        val latitude = values[2].toDouble()
        val longitude = values[3].toDouble()

        Airport(iataCode, country, latitude, longitude)
    }
}

val airportsCsv: String by lazy {
    "<pre>${::SqdCalculator.javaClass.getResource("/airports.csv").readText()}</pre>"
}
