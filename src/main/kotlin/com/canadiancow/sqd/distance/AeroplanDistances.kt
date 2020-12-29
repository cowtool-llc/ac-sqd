package com.canadiancow.sqd.distance

import com.canadiancow.sqd.SqdCalculator
import com.canadiancow.sqd.parseResourceToCsv

val aeroplanDistances: Map<String, Int> by lazy {
    parseResourceToCsv(
        "/aeroplan_distances.csv",
        keyParser = { "${it[0]}${it[1]}" }
    ) { index, line, values ->
        if (values.size != 4) {
            throw IllegalStateException("Invalid line $index: $line")
        }

        val newDistance = values[3]
        val oldDistance = values[2]

        getNewAeroplanDistance(oldDistance, newDistance)
    }
}

fun getNewAeroplanDistance(oldDistance: String, newDistance: String): Int {
    return (if (newDistance.isNotBlank()) newDistance else oldDistance).toInt()
}

val aeroplanDistanceCsv: String by lazy {
    "<pre>${::SqdCalculator.javaClass.getResource("/aeroplan_distances.csv").readText()}</pre>"
}
