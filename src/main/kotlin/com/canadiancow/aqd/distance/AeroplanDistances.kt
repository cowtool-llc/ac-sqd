package com.canadiancow.aqd.distance

import com.canadiancow.aqd.AqdCalculator
import com.canadiancow.aqd.parseResourceToCsv

val aeroplanDistances: Map<String, Int> by lazy {
    parseResourceToCsv(
            "/aeroplan_distances.csv",
            keyParser = { "${it[0]}${it[1]}" }
    ) { index, line, values ->
        if (values.size != 3) {
            throw IllegalStateException("Invalid line $index: $line")
        }

        val distance = values[2].toInt()

        distance
    }
}

val aeroplanDistanceCsv: String by lazy {
    "<pre>${::AqdCalculator.javaClass.getResource("/aeroplan_distances.csv").readText()}</pre>"
}