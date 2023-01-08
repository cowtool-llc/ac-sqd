package com.cowtool.acsqd.distance

import com.cowtool.acsqd.parseResourceToCsv
import org.junit.jupiter.api.Test
import kotlin.math.abs

internal class AeroplanDistancesTest {
    @Test
    fun `city pairs are unique`() {
        val pairs = mutableSetOf<String>()
        validateCityPairs { city1, city2, _, _ ->
            val cityPair = "$city1-$city2"
            if (cityPair in pairs) {
                throw IllegalStateException("$cityPair in distances file multiple times")
            }
            pairs += cityPair
        }
    }

    @Test
    fun `city1 + city2 are alphabetically listed`() {
        validateCityPairs { city1, city2, _, _ ->
            if (city1 > city2) {
                throw IllegalStateException("$city1-$city2 should be listed as $city2-$city1")
            }
        }
    }

    @Test
    fun `city pair list is sorted alphabetically`() {
        var lastPair: String? = null
        validateCityPairs { city1, city2, _, _ ->
            val cityPair = "$city1-$city2"
            lastPair?.let {
                if (it > cityPair) {
                    throw IllegalStateException("$lastPair must be after $cityPair")
                }
            }
            lastPair = cityPair
        }
    }

    @Test
    fun `saved distance is within 10 percent of haversine`() {
        validateCityPairs { city1, city2, newDistance, _ ->
            newDistance?.let { _ ->
                val haversineDistance = calculateHaversine(city1, city2).distance!!

                val difference = abs(newDistance.toDouble() / haversineDistance.toDouble())

                assert(
                    difference in 0.97..1.03,
                ) { "$city1-$city2 Haversine is $haversineDistance, but Aeroplan is $newDistance" }
            }
        }
    }

    private fun validateCityPairs(
        validator: (
            city1: String,
            city2: String,
            newDistance: Int?,
            oldDistance: Int?,
        ) -> Unit,
    ) {
        parseResourceToCsv("/aeroplan_distances.csv") { index, line, values ->
            if (values.size != 4) {
                throw IllegalStateException("Invalid line $index: $line")
            }

            val city1 = values[0]
            val city2 = values[1]

            validator(city1, city2, values[3].ifBlank { null }?.toInt(), values[2].ifBlank { null }?.toInt())
        }
    }
}
