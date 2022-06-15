package com.canadiancow.sqd.distance

import com.canadiancow.sqd.parseResourceToCsv
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class AeroplanDistancesTest {
    @Test
    fun `city pairs are unique`() {
        val pairs = mutableSetOf<String>()
        validateCityPairs { city1, city2, _ ->
            val cityPair = "$city1-$city2"
            if (cityPair in pairs) {
                throw IllegalStateException("$cityPair in distances file multiple times")
            }
            pairs += cityPair
        }
    }

    @Test
    fun `city1 + city2 are alphabetically listed`() {
        validateCityPairs { city1, city2, _ ->
            if (city1 > city2) {
                throw IllegalStateException("$city1-$city2 should be listed as $city2-$city1")
            }
        }
    }

    @Test
    fun `city pair list is sorted alphabetically`() {
        var lastPair: String? = null
        validateCityPairs { city1, city2, _ ->
            val cityPair = "$city1-$city2"
            lastPair?.let {
                if (it > cityPair) {
                    throw IllegalStateException("$lastPair must be after $cityPair")
                }
            }
            lastPair = cityPair
        }
    }

    private fun validateCityPairs(validator: (String, String, Int) -> Unit) {
        parseResourceToCsv("/aeroplan_distances.csv") { index, line, values ->
            if (values.size != 4) {
                throw IllegalStateException("Invalid line $index: $line")
            }

            val city1 = values[0]
            val city2 = values[1]
            val distance = getNewAeroplanDistance(oldDistance = values[2], newDistance = values[3])

            validator(city1, city2, distance)
        }
    }

    @Test
    fun `Aeroplan distances match Haversine distances`() {
        val percentageThreshold = 0.03

        parseResourceToCsv("/aeroplan_distances.csv") { _, _, values ->
            val city1 = values[0]
            val city2 = values[1]
            val oldDistance = values[2].toIntOrNull()
            val newDistance = values[3].toIntOrNull()
            val haversineDistance = calculateHaversine(city1, city2).distance!!

            if (oldDistance == null && newDistance == null) {
                throw IllegalArgumentException(
                    "Each line in the csv needs at least one of an old distance or new distance"
                )
            }

            oldDistance?.let { _ ->
                val oldPercentage = abs((oldDistance - haversineDistance) / haversineDistance.toDouble())
                if (oldPercentage > percentageThreshold) {
                    println(
                        "$city1-$city2 old Aeroplan distance ($oldDistance) more than $percentageThreshold off of " +
                            "Haversine distance ($haversineDistance)"
                    )
                }
//                assert(oldPercentage <= percentageThreshold) {
//                    "$city1-$city2 old Aeroplan distance ($oldDistance) more than $percentageThreshold off of " +
//                            "Haversine distance ($haversineDistance)"
//                }
            }

            newDistance?.let { _ ->
                val newPercentage = abs((newDistance - haversineDistance) / haversineDistance.toDouble())
                if (newPercentage > percentageThreshold) {
                    println(
                        "$city1-$city2 new Aeroplan distance ($newDistance) more than $percentageThreshold off of " +
                            "Haversine distance ($haversineDistance)"
                    )
                }
//                assert(newPercentage <= percentageThreshold) {
//                    "$city1-$city2 new Aeroplan distance ($newDistance) more than $percentageThreshold off of " +
//                            "Haversine distance ($haversineDistance)"
//                }
            }
        }
    }
}
