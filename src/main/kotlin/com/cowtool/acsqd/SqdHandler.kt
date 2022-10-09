package com.cowtool.acsqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cowtool.acsqd.distance.aeroplanDistanceCsv
import com.cowtool.acsqd.distance.airportsCsv
import com.cowtool.acsqd.sqm.countriesCsv

data class SqdInput @JvmOverloads constructor(
    var baseFare: String = "",
    var surcharges: String = "",
    var segments: String = "",
    var ticket: String = "",
    var aeroplanStatus: String = "",
    var bonusPointsPrivilege: String = "",
    var fetch: String? = ""
)

class SqdHandler : RequestHandler<SqdInput, String> {
    override fun handleRequest(input: SqdInput, context: Context): String {
        when (input.fetch) {
            "airports" -> return airportsCsv
            "countries" -> return countriesCsv
            "distances" -> return aeroplanDistanceCsv
        }

        val baseFare = input.baseFare.toDoubleOrNull()
        val surcharges = input.surcharges.toDoubleOrNull()
        val segments = input.segments
        val ticket = if (input.ticket.isBlank()) "014" else input.ticket
        val aeroplanStatus = input.aeroplanStatus
        val hasBonusPointsPrivilege = input.bonusPointsPrivilege.toBoolean()

        return SqdCalculator(
            ticket,
            aeroplanStatus,
            hasBonusPointsPrivilege,
            segments,
            baseFare,
            surcharges
        ).calculate()
    }
}
