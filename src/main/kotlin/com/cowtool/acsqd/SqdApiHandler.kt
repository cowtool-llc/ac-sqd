package com.cowtool.acsqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.cowtool.acsqd.sqm.EarningResult
import com.cowtool.acsqd.sqm.getEarningResult

data class SqdApiInput @JvmOverloads constructor(
    var operatingAirline: String = "",
    var marketingAirline: String? = null,
    var origin: String = "",
    var destination: String = "",
    var fareClass: String? = null,
    var fareBasis: String? = null,
    var ticketNumber: String = "",
    var aeroplanStatus: String = "",
    var eligibleDollars: Int? = null,
)

class SqdApiHandler : RequestHandler<SqdApiInput, EarningResult?> {
    override fun handleRequest(input: SqdApiInput, context: Context): EarningResult? {
        return getEarningResult(
            operatingAirline = input.operatingAirline,
            marketingAirline = input.marketingAirline.takeIf { !it.isNullOrBlank() },
            origin = input.origin,
            destination = input.destination,
            fareClass = input.fareClass,
            fareBasis = input.fareBasis,
            ticketNumber = input.ticketNumber,
            eliteBonusMultiplier = getEliteBonusMultiplier(input.aeroplanStatus),
            eligibleDollars = input.eligibleDollars,
        )
    }
}
