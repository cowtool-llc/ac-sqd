package com.cowtool.acsqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

data class SqdInput @JvmOverloads constructor(
    var baseFare: String = "",
    var surcharges: String = "",
    var segments: String = "",
    var ticket: String = "",
    var aeroplanStatus: String = "",
    var bonusPointsPrivilege: String = "",
)

data class SqdResult(
    val itinerary: Itinerary?,
    val errorMessage: String?,
)

class SqdHandler : RequestHandler<SqdInput, SqdResult> {
    override fun handleRequest(input: SqdInput, context: Context): SqdResult {
        val baseFare = input.baseFare.toDoubleOrNull()
        val surcharges = input.surcharges.toDoubleOrNull()
        val segments = input.segments
        val ticket = input.ticket.ifBlank { "014" }
        val aeroplanStatus = input.aeroplanStatus
        val hasBonusPointsPrivilege = input.bonusPointsPrivilege.toBoolean()

        return SqdCalculator(
            ticket,
            aeroplanStatus,
            hasBonusPointsPrivilege,
            segments,
            baseFare,
            surcharges,
        ).calculate()
    }
}
