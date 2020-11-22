package com.canadiancow.sqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.canadiancow.sqd.distance.aeroplanDistanceCsv
import com.canadiancow.sqd.distance.airportsCsv
import com.canadiancow.sqd.sqm.countriesCsv

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
        val altitudeStatus = input.aeroplanStatus
        val hasBonusPointsPrivilege = input.bonusPointsPrivilege.toBoolean()

        return SqdCalculator(
            ticket,
            altitudeStatus,
            hasBonusPointsPrivilege,
            segments,
            baseFare,
            surcharges
        ).calculate()
    }
}
