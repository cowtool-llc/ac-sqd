package com.cowtool.acsqd.sqm

import com.cowtool.acsqd.SqdCalculator
import com.cowtool.acsqd.parseResourceToCsv

val countriesToContinent: Map<String, String> by lazy {
    parseResourceToCsv("/country_continents.csv") { _, _, values ->
        values[1]
    }
}

val countriesCsv: String by lazy {
    "<pre>${::SqdCalculator.javaClass.getResource("/country_continents.csv").readText()}</pre>"
}
