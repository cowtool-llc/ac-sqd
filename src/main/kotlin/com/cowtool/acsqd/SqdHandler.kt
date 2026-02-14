package com.cowtool.acsqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

data class SqdInput @JvmOverloads constructor(
    var baseFare: String = "",
    var surcharges: String = "",
    var segments: String = "",
    var ticket: String = "",
    var aeroplanStatus: String = "",
)

interface SqdResult {
    val itinerary: Itinerary?
    val errorMessage: String?
}

data class SqdResultImpl(
    override val itinerary: Itinerary?,
    override val errorMessage: String?,
) : SqdResult

class SqdHandler : RequestHandler<SqdInput, SqdResult> {
    override fun handleRequest(input: SqdInput, context: Context): SqdResult {
        val baseFare = input.baseFare.toDoubleOrNull()
        val surcharges = input.surcharges.toDoubleOrNull()
        val segments = input.segments
        val ticket = input.ticket.ifBlank { "014" }
        val aeroplanStatus = input.aeroplanStatus

        return SqdCalculator(
            ticket,
            aeroplanStatus,
            segments,
            baseFare,
            surcharges,
        ).calculate()
    }
}
