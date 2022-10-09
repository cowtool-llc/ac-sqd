package com.cowtool.acsqd.distance

import com.cowtool.acsqd.parseResourceToCsv
import org.junit.jupiter.api.Test

internal class AirportsTest {
    @Test
    fun `airports are unique`() {
        val airports = mutableSetOf<String>()
        validateAirports { iataCode, _, _, _ ->
            if (iataCode in airports) {
                throw IllegalStateException("$iataCode in airports file multiple times")
            }
            airports += iataCode
        }
    }

    @Test
    fun `airport codes are valid`() {
        val airports = mutableSetOf<String>()
        validateAirports { iataCode, _, _, _ ->
            if (iataCode.length != 3) {
                throw IllegalStateException("'$iataCode' is not a valid 3 character code")
            }
            airports += iataCode
        }
    }

    private fun validateAirports(validator: (String, String, Double, Double) -> Unit) {
        parseResourceToCsv("/airports.csv") { index, line, values ->
            if (values.size != 4) {
                throw IllegalStateException("Invalid line $index: $line")
            }

            val iataCode = values[0]
            val country = values[1]
            val latitude = values[2].toDouble()
            val longitude = values[3].toDouble()

            validator(iataCode, country, latitude, longitude)
        }
    }
}
