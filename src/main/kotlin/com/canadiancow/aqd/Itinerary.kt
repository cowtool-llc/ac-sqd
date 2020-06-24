package com.canadiancow.aqd

import com.canadiancow.aqd.aqm.getEarningResult
import com.canadiancow.aqd.distance.airports

class Itinerary(
        val segments: List<Segment>,
        val segmentAqd: List<Double?>,
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
            val segmentAqd = segments.map {
                when {
                    !missingAnyDistance || it.earningResult == null || it.distance == null -> null
                    it.earningResult.isAqdEligible -> it.distance.toLong() * totalFare / totalDistance
                    else -> 0.0
                }
            }

            val totalAqd = if (segmentAqd.none { it == null }) {
                segmentAqd.mapNotNull { it }.sum()
            } else {
                null
            }

            val totalAqm = if (segments.none { it.earningResult?.aqm == null }) {
                segments.mapNotNull { it.earningResult?.aqm }.sum()
            } else {
                null
            }

            val totalAeroplan = if (segments.none { it.earningResult?.aeroplan == null }) {
                segments.mapNotNull { it.earningResult?.aeroplan }.sum()
            } else {
                null
            }

            val totalBonus = if (segments.none { it.earningResult?.bonus == null }) {
                segments.mapNotNull { it.earningResult?.bonus }.sum()
            } else {
                null
            }

            val total = if (segments.none { it.earningResult?.total == null }) {
                segments.mapNotNull { it.earningResult?.total }.sum()
            } else {
                null
            }

            val totalRow =
                    TotalRow(
                            distance = totalDistance,
                            aqm = totalAqm,
                            aqd = totalAqd,
                            aeroplan = totalAeroplan,
                            bonus = totalBonus,
                            total = total
                    )

            return Itinerary(segments, segmentAqd, totalRow)
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
        bonusMilesPercentage: Int
) {
    val earningResult = getEarningResult(
            airline,
            origin,
            destination,
            fareClass,
            fareBasis = fareBrand,
            ticketNumber = ticketNumber,
            hasAltitudeStatus = hasAltitudeStatus,
            bonusMilesPercentage = bonusMilesPercentage
    )

    val distance = earningResult?.distance

    val distanceString = distance?.toString() ?: "???"
    val distanceSourceString = earningResult?.distanceSource ?: "???"

    val aqmString = earningResult?.aqm?.toString() ?: "???"
    val aeroplanString = earningResult?.aeroplan?.toString() ?: "???"
    val bonusString = earningResult?.bonus?.toString() ?: "???"
    val totalString = earningResult?.total?.toString() ?: "???"

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
                    FareBrand.values().map { it.name.toUpperCase() }.contains(csvValues[4].toUpperCase()) -> csvValues[4]
                    else -> {
                        throw AqdCalculatorException("Invalid fare brand: ${csvValues[4]}. For non-AC, leave brand blank.")
                    }
                }
            } else {
                null
            }

            val hasAltitudeStatus = altitudeStatus.isNotBlank()
            val bonusMilesPercentage = (if (hasBonusMilesPrivilege) altitudeStatus.toIntOrNull() else null) ?: 0

            return Segment(
                    airline,
                    origin,
                    destination,
                    fareClass,
                    fareBrand,
                    ticketNumber,
                    hasAltitudeStatus,
                    bonusMilesPercentage
            )
        }
    }
}

class TotalRow(
        val distance: Int?,
        val aqm: Int?,
        val aqd: Double?,
        val aeroplan: Int?,
        val bonus: Int?,
        val total: Int?
)