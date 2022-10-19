package com.cowtool.acsqd

class SqdCalculator(
    private val ticket: String,
    private val aeroplanStatus: String,
    private val hasBonusPointsPrivilege: Boolean,
    private val segments: String,
    private val baseFare: Double?,
    private val surcharges: Double?,
) {

    fun calculate(): SqdResult {
        if (segments.isNotBlank() && baseFare != null || surcharges != null) {
            return try {
                val itinerary = calculateSqdBreakdown()

                SqdResult(
                    itinerary = itinerary,
                    errorMessage = null,
                )
            } catch (e: SqdCalculatorException) {
                SqdResult(
                    itinerary = null,
                    errorMessage = e.message,
                )
            }
        }

        return SqdResult(
            itinerary = null,
            errorMessage = "You must specify all required values",
        )
    }

    @Throws(SqdCalculatorException::class)
    private fun calculateSqdBreakdown(): Itinerary {
        return Itinerary.parse(
            ticket = ticket,
            aeroplanStatus = aeroplanStatus,
            hasBonusPointsPrivilege = hasBonusPointsPrivilege,
            segmentsCsv = segments,
            baseFare = baseFare,
            surcharges = surcharges
        )
    }
}

internal val defaultSegments =
    """
AC,SFO,YVR,K,CO
AC,YVR,LHR,P,EL
LH,LHR,MUC,P,
TG,MUC,BKK,M
CX,BKK,HKG,C
""".trim()
