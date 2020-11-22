package com.canadiancow.sqd.distance

import com.canadiancow.sqd.SqdCalculator
import com.canadiancow.sqd.parseResourceToCsv

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
    "<pre>${::SqdCalculator.javaClass.getResource("/aeroplan_distances.csv").readText()}</pre>"
}
