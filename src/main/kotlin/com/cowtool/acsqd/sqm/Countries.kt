package com.cowtool.acsqd.sqm

import com.cowtool.acsqd.parseResourceToCsv

val countriesToContinent: Map<String, String> by lazy {
    parseResourceToCsv("/country_continents.csv") { _, _, values ->
        values[1]
    }
}
