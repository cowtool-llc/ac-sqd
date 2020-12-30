package com.canadiancow.sqd.distance

import com.canadiancow.sqd.parseResourceToCsv
import org.junit.jupiter.api.Test

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
}
