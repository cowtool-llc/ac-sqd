package com.canadiancow.sqd.sqm

import com.canadiancow.sqd.SqdCalculator
import com.canadiancow.sqd.parseResourceToCsv

val countriesToContinent: Map<String, String> by lazy {
    parseResourceToCsv("/country_continents.csv") { _, _, values ->
        values[1]
    }
}

val countriesCsv: String by lazy {
    "<pre>${::SqdCalculator.javaClass.getResource("/country_continents.csv").readText()}</pre>"
}
