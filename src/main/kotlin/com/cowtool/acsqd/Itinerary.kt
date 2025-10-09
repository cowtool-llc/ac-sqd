package com.cowtool.acsqd

import com.cowtool.acsqd.distance.airports
import com.cowtool.acsqd.sqm.getDistanceResult
import com.cowtool.acsqd.sqm.getEarningResult
import kotlin.math.ceil
import kotlin.math.roundToInt

data class Itinerary(
    val segments: List<Segment>,
    val totalRow: TotalRow,
) {
    companion object {
        @Throws(SqdCalculatorException::class)
        fun parse(
            ticket: String,
            aeroplanStatus: String,
            segmentsCsv: String,
            baseFare: Double?,
            surcharges: Double?,
        ): Itinerary {
            if (baseFare == null || baseFare < 0) {
                throw SqdCalculatorException("Invalid base fare.  If base fare is 0, enter 0.")
            }
            if (surcharges == null || surcharges < 0) {
                throw SqdCalculatorException("Invalid surcharges.  If surcharges are 0, enter 0.")
            }

            val segments = segmentsCsv.trim().split(Regex("\\s+")).map {
                Segment.parse(it, ticket, aeroplanStatus)
            }
            // If we're missing any distance, we can't calculate SQC
            val missingAnyDistance = segments.none { it.distance == null }
            val totalDistance = segments.mapNotNull { it.distance }.sum()
            val totalFare = baseFare + surcharges
            segments.forEach {
                it.earningResult?.eligibleDollars = when {
                    !missingAnyDistance || it.distance == null -> null
                    else -> ceil(it.distance.toLong() * totalFare / totalDistance).roundToInt()
                }
            }

            val totalSqc = if (segments.none { it.earningResult?.sqc == null }) {
                segments.mapNotNull { it.earningResult?.sqc }.sum()
            } else {
                null
            }

            val totalBasePoints = if (segments.none { it.earningResult?.basePoints == null }) {
                segments.mapNotNull { it.earningResult?.basePoints }.sum()
            } else {
                null
            }

            val totalBonusPoints = if (segments.none { it.earningResult?.bonusPoints == null }) {
                segments.mapNotNull { it.earningResult?.bonusPoints }.sum()
            } else {
                null
            }

            val totalPoints = if (segments.none { it.earningResult?.totalPoints == null }) {
                segments.mapNotNull { it.earningResult?.totalPoints }.sum()
            } else {
                null
            }

            val totalLqm = if (segments.none { it.earningResult?.lqm == null }) {
                segments.mapNotNull { it.earningResult?.lqm }.sum()
            } else {
                null
            }

            val totalRow =
                TotalRow(
                    distance = totalDistance,

                    basePoints = totalBasePoints,
                    bonusPoints = totalBonusPoints,
                    totalPoints = totalPoints,

                    sqc = totalSqc,

                    lqm = totalLqm,
                )

            return Itinerary(segments, totalRow)
        }
    }
}

class Segment(
    val airline: String,
    marketingAirline: String?,
    val origin: String,
    val destination: String,
    val fareClass: String,
    val fareBrand: String?,
    ticketNumber: String,
    eliteBonusMultiplier: Int,
) {
    val earningResult = getEarningResult(
        operatingAirline = airline,
        marketingAirline = marketingAirline,
        origin = origin,
        destination = destination,
        fareClass = fareClass,
        fareBasis = fareBrand,
        ticketNumber = ticketNumber,
        eliteBonusMultiplier = eliteBonusMultiplier,
    )

    private val distanceResult = earningResult?.distanceResult ?: getDistanceResult(origin, destination)
    val distance = distanceResult.distance

    override fun toString(): String {
        return "$airline,$origin,$destination,$fareClass,$fareBrand"
    }

    companion object {
        @Throws(SqdCalculatorException::class)
        fun parse(
            csv: String,
            ticketNumber: String,
            aeroplanStatus: String,
        ): Segment {
            val csvValues = csv.split(",")

            if (csvValues.size !in 4..5) {
                throw SqdCalculatorException("Each line must contain airline, origin, destination, fare class, and optionally fare brand.  Error parsing: $csv")
            }

            val airline = csvValues[0].uppercase()

            val origin = csvValues[1].uppercase()
            if (origin !in airports) {
                throw SqdCalculatorException("Invalid origin: $origin")
            }

            val destination = csvValues[2].uppercase()
            if (destination !in airports) {
                throw SqdCalculatorException("Invalid destination: $destination")
            }

            val fareClass = csvValues[3].uppercase()
            if (fareClass.length != 1 || !fareClass.toCharArray().first().isLetter()) {
                throw SqdCalculatorException("Invalid fare class: $fareClass")
            }

            val fareBrand = if (csvValues.size > 4) {
                when {
                    csvValues[4].isBlank() -> null
                    else -> csvValues[4].uppercase()
                }
            } else {
                null
            }

            val eliteBonusMultiplier = getEliteBonusMultiplier(aeroplanStatus)

            return Segment(
                airline = airline,
                marketingAirline = null,
                origin = origin,
                destination = destination,
                fareClass = fareClass,
                fareBrand = fareBrand,
                ticketNumber = ticketNumber,
                eliteBonusMultiplier = eliteBonusMultiplier,
            )
        }
    }
}

fun getEliteBonusMultiplier(
    aeroplanStatus: String,
) = when (aeroplanStatus.toIntOrNull()) {
    100 -> 5
    75 -> 4
    50 -> 3
    35 -> 2
    25 -> 1
    else -> 0
}

data class TotalRow(
    val distance: Int?,

    val basePoints: Int?,
    val bonusPoints: Int?,
    val totalPoints: Int?,

    val sqc: Int?,

    val lqm: Int?,
)
