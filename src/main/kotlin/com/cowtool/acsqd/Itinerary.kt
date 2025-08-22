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
            hasBonusPointsPrivilege: Boolean,
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
                Segment.parse(it, ticket, aeroplanStatus, hasBonusPointsPrivilege)
            }
            // If we're missing any distance, we can't calculate SQD
            val missingAnyDistance = segments.none { it.distance == null }
            val totalDistance = segments.mapNotNull { it.distance }.sum()
            val totalFare = baseFare + surcharges
            segments.forEach {
                it.earningResult?.sqd = when {
                    !missingAnyDistance || it.distance == null -> null
                    it.earningResult.isSqdEligible -> ceil(it.distance.toLong() * totalFare / totalDistance).roundToInt()
                    else -> 0
                }
            }

            val totalSqd = if (segments.none { it.earningResult?.sqd == null }) {
                segments.mapNotNull { it.earningResult?.sqd }.sum()
            } else {
                null
            }

            val totalAeroplanMiles = if (segments.none { it.earningResult?.aeroplanMiles == null }) {
                segments.mapNotNull { it.earningResult?.aeroplanMiles }.sum()
            } else {
                null
            }

            val totalBonusPoints = if (segments.none { it.earningResult?.bonusPoints == null }) {
                segments.mapNotNull { it.earningResult?.bonusPoints }.sum()
            } else {
                null
            }

            val totalMiles = if (segments.none { it.earningResult?.totalMiles == null }) {
                segments.mapNotNull { it.earningResult?.totalMiles }.sum()
            } else {
                null
            }

            val totalSqm = if (segments.none { it.earningResult?.sqm == null }) {
                segments.mapNotNull { it.earningResult?.sqm }.sum()
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

                    aeroplanMiles = totalAeroplanMiles,
                    bonusPoints = totalBonusPoints,
                    totalMiles = totalMiles,

                    sqm = totalSqm,
                    sqd = totalSqd,

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
    hasAeroplanStatus: Boolean,
    bonusPointsPercentage: Int,
) {
    val earningResult = getEarningResult(
        airline,
        marketingAirline = marketingAirline,
        origin,
        destination,
        fareClass,
        fareBasis = fareBrand,
        ticketNumber = ticketNumber,
        hasAeroplanStatus = hasAeroplanStatus,
        bonusPointsPercentage = bonusPointsPercentage,
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
            hasBonusPointsPrivilege: Boolean,
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

            val hasAeroplanStatus = aeroplanStatus.isNotBlank()
            val bonusPointsPercentage = (if (hasBonusPointsPrivilege) aeroplanStatus.toIntOrNull() else null) ?: 0

            return Segment(
                airline,
                marketingAirline = null,
                origin,
                destination,
                fareClass,
                fareBrand,
                ticketNumber,
                hasAeroplanStatus,
                bonusPointsPercentage,
            )
        }
    }
}

data class TotalRow(
    val distance: Int?,

    val aeroplanMiles: Int?,
    val bonusPoints: Int?,
    val totalMiles: Int?,

    val sqm: Int?,
    val sqd: Int?,

    val lqm: Int?,
)
