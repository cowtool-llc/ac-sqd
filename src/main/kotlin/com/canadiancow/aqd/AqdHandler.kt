package com.canadiancow.aqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.canadiancow.aqd.aqm.countriesCsv
import com.canadiancow.aqd.distance.aeroplanDistanceCsv
import com.canadiancow.aqd.distance.airportsCsv

data class AqdInput @JvmOverloads constructor(
        var baseFare: String = "",
        var surcharges: String = "",
        var segments: String = "",
        var ticket: String = "",
        var altitudeStatus: String = "",
        var bonusMilesPrivilege: String = "",
        var fetch: String? = ""
)

class AqdHandler : RequestHandler<AqdInput, String> {
    override fun handleRequest(input: AqdInput, context: Context): String {
        when (input.fetch) {
            "airports" -> return airportsCsv
            "countries" -> return countriesCsv
            "distances" -> return aeroplanDistanceCsv
        }

        val baseFare = input.baseFare.toDoubleOrNull()
        val surcharges = input.surcharges.toDoubleOrNull()
        val segments = input.segments
        val ticket = if (input.ticket.isBlank()) "014" else input.ticket
        val altitudeStatus = input.altitudeStatus
        val hasBonusMilesPrivilege = input.bonusMilesPrivilege.toBoolean()

        return AqdCalculator(
                ticket,
                altitudeStatus,
                hasBonusMilesPrivilege,
                segments,
                baseFare,
                surcharges
        ).calculate()
    }
}
