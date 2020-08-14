package com.canadiancow.aqd

import com.canadiancow.aqd.aqm.getDistanceResult
import com.canadiancow.aqd.aqm.getEarningResult
import com.canadiancow.aqd.distance.airports

class Itinerary(
    val segments: List<Segment>,
    val totalRow: TotalRow
) {
    companion object {
        @Throws(AqdCalculatorException::class)
        fun parse(
            ticket: String,
            altitudeStatus: String,
            hasBonusMilesPrivilege: Boolean,
            segmentsCsv: String,
            baseFare: Double?,
            surcharges: Double?
        ): Itinerary {
            if (baseFare == null || baseFare < 0) {
                throw AqdCalculatorException("Invalid base fare.  If base fare is 0, enter 0.")
            }
            if (surcharges == null || surcharges < 0) {
                throw AqdCalculatorException("Invalid surcharges.  If surcharges are 0, enter 0.")
            }

            val segments = segmentsCsv.trim().split(Regex("\\s+")).map {
                Segment.parse(it, ticket, altitudeStatus, hasBonusMilesPrivilege)
            }
            // If we're missing any distance, we can't calculate AQD
            val missingAnyDistance = segments.none { it.distance == null }
            val totalDistance = segments.mapNotNull { it.distance }.sum()
            val totalFare = baseFare + surcharges
            segments.forEach {
                it.earningResult?.sqd = when {
                    !missingAnyDistance || it.earningResult == null || it.distance == null -> null
                    it.earningResult.isAqdEligible -> it.distance.toLong() * totalFare / totalDistance
                    else -> 0.0
                }
            }

            val totalAqd = if (segments.none { it.earningResult?.sqd == null }) {
                segments.mapNotNull { it.earningResult?.sqd }.sum()
            } else {
                null
            }

            val totalAqm = if (segments.none { it.earningResult?.aqm == null }) {
                segments.mapNotNull { it.earningResult?.aqm }.sum()
            } else {
                null
            }

            val totalAeroplanMiles = if (segments.none { it.earningResult?.aeroplanMiles == null }) {
                segments.mapNotNull { it.earningResult?.aeroplanMiles }.sum()
            } else {
                null
            }

            val totalBonusMiles = if (segments.none { it.earningResult?.bonusMiles == null }) {
                segments.mapNotNull { it.earningResult?.bonusMiles }.sum()
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

            val totalPoints = if (segments.none { it.earningResult?.totalPoints == null }) {
                segments.mapNotNull { it.earningResult?.totalPoints }.sum()
            } else {
                null
            }

            val totalRow =
                TotalRow(
                    distance = totalDistance,

                    aqm = totalAqm,
                    aqd = totalAqd,
                    aeroplanMiles = totalAeroplanMiles,
                    bonusMiles = totalBonusMiles,
                    totalMiles = totalMiles,

                    sqm = totalSqm,
                    sqd = totalAqd,
                    totalPoints = totalPoints
                )

            return Itinerary(segments, totalRow)
        }
    }
}

class Segment(
    val airline: String,
    val origin: String,
    val destination: String,
    val fareClass: String,
    val fareBrand: String?,
    ticketNumber: String,
    hasAltitudeStatus: Boolean,
    bonusMilesPercentage: Int,
    statusRate: Int,
    bonusRate: Int
) {
    val earningResult = getEarningResult(
        airline,
        origin,
        destination,
        fareClass,
        fareBasis = fareBrand,
        ticketNumber = ticketNumber,
        hasAltitudeStatus = hasAltitudeStatus,
        bonusMilesPercentage = bonusMilesPercentage,
        statusRate = statusRate,
        bonusRate = bonusRate
    )

    private val distanceResult = earningResult?.distanceResult ?: getDistanceResult(origin, destination)
    val distance = distanceResult.distance

    val distanceString = distanceResult.distance?.toString() ?: "???"
    val distanceSourceString = distanceResult.source ?: "???"

    val aqmString = earningResult?.aqm?.toString() ?: "???"
    val aeroplanMilesString = earningResult?.aeroplanMiles?.toString() ?: "???"
    val bonusMilesString = earningResult?.bonusMiles?.toString() ?: "???"
    val totalMilesString = earningResult?.totalMiles?.toString() ?: "???"

    val sqmString = earningResult?.sqm?.toString() ?: "???"
    val baseRateString = earningResult?.baseRate?.let { "${it}x" } ?: "???"
    val statusRateString = earningResult?.statusRate?.let { "${it}x" } ?: "???"
    val bonusRateString = earningResult?.bonusRate?.let { "${it}x" } ?: "???"
    val totalRateString = earningResult?.totalRate?.let { "${it}x" } ?: "???"
    val totalPointsString: String
        get() = earningResult?.totalPoints?.toString() ?: "???"

    override fun toString(): String {
        return "$airline,$origin,$destination,$fareClass,$fareBrand"
    }

    companion object {
        @Throws(AqdCalculatorException::class)
        fun parse(csv: String, ticketNumber: String, altitudeStatus: String, hasBonusMilesPrivilege: Boolean): Segment {
            val csvValues = csv.split(",")

            if (csvValues.size < 4 || csvValues.size > 5) {
                throw AqdCalculatorException("Each line must contain airline, origin, destination, fare class, and optionally fare brand.  Error parsing: $csv")
            }

            val airline = csvValues[0].toUpperCase()

            val origin = csvValues[1].toUpperCase()
            if (origin !in airports) {
                throw AqdCalculatorException("Invalid origin: $origin")
            }

            val destination = csvValues[2].toUpperCase()
            if (destination !in airports) {
                throw AqdCalculatorException("Invalid destination: $destination")
            }

            val fareClass = csvValues[3].toUpperCase()
            if (fareClass.length != 1 || !fareClass.toCharArray().first().isLetter()) {
                throw AqdCalculatorException("Invalid fare class: $fareClass")
            }

            val fareBrand = if (csvValues.size > 4) {
                when {
                    csvValues[4].isBlank() -> null
                    FareBrand.values().map { it.name.toUpperCase() }
                        .contains(csvValues[4].toUpperCase()) -> csvValues[4]
                    else -> {
                        throw AqdCalculatorException("Invalid fare brand: ${csvValues[4]}. For non-AC, leave brand blank.")
                    }
                }
            } else {
                null
            }

            val hasAltitudeStatus = altitudeStatus.isNotBlank()
            val bonusMilesPercentage = (if (hasBonusMilesPrivilege) altitudeStatus.toIntOrNull() else null) ?: 0

            val statusRate = convertBonusMilesPercentageToStatusEarnRate(altitudeStatus.toIntOrNull() ?: 0)
            val bonusRate = if (hasBonusMilesPrivilege) statusRate else 0

            return Segment(
                airline,
                origin,
                destination,
                fareClass,
                fareBrand,
                ticketNumber,
                hasAltitudeStatus,
                bonusMilesPercentage,
                statusRate,
                bonusRate
            )
        }
    }
}

private fun convertBonusMilesPercentageToStatusEarnRate(bonusMilesPercentage: Int) = when (bonusMilesPercentage) {
    25, 35 -> 1
    50 -> 2
    75 -> 3
    100 -> 4
    else -> 0
}

class TotalRow(
    val distance: Int?,

    val aqm: Int?,
    val aqd: Double?,
    val aeroplanMiles: Int?,
    val bonusMiles: Int?,
    val totalMiles: Int?,

    val sqm: Int?,
    val sqd: Double?,
    val totalPoints: Int?
)
