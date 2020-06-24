package com.canadiancow.aqd.aqm

import com.canadiancow.aqd.AqdCalculator
import com.canadiancow.aqd.parseResourceToCsv

val countriesToContinent: Map<String, String> by lazy {
    parseResourceToCsv("/country_continents.csv") { _, _, values ->
        values[1]
    }
}

val countriesCsv: String by lazy {
    "<pre>${::AqdCalculator.javaClass.getResource("/country_continents.csv").readText()}</pre>"
}
