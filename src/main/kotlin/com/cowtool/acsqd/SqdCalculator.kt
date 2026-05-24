package com.cowtool.acsqd

class SqdCalculator(
    private val ticket: String,
    private val aeroplanStatus: String,
    private val segments: String,
    private val baseFare: Double?,
    private val surcharges: Double?,
) {

    fun calculate(): SqdResult {
        if (segments.isNotBlank() && baseFare != null || surcharges != null) {
            return try {
                val itinerary = calculateSqdBreakdown()

                SqdResultImpl(
                    itinerary = itinerary,
                    errorMessage = null,
                )
            } catch (e: SqdCalculatorException) {
                SqdResultImpl(
                    itinerary = null,
                    errorMessage = e.message,
                )
            }
        }

        return SqdResultImpl(
            itinerary = null,
            errorMessage = "You must specify all required values",
        )
    }

    @Throws(SqdCalculatorException::class)
    private fun calculateSqdBreakdown(): Itinerary {
        return ItineraryImpl.parse(
            ticket = ticket,
            aeroplanStatus = aeroplanStatus,
            segmentsCsv = segments,
            baseFare = baseFare,
            surcharges = surcharges,
        )
    }
}

internal val defaultSegments =
    """
AC,SFO,YVR,K,CO
AC,YVR,LHR,P,EL
LH,LHR,MUC,P,EL
TG,MUC,BKK,M,EL
CX,BKK,HKG,C,EL
""".trim()
