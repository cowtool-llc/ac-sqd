package com.cowtool.acsqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cowtool.acsqd.sqm.EarningResult
import com.cowtool.acsqd.sqm.getEarningResult

data class SqdApiInput @JvmOverloads constructor(
    var operatingAirline: String = "",
    var origin: String = "",
    var destination: String = "",
    var fareClass: String? = null,
    var fareBasis: String? = null,
    var ticketNumber: String = "",
    var hasAeroplanStatus: Boolean = false,
    var bonusPointsPercentage: Int = 0,
    var statusRate: Int = 0,
    var bonusRate: Int = 0,
)

class SqdApiHandler : RequestHandler<SqdApiInput, EarningResult?> {
    override fun handleRequest(input: SqdApiInput, context: Context): EarningResult? {
        return getEarningResult(
            operatingAirline = input.operatingAirline,
            origin = input.origin,
            destination = input.destination,
            fareClass = input.fareClass,
            fareBasis = input.fareBasis,
            ticketNumber = input.ticketNumber,
            hasAeroplanStatus = input.hasAeroplanStatus,
            bonusPointsPercentage = input.bonusPointsPercentage,
            statusRate = input.statusRate,
            bonusRate = input.bonusRate,
        )
    }
}
