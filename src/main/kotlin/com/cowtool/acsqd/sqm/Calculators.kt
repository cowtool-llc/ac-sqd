package com.cowtool.acsqd.sqm

import com.cowtool.acsqd.SqdCalculatorException
import com.cowtool.acsqd.distance.DistanceResult
import com.cowtool.acsqd.distance.airports
import com.cowtool.acsqd.distance.getSegmentDistance
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import kotlin.math.roundToInt

interface EarningResultCore {
    val distanceResult: DistanceResult
    val sqmPercent: Int
    val isSqmPercentEstimated: Boolean
    val aeroplanPointsPercent: Int
    val bonusPointsPercent: Int
    val isSqdEligible: Boolean
    var sqd: Int?

    val sqm: Int?
    val aeroplanMiles: Int?
    val bonusPoints: Int?
    val totalMiles: Int?
}

open class EarningResult(
    override val distanceResult: DistanceResult,
    override val sqmPercent: Int,
    override val isSqmPercentEstimated: Boolean,
    override val aeroplanPointsPercent: Int = sqmPercent,
    override val bonusPointsPercent: Int,
    eligibleForMinimumPoints: Boolean,
    private val minimumPoints: Int = if (eligibleForMinimumPoints) 250 else 0,
    override val isSqdEligible: Boolean,
    override var sqd: Int? = null,
) : EarningResultCore {
    private val distance
        get() = distanceResult.distance

    override val sqm
        get() = distance?.let { distance ->
            if (sqmPercent == 0) {
                0
            } else {
                (max(distance, minimumPoints) * sqmPercent / 100.0).roundToInt()
            }
        }

    override val aeroplanMiles
        get() = distance?.let { distance ->
            if (aeroplanPointsPercent == 0) {
                0
            } else {
                (max(distance, minimumPoints) * aeroplanPointsPercent / 100.0).roundToInt()
            }
        }

    override val bonusPoints: Int?
        get() = calculateBonusPoints()

    private fun calculateBonusPoints(): Int? {
        val sqm = sqm
        val distance = distance
        return when {
            sqm == null || distance == null -> null
            bonusPointsPercent == 0 -> 0
            else -> (min(sqm, max(distance, minimumPoints)) * bonusPointsPercent / 100.0).roundToInt()
        }
    }

    override val totalMiles
        get() = calculateTotalMiles()

    private fun calculateTotalMiles(): Int? {
        val aeroplanMiles = aeroplanMiles
        val bonusPoints = bonusPoints
        return if (aeroplanMiles == null || bonusPoints == null) null else aeroplanMiles + bonusPoints
    }
}

open class StarAllianceEarningResult(
    distanceResult: DistanceResult,
    sqmPercent: Int,
    bonusPointsPercent: Int = 0,
    hasAeroplanStatus: Boolean,
    ticketNumber: String,
) : EarningResult(
    distanceResult = distanceResult,
    sqmPercent = sqmPercent,
    isSqmPercentEstimated = false,
    bonusPointsPercent = bonusPointsPercent,
    eligibleForMinimumPoints = hasAeroplanStatus,
    isSqdEligible = sqmPercent > 0 && ticketNumber.startsWith("014")
)

typealias EarningCalculator = (
    distanceResult: DistanceResult,
    origin: String,
    originCountry: String?,
    originContinent: String?,
    destination: String,
    destinationCountry: String?,
    destinationContinent: String?,
    fareClass: String?,
    fareBasis: String?,
    ticketNumber: String,
    hasAeroplanStatus: Boolean,
    bonusPointsPercentage: Int,
) -> EarningResult?

private enum class BonusPercentage {
    FULL, FIXED_25, NONE
}

private abstract class SimpleStarAllianceEarningCalculator(
    private val bonusPercentage: BonusPercentage = BonusPercentage.NONE,
) : EarningCalculator {
    abstract fun getSqmPercentage(fareClass: String): Int

    fun calculate(
        distanceResult: DistanceResult,
        fareClass: String?,
        ticketNumber: String,
        hasAeroplanStatus: Boolean,
        bonusPointsPercentage: Int,
    ): EarningResult? {
        if (fareClass == null) {
            return null
        }
        val percentage = getSqmPercentage(fareClass)
        val bonusPointsPercent = if (bonusPointsPercentage > 0) {
            when (bonusPercentage) {
                BonusPercentage.FULL -> bonusPointsPercentage
                BonusPercentage.FIXED_25 -> 25
                BonusPercentage.NONE -> 0
            }
        } else {
            0
        }
        return EarningResult(
            distanceResult = distanceResult,
            sqmPercent = percentage,
            isSqmPercentEstimated = false,
            bonusPointsPercent = bonusPointsPercent,
            eligibleForMinimumPoints = hasAeroplanStatus,
            isSqdEligible = ticketNumber.startsWith("014") && percentage > 0
        )
    }

    override fun invoke(
        distanceResult: DistanceResult,
        origin: String,
        originCountry: String?,
        originContinent: String?,
        destination: String,
        destinationCountry: String?,
        destinationContinent: String?,
        fareClass: String?,
        fareBasis: String?,
        ticketNumber: String,
        hasAeroplanStatus: Boolean,
        bonusPointsPercentage: Int,
    ) = calculate(
        distanceResult,
        fareClass,
        ticketNumber,
        hasAeroplanStatus,
        bonusPointsPercentage,
    )
}

private abstract class SimplePartnerEarningCalculator(
    private val baseMinimumPoints: Int = 250,
    private val alwaysEarnsMinimumPoints: Boolean = false,
) : EarningCalculator {
    abstract fun getAeroplanMilesPercentage(fareClass: String): Int

    fun calculate(
        distanceResult: DistanceResult,
        fareClass: String?,
        hasAeroplanStatus: Boolean,
    ): EarningResult? {
        if (fareClass == null) {
            return null
        }
        val aeroplanMilesPercent = getAeroplanMilesPercentage(fareClass)
        return EarningResult(
            distanceResult = distanceResult,
            sqmPercent = 0,
            isSqmPercentEstimated = false,
            aeroplanPointsPercent = aeroplanMilesPercent,
            bonusPointsPercent = 0,
            eligibleForMinimumPoints = hasAeroplanStatus || alwaysEarnsMinimumPoints,
            minimumPoints = if (hasAeroplanStatus || alwaysEarnsMinimumPoints) baseMinimumPoints else 0,
            isSqdEligible = false
        )
    }

    override fun invoke(
        distanceResult: DistanceResult,
        origin: String,
        originCountry: String?,
        originContinent: String?,
        destination: String,
        destinationCountry: String?,
        destinationContinent: String?,
        fareClass: String?,
        fareBasis: String?,
        ticketNumber: String,
        hasAeroplanStatus: Boolean,
        bonusPointsPercentage: Int,
    ) = calculate(distanceResult, fareClass, hasAeroplanStatus)
}

fun isAeroplanFareBasis(fareBasis: String) =
    fareBasis.contains("BP00") || fareBasis.contains("AERO")

private val acCalculator: EarningCalculator =
    calc@{ distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, fareBasis, _, hasAeroplanStatus, bonusPointsPercentage ->
        class ACEarningResult(
            sqmPercent: Int,
            isSqmPercentEstimated: Boolean,
            aeroplanPointsPercent: Int = sqmPercent,
        ) : EarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            isSqmPercentEstimated = isSqmPercentEstimated,
            aeroplanPointsPercent = aeroplanPointsPercent,
            bonusPointsPercent = bonusPointsPercentage,
            eligibleForMinimumPoints = hasAeroplanStatus,
            isSqdEligible = sqmPercent > 0
        )

        if (!fareBasis.isNullOrEmpty()) {
            if (isAeroplanFareBasis(fareBasis) || fareClass in setOf("X", "I")) {
                return@calc ACEarningResult(sqmPercent = 0, isSqmPercentEstimated = false)
            }

            val split = fareBasis.split("/")

            if (split.size > 1) {
                val designator = split[1]
                if (designator.startsWith("AE")) {
                    return@calc ACEarningResult(sqmPercent = 0, isSqmPercentEstimated = false)
                }
            }

            val trueBasis = split[0]
            val brand = trueBasis.takeLast(2)

            when (brand) {
                "BA", "GT" ->
                    return@calc if (originCountry == null || destinationCountry == null) {
                        null
                    } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                        ACEarningResult(sqmPercent = 0, aeroplanPointsPercent = 10, isSqmPercentEstimated = false)
                    } else {
                        ACEarningResult(sqmPercent = 0, aeroplanPointsPercent = 25, isSqmPercentEstimated = false)
                    }

                "TG" ->
                    return@calc if (originCountry == null || destinationCountry == null) {
                        null
                    } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                        ACEarningResult(sqmPercent = 50, aeroplanPointsPercent = 25, isSqmPercentEstimated = false)
                    } else {
                        ACEarningResult(sqmPercent = 50, isSqmPercentEstimated = false)
                    }

                "FL" -> return@calc ACEarningResult(sqmPercent = 100, isSqmPercentEstimated = false)
                "CO" -> return@calc ACEarningResult(sqmPercent = 115, isSqmPercentEstimated = false)
                "LT" -> return@calc ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = false)

                "PL", "PF" -> {
                    return@calc when (fareClass) {
                        "O", "E", "A" -> ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = false)
                        "Y", "B" -> ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = true)
                        "M" -> ACEarningResult(sqmPercent = 115, isSqmPercentEstimated = true)
                        "U", "H", "Q", "V" -> ACEarningResult(sqmPercent = 100, isSqmPercentEstimated = true)
                        "W" -> if ((originCountry == "Canada" || originCountry == "United States") &&
                            (destinationCountry == "Canada" || destinationCountry == "United States")
                        ) {
                            ACEarningResult(sqmPercent = 100, isSqmPercentEstimated = true)
                        } else {
                            ACEarningResult(sqmPercent = 50, isSqmPercentEstimated = true)
                        }

                        "S", "T", "L", "K", "G" -> if (originCountry == "Canada" && destinationCountry == "Canada") {
                            ACEarningResult(sqmPercent = 50, aeroplanPointsPercent = 25, isSqmPercentEstimated = true)
                        } else {
                            ACEarningResult(sqmPercent = 50, isSqmPercentEstimated = true)
                        }

                        else -> ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = true)
                    }
                }

                "EL", "EF" -> {
                    return@calc when (fareClass) {
                        "J", "C", "D", "Z", "P" -> ACEarningResult(sqmPercent = 150, isSqmPercentEstimated = false)
                        "O", "E", "A" -> ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = true)
                        "Y", "B" -> ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = true)
                        "M" -> ACEarningResult(sqmPercent = 115, isSqmPercentEstimated = true)
                        "U", "H", "Q", "V" -> ACEarningResult(sqmPercent = 100, isSqmPercentEstimated = true)
                        "W" -> if ((originCountry == "Canada" || originCountry == "United States") &&
                            (destinationCountry == "Canada" || destinationCountry == "United States")
                        ) {
                            ACEarningResult(sqmPercent = 100, isSqmPercentEstimated = true)
                        } else {
                            ACEarningResult(sqmPercent = 50, isSqmPercentEstimated = true)
                        }

                        "S", "T", "L", "K", "G" -> if (originCountry == "Canada" && destinationCountry == "Canada") {
                            ACEarningResult(sqmPercent = 50, aeroplanPointsPercent = 25, isSqmPercentEstimated = true)
                        } else {
                            ACEarningResult(sqmPercent = 50, isSqmPercentEstimated = true)
                        }

                        else -> ACEarningResult(sqmPercent = 150, isSqmPercentEstimated = true)
                    }
                }
            }
        }

        // If it's an eUpgrade, use the first letter of the fare basis
        val trueFareClass = if (fareClass in setOf("R", "N")) {
            if (fareBasis.isNullOrBlank()) {
                fareClass // Not really, but we want to continue
            } else {
                fareBasis.substring(0, 1)
            }
        } else {
            fareClass
        }

        when (trueFareClass) {
            "J", "C", "D", "Z", "P" -> ACEarningResult(sqmPercent = 150, isSqmPercentEstimated = false)
            "O", "E", "A" -> ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = false)
            "Y", "B" -> ACEarningResult(sqmPercent = 125, isSqmPercentEstimated = false)
            "M", "U", "H", "Q", "V" -> ACEarningResult(sqmPercent = 100, isSqmPercentEstimated = false)
            "W" ->
                if (originCountry == null || destinationCountry == null) {
                    null
                } else if ((originCountry == "Canada" || originCountry == "United States") &&
                    (destinationCountry == "Canada" || destinationCountry == "United States")
                ) {
                    ACEarningResult(sqmPercent = 100, isSqmPercentEstimated = false)
                } else {
                    ACEarningResult(sqmPercent = 50, isSqmPercentEstimated = false)
                }

            // TODO: F may not be accurate here.  This needs more ACV data.
            "S", "T", "L", "K", "G", "F" ->
                if (originCountry == null || destinationCountry == null) {
                    null
                } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                    ACEarningResult(sqmPercent = 50, aeroplanPointsPercent = 25, isSqmPercentEstimated = false)
                } else {
                    ACEarningResult(sqmPercent = 50, isSqmPercentEstimated = false)
                }

            null -> null
            else -> ACEarningResult(sqmPercent = 0, isSqmPercentEstimated = false)
        }
    }

private val a3Calculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "A", "C", "D", "Z" -> 125
        "Y", "B", "G", "W", "H", "L", "M", "V", "Q" -> 100
        "O", "J", "S", "K" -> 50
        "U", "T", "P", "E" -> 25
        else -> 0
    }
}

private val adCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "J", "C", "D", "I" -> 150
        "Y", "B", "A", "E", "F", "G", "H", "K", "L", "M", "N", "O" -> 100
        "P", "Q" -> 75
        "S", "T", "U" -> 50
        "X", "Z" -> 25
        else -> 0
    }
}

private val aiCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class AIEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "India" && destinationCountry == "India") {
            when (fareClass) {
                "F" -> AIEarningResult(sqmPercent = 150)
                "C", "D", "J", "Z" -> AIEarningResult(sqmPercent = 125)
                "R", "A", "N" -> AIEarningResult(sqmPercent = 110)
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> AIEarningResult(sqmPercent = 100)
                "L" -> AIEarningResult(sqmPercent = 50)
                "U", "T", "S" -> AIEarningResult(sqmPercent = 25)
                else -> AIEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F" -> AIEarningResult(sqmPercent = 150)
                "C", "D", "J", "Z" -> AIEarningResult(sqmPercent = 125)
                "R", "A", "N" -> AIEarningResult(sqmPercent = 110)
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> AIEarningResult(sqmPercent = 100)
                "L", "U", "T", "S" -> AIEarningResult(sqmPercent = 50)
                else -> AIEarningResult(sqmPercent = 0)
            }
        }
    }

private val avCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class AVEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        val domesticCountries = setOf(
            "Colombia",
            "Peru",
            "Ecuador",
            "Belize",
            "El Salvador",
            "Guatemala",
            "Honduras",
            "Nicaragua",
            "Costa Rica",
            "Panama"
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry in domesticCountries && destinationCountry in domesticCountries) {
            when (fareClass) {
                "C", "J", "D", "A", "K" -> AVEarningResult(sqmPercent = 125)
                "Y", "B", "M", "H", "Q", "V", "E", "G", "L", "O", "P", "Z" -> AVEarningResult(sqmPercent = 100)
                "T", "W", "S" -> AVEarningResult(sqmPercent = 25)
                else -> AVEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "J", "D", "A", "K" -> AVEarningResult(sqmPercent = 125)
                "Y", "B", "M", "H", "Q", "V", "E", "G", "L", "O", "P", "Z" -> AVEarningResult(sqmPercent = 100)
                "T", "W" -> AVEarningResult(sqmPercent = 50)
                "S" -> AVEarningResult(sqmPercent = 25)
                else -> AVEarningResult(sqmPercent = 0)
            }
        }
    }

private val brCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "J", "D" -> 125
        "K", "L", "T", "P" -> 100
        "Y", "B" -> 100
        "M", "H" -> 75
        "Q", "S" -> 50
        else -> 0
    }
}

private val caCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "F", "A" -> 150
        "J", "C", "D" -> 150
        "Z" -> 125
        "R" -> 110
        "G" -> 110
        "E" -> 90
        "Y", "B" -> 100
        "M", "H", "U", "Q", "V" -> 75
        "W", "T", "S" -> 50
        "L", "K", "P" -> 25
        else -> 0
    }
}

private val cmCalculator = object : SimpleStarAllianceEarningCalculator(
    bonusPercentage = BonusPercentage.FULL
) {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "J", "D", "R" -> 125
        "Y", "B", "M", "H", "Q", "K", "V", "U", "S", "W", "E", "L", "T" -> 100
        "O", "A" -> 100
        else -> 0
    }
}

private val cxCalculator: EarningCalculator =
    calc@{ distanceResult, origin, _, _, destination, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class CXEarningResult(
            aeroplanMilesPercent: Int,
        ) : EarningResult(
            distanceResult = distanceResult,
            sqmPercent = 0,
            isSqmPercentEstimated = false,
            aeroplanPointsPercent = aeroplanMilesPercent,
            bonusPointsPercent = 0,
            eligibleForMinimumPoints = hasAeroplanStatus,
            isSqdEligible = false
        )

        val other = when {
            origin == "HKG" -> destination
            destination == "HKG" -> origin
            else -> null
        }

        if (other !in setOf("CNX", "HKT", "BKK", "CEB", "MNL", "KUL", "SGN", "HAN")) {
            return@calc CXEarningResult(aeroplanMilesPercent = 0)
        }

        // TODO: Must be codeshare
        // Assume 014 is good enough
        if (!ticketNumber.startsWith("014")) {
            return@calc CXEarningResult(aeroplanMilesPercent = 0)
        }

        when (fareClass) {
            "F", "A" -> CXEarningResult(aeroplanMilesPercent = 150)
            "J", "C", "D", "P", "I" -> CXEarningResult(aeroplanMilesPercent = 125)
            "W", "R", "E" -> CXEarningResult(aeroplanMilesPercent = 110)
            "Y", "B", "H", "K", "M" -> CXEarningResult(aeroplanMilesPercent = 100)
            "L" -> CXEarningResult(aeroplanMilesPercent = 50)
            "V" -> CXEarningResult(aeroplanMilesPercent = 25)
            else -> CXEarningResult(aeroplanMilesPercent = 0)
        }
    }

private val ekCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "F", "A" -> 150
        "J", "C", "I", "O" -> 125
        "H", "W", "E" -> 110
        "R", "Y", "P", "X" -> 100
        "U", "B", "M", "K" -> 50
        "G", "T", "L", "Q" -> 15
        else -> 0
    }
}

private val enCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "J", "C" -> 150
        "D", "Z", "P" -> 125
        "Y", "B", "M" -> 100
        "U", "H", "Q", "V" -> 75
        "W", "S", "T", "L", "K" -> 50
        else -> 0
    }
}

private val etCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "J", "D" -> 125
        "Y", "G", "S", "B" -> 100
        "M", "K", "L", "V" -> 75
        "H", "U", "Q", "T" -> 50
        "W", "E", "O" -> 25
        else -> 0
    }
}

private val ewCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "J", "D" -> 150
        "E", "N" -> 125
        "I", "C", "H", "Q", "V", "W", "S", "G", "K", "L", "T", "X", "Y", "B", "M", "F", "O", "R" -> 50
        else -> 0
    }
}

private val eyCalculator: EarningCalculator =
    { distanceResult, _, _, _, _, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class EYEarningResult(
            aeroplanMilesPercent: Int,
        ) : EarningResult(
            distanceResult = distanceResult,
            sqmPercent = 0,
            isSqmPercentEstimated = false,
            aeroplanPointsPercent = aeroplanMilesPercent,
            bonusPointsPercent = 0,
            eligibleForMinimumPoints = hasAeroplanStatus,
            isSqdEligible = false
        )

        // Miles earned on Etihad Airways is limited to flights ticketed and operated by Etihad Airways or flights marketed
        // and ticketed by Air Canada, operated by Etihad Airways
        // TODO: Deal with EY-marketed AC-ticketed
        if (!(ticketNumber.startsWith("014") || ticketNumber.startsWith("607"))) {
            EYEarningResult(aeroplanMilesPercent = 0)
        }

        when (fareClass) {
            "P" -> EYEarningResult(aeroplanMilesPercent = 400)
            "F", "A" -> EYEarningResult(aeroplanMilesPercent = 250)
            "J", "C", "D" -> EYEarningResult(aeroplanMilesPercent = 150)
            "W", "Z" -> EYEarningResult(aeroplanMilesPercent = 125)
            "R" -> EYEarningResult(aeroplanMilesPercent = 110)
            "Y", "B" -> EYEarningResult(aeroplanMilesPercent = 100)
            "H", "K", "M", "Q" -> EYEarningResult(aeroplanMilesPercent = 75)
            "L", "V", "U", "E", "G" -> EYEarningResult(aeroplanMilesPercent = 50)
            "T" -> EYEarningResult(aeroplanMilesPercent = 25)
            else -> EYEarningResult(aeroplanMilesPercent = 0)
        }
    }

private val g3Calculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "C", "L" -> 150
        "F", "D" -> 125
        "Y", "T", "J" -> 100
        "W", "P", "E", "A" -> 75
        "U", "N", "B" -> 50
        else -> 0
    }
}

private val gfCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "J", "C", "D", "I" -> 125
        "Y" -> 100
        "L", "M", "B", "H" -> 50
        "U", "V", "E", "O", "N", "S", "K", "X", "Q", "W" -> 25
        else -> 0
    }
}

private val hoCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "J", "C" -> 150
        "D", "A" -> 125
        "R" -> 110
        "Y", "B", "M", "U" -> 100
        "H", "Q", "V" -> 75
        "W" -> 50
        else -> 0
    }
}

private val lhCalculator: EarningCalculator =
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage ->
        class LHEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = if (bonusPointsPercentage > 0) 25 else 0,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> LHEarningResult(sqmPercent = 150)
                "P" -> LHEarningResult(sqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> LHEarningResult(sqmPercent = 50)
                else -> LHEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F", "A" -> LHEarningResult(sqmPercent = 150)
                "J", "C", "D", "Z" -> LHEarningResult(sqmPercent = 150)
                "P" -> LHEarningResult(sqmPercent = 100)
                "G", "E" -> LHEarningResult(sqmPercent = 125)
                "N" -> LHEarningResult(sqmPercent = 100)
                "Y", "B" -> LHEarningResult(sqmPercent = 125)
                "M", "U", "H", "Q", "V" -> LHEarningResult(sqmPercent = 100)
                "W", "S", "T", "L" -> LHEarningResult(sqmPercent = 50)
                else -> LHEarningResult(sqmPercent = 0)
            }
        }
    }

private val loCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D" -> 125
        "Z", "F" -> 100
        "P" -> 105
        "A", "R" -> 100
        "Y", "B", "M" -> 100
        "E", "H", "K", "Q", "T", "G", "S" -> 75
        "V", "W", "L" -> 50
        "U", "O" -> 25
        else -> 0
    }
}

private val lxCalculator: EarningCalculator =
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage ->
        class LXEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = if (bonusPointsPercentage > 0) 25 else 0,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> LXEarningResult(sqmPercent = 150)
                "P" -> LXEarningResult(sqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> LXEarningResult(sqmPercent = 50)
                else -> LXEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F", "A" -> LXEarningResult(sqmPercent = 150)
                "J", "C", "D", "Z" -> LXEarningResult(sqmPercent = 150)
                "P" -> LXEarningResult(sqmPercent = 100)
                "G", "E" -> LXEarningResult(sqmPercent = 125)
                "N" -> LXEarningResult(sqmPercent = 100)
                "Y", "B" -> LXEarningResult(sqmPercent = 125)
                "M", "U", "H", "Q", "V" -> LXEarningResult(sqmPercent = 100)
                "W", "S", "T", "L" -> LXEarningResult(sqmPercent = 50)
                else -> LXEarningResult(sqmPercent = 0)
            }
        }
    }

private val mkCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "J", "D", "C", "R", "I" -> 125
        "Y", "K" -> 100
        "H", "T" -> 75
        "U", "V", "S", "L" -> 50
        "Q", "M", "O", "X", "G", "B", "E", "N" -> 25
        else -> 0
    }
}

private val msCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class MSEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "Egypt" && destinationCountry == "Egypt") {
            when (fareClass) {
                "C", "D", "J", "Z" -> MSEarningResult(sqmPercent = 125)
                "Y", "B", "M", "H" -> MSEarningResult(sqmPercent = 100)
                "Q", "K" -> MSEarningResult(sqmPercent = 75)
                else -> MSEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "J", "Z" -> MSEarningResult(sqmPercent = 125)
                "Y", "B", "M", "H" -> MSEarningResult(sqmPercent = 100)
                "Q", "K" -> MSEarningResult(sqmPercent = 75)
                "V", "L" -> MSEarningResult(sqmPercent = 50)
                "G", "S", "W", "T" -> MSEarningResult(sqmPercent = 25)
                else -> MSEarningResult(sqmPercent = 0)
            }
        }
    }

private val nhCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "F", "A" -> 150
        "J" -> 150
        "C", "D", "Z" -> 125
        "P" -> 100
        "G", "E" -> 100
        "N" -> 70
        "Y", "B", "M" -> 100
        "U", "H", "Q" -> 70
        "V", "W", "S", "T" -> 50
        "L", "K" -> 30
        else -> 0
    }
}

private val nzCalculator: EarningCalculator =
    { distanceResult, _, originCountry, originContinent, _, destinationCountry, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class NZEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null ||
            originContinent == null || destinationContinent == null
        ) {
            null
        } else if (originCountry == "New Zealand" && destinationCountry == "New Zealand") {
            when (fareClass) {
                "C", "D", "J", "Z" -> NZEarningResult(sqmPercent = 125)
                "U", "E", "O", "A", "Y", "B" -> NZEarningResult(sqmPercent = 100)
                "M", "H", "Q", "V" -> NZEarningResult(sqmPercent = 70)
                else -> NZEarningResult(sqmPercent = 0)
            }
        } else if (originContinent == "Oceania" && destinationContinent == "Oceania") {
            when (fareClass) {
                "C", "D", "J", "Z" -> NZEarningResult(sqmPercent = 125)
                "U", "E", "O", "A", "Y", "B" -> NZEarningResult(sqmPercent = 100)
                "M", "H", "Q" -> NZEarningResult(sqmPercent = 70)
                else -> NZEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "J", "Z" -> NZEarningResult(sqmPercent = 125)
                "U", "E", "O", "A", "Y", "B" -> NZEarningResult(sqmPercent = 100)
                "M", "H", "Q", "V", "W", "T" -> NZEarningResult(sqmPercent = 70)
                else -> NZEarningResult(sqmPercent = 0)
            }
        }
    }

private val oaCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "A", "C", "D", "Z" -> 125
        "Y", "B", "G", "W", "H", "L", "M", "V", "Q" -> 100
        "O", "J", "S", "K" -> 50
        "U", "T", "P", "E" -> 25
        else -> 0
    }
}

private val osCalculator: EarningCalculator =
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage ->
        class OSEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = if (bonusPointsPercentage > 0) 25 else 0,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> OSEarningResult(sqmPercent = 150)
                "P" -> OSEarningResult(sqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> OSEarningResult(sqmPercent = 50)
                else -> OSEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "J", "C", "D", "Z" -> OSEarningResult(sqmPercent = 150)
                "P" -> OSEarningResult(sqmPercent = 100)
                "G", "E" -> OSEarningResult(sqmPercent = 125)
                "N" -> OSEarningResult(sqmPercent = 100)
                "Y", "B" -> OSEarningResult(sqmPercent = 125)
                "M", "U", "H", "Q", "V" -> OSEarningResult(sqmPercent = 100)
                "W", "S", "T", "L" -> OSEarningResult(sqmPercent = 50)
                else -> OSEarningResult(sqmPercent = 0)
            }
        }
    }

private val ouCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D", "Z" -> 125
        "Y", "B" -> 100
        "M", "H", "K", "V", "Q", "A", "F" -> 75
        "W", "S", "J", "O", "P", "G" -> 50
        "T", "E" -> 25
        else -> 0
    }
}

private val ozCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class OZEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "South Korea" && destinationCountry == "South Korea") {
            when (fareClass) {
                "C" -> OZEarningResult(sqmPercent = 125)
                "U", "Y", "B", "A" -> OZEarningResult(sqmPercent = 100)
                "M", "H", "E", "Q", "K", "S" -> OZEarningResult(sqmPercent = 50)
                "V" -> OZEarningResult(sqmPercent = 25)
                else -> OZEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "J", "Z" -> OZEarningResult(sqmPercent = 125)
                "U", "Y", "B", "M" -> OZEarningResult(sqmPercent = 100)
                "A", "H", "E", "Q", "K", "S" -> OZEarningResult(sqmPercent = 50)
                "V", "W", "G", "T" -> OZEarningResult(sqmPercent = 25)
                else -> OZEarningResult(sqmPercent = 0)
            }
        }
    }

private val pbCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "Y", "R" -> 125
        "V", "L", "H" -> 75
        "T", "K", "D" -> 25
        else -> 0
    }
}

private val qhCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "J", "C", "I" -> 125
        "Z", "X", "E" -> 110
        "Y", "W", "S", "B" -> 100
        "H", "K", "L", "M", "N" -> 50
        "Q", "T", "O", "R" -> 25
        else -> 0
    }
}

private val saCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class SAEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "South Africa" && destinationCountry == "South Africa") {
            when (fareClass) {
                "C", "J" -> SAEarningResult(sqmPercent = 150)
                "Z" -> SAEarningResult(sqmPercent = 125)
                "D" -> SAEarningResult(sqmPercent = 100)
                "Y", "B", "M", "K" -> SAEarningResult(sqmPercent = 100)
                "H", "S", "Q" -> SAEarningResult(sqmPercent = 50)
                "T", "V" -> SAEarningResult(sqmPercent = 50)
                "L", "W", "G" -> SAEarningResult(sqmPercent = 25)
                else -> SAEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "J" -> SAEarningResult(sqmPercent = 150)
                "Z", "D", "P" -> SAEarningResult(sqmPercent = 125)
                "Y", "B", "M", "K" -> SAEarningResult(sqmPercent = 100)
                "H", "S", "Q" -> SAEarningResult(sqmPercent = 50)
                "T", "V" -> SAEarningResult(sqmPercent = 50)
                "L", "W", "G" -> SAEarningResult(sqmPercent = 25)
                else -> SAEarningResult(sqmPercent = 0)
            }
        }
    }

private val snCalculator = object : SimpleStarAllianceEarningCalculator(
    bonusPercentage = BonusPercentage.FIXED_25
) {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "J", "C", "D", "Z" -> 150
        "P" -> 100
        "G", "E" -> 125
        "N" -> 100
        "Y", "B" -> 125
        "M", "U", "H" -> 100
        "W", "S", "T", "Q", "V", "O" -> 50
        else -> 0
    }
}

private val sqCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "A", "F" -> 150
        "Z", "C", "J", "D", "U" -> 125
        "S", "T", "P", "R", "L" -> 100
        "Y", "B", "E" -> 100
        "M", "H", "W" -> 75
        else -> 0
    }
}

private val tgCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "F", "A", "P" -> 150
        "C", "D", "J", "Z" -> 125
        "Y", "B" -> 110
        "M", "H", "Q", "U" -> 100
        "T", "K", "S" -> 50
        else -> 0
    }
}

private val tkCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D", "Z", "K" -> 125
        "J" -> 110
        "Y", "B", "M", "A", "H" -> 100
        "S", "O", "E", "Q", "T", "L" -> 70
        "V" -> 25
        else -> 0
    }
}

private val tpCalculator: EarningCalculator =
    { distanceResult, origin, _, _, destination, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class TPEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        val specialDestinations = setOf("LIS", "OPO", "PXO", "FNC")
        if (origin in specialDestinations && destination in specialDestinations) {
            when (fareClass) {
                "C", "D", "Z", "J" -> TPEarningResult(sqmPercent = 150)
                "Y", "B" -> TPEarningResult(sqmPercent = 100)
                "M", "H", "Q", "W", "K", "U" -> TPEarningResult(sqmPercent = 100)
                "V", "S", "L", "G", "A", "P", "E" -> TPEarningResult(sqmPercent = 50)
                else -> TPEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "Z", "J" -> TPEarningResult(sqmPercent = 150)
                "Y", "B" -> TPEarningResult(sqmPercent = 100)
                "M", "H", "Q" -> TPEarningResult(sqmPercent = 100)
                "V", "W", "S", "L", "K", "U", "G", "A", "P" -> TPEarningResult(sqmPercent = 50)
                else -> TPEarningResult(sqmPercent = 0)
            }
        }
    }

private val uaCalculator: EarningCalculator =
    { distanceResult, _, _, _, _, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage ->
        class UAEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = bonusPointsPercentage,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        when (fareClass) {
            "J", "C", "D", "Z", "P" -> UAEarningResult(sqmPercent = 150)
            "O", "A" -> UAEarningResult(sqmPercent = 125)
            "R" -> UAEarningResult(sqmPercent = 100)
            "Y", "B" -> UAEarningResult(sqmPercent = 125)
            "M", "E", "U", "H" -> UAEarningResult(sqmPercent = 100)
            "Q", "V", "W" -> UAEarningResult(sqmPercent = 75)
            "S", "T", "L" -> UAEarningResult(sqmPercent = 50)
            "K", "G" -> UAEarningResult(sqmPercent = 25)
            "N" -> EarningResult(
                distanceResult = distanceResult,
                sqmPercent = 0,
                isSqmPercentEstimated = false,
                aeroplanPointsPercent = 50,
                bonusPointsPercent = 0,
                eligibleForMinimumPoints = hasAeroplanStatus,
                isSqdEligible = false
            )

            else -> UAEarningResult(sqmPercent = 0)
        }
    }

private val ukCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "C", "J", "D", "Z" -> 125
        "S", "T", "P", "R", "Y", "B", "M" -> 100
        "A", "H", "N", "Q", "V" -> 50
        "E", "O" -> 20
        else -> 0
    }
}

private val vaCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, _, hasAeroplanStatus, _ ->
        class VAEarningResult(
            percent: Int,
            isDomestic: Boolean,
        ) : EarningResult(
            distanceResult = distanceResult,
            sqmPercent = if (isDomestic) percent else 0,
            isSqmPercentEstimated = false,
            aeroplanPointsPercent = percent,
            bonusPointsPercent = 0,
            eligibleForMinimumPoints = hasAeroplanStatus,
            isSqdEligible = false,
        )

        val isDomestic = originCountry != null && originCountry == destinationCountry

        when (fareClass) {
            "J" -> VAEarningResult(percent = 150, isDomestic = isDomestic)
            "C", "D" -> VAEarningResult(percent = 125, isDomestic = isDomestic)
            "A", "Y", "B", "W", "H", "K", "L" -> VAEarningResult(percent = 100, isDomestic = isDomestic)
            "R", "E", "O", "N", "V", "P", "Q", "T", "I", "S", "F", "U" -> VAEarningResult(percent = 50, isDomestic = isDomestic)
            "M" -> VAEarningResult(percent = 25, isDomestic = isDomestic)
            "G" -> VAEarningResult(percent = 50, isDomestic = isDomestic)
            else -> VAEarningResult(percent = 0, isDomestic = isDomestic)
        }
    }

private val wyCalculator = object : SimplePartnerEarningCalculator() {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "P", "F" -> 150
        "J", "C", "D", "R" -> 125
        "Y", "H", "M", "B" -> 100
        "K", "I", "Q", "T" -> 50
        "N", "L", "U", "O", "G" -> 25
        "E" -> 10
        else -> 0
    }
}

private val ynCalculator = object : SimplePartnerEarningCalculator(
    baseMinimumPoints = 500,
    alwaysEarnsMinimumPoints = true
) {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "Y" -> 100
        "V", "Q", "B" -> 75
        "E", "H", "L" -> 50
        else -> 0
    }
}

private val zhCalculator = object : SimpleStarAllianceEarningCalculator() {
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "J", "C" -> 150
        "D", "Z", "R" -> 125
        "G" -> 100
        "Y", "B", "M", "U" -> 100
        "E" -> 90
        "H", "Q" -> 70
        "V", "W", "S", "T" -> 50
        "L", "P", "A", "K" -> 50
        else -> 0
    }
}

private val _4yCalculator: EarningCalculator =
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, _ ->
        class _4YEarningResult(
            sqmPercent: Int,
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> _4YEarningResult(sqmPercent = 150)
                "P" -> _4YEarningResult(sqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> _4YEarningResult(sqmPercent = 50)
                else -> _4YEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "J", "C", "D", "Z" -> _4YEarningResult(sqmPercent = 150)
                "P" -> _4YEarningResult(sqmPercent = 100)
                "G", "E" -> _4YEarningResult(sqmPercent = 125)
                "N" -> _4YEarningResult(sqmPercent = 100)
                "Y", "B" -> _4YEarningResult(sqmPercent = 125)
                "M", "U", "H", "Q", "V" -> _4YEarningResult(sqmPercent = 100)
                "W", "S", "T", "L" -> _4YEarningResult(sqmPercent = 50)
                else -> _4YEarningResult(sqmPercent = 0)
            }
        }
    }

private val _5tCalculator = object : SimplePartnerEarningCalculator(
    baseMinimumPoints = 500,
    alwaysEarnsMinimumPoints = true
) {
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "Y" -> 100
        "C", "H", "P" -> 75
        "M", "O" -> 50
        "B", "A", "T" -> 25
        else -> 0
    }
}

private val nonStarCalculator = object : EarningCalculator {
    override fun invoke(
        distanceResult: DistanceResult,
        origin: String,
        originCountry: String?,
        originContinent: String?,
        destination: String,
        destinationCountry: String?,
        destinationContinent: String?,
        fareClass: String?,
        fareBasis: String?,
        ticketNumber: String,
        hasAeroplanStatus: Boolean,
        bonusPointsPercentage: Int,
    ) = EarningResult(
        distanceResult = distanceResult,
        sqmPercent = 0,
        isSqmPercentEstimated = false,
        bonusPointsPercent = 0,
        eligibleForMinimumPoints = false,
        isSqdEligible = false,
    )
}

private fun getCalculator(operatingAirline: String) = when (operatingAirline.uppercase(Locale.getDefault())) {
    "AC", "KV", "L4", "QK", "RV", "ZX" -> acCalculator // Air Canada
    "A3" -> a3Calculator // Aegean Airlines
    "AD" -> adCalculator // Azul Airlines
    "AI" -> aiCalculator // Air India
    "AV" -> avCalculator // Avianca
    "BR" -> brCalculator // EVA Air
    "CA" -> caCalculator // Air China
    "CM" -> cmCalculator // COPA Airlines
    "CX", "KA" -> cxCalculator // Cathay Pacific
    "EK" -> ekCalculator // Emirates
    "EN" -> enCalculator // Air Dolomiti
    "ET" -> etCalculator // Ethiopian Airlines
    "EW" -> ewCalculator // Eurowings
    "EY" -> eyCalculator // Etihad Airways
    "G3" -> g3Calculator // GOL
    "GF" -> gfCalculator // Gulf Air
    "HO" -> hoCalculator // Juneyao Airlines
    "LH", "CL" -> lhCalculator // Lufthansa
    "LO" -> loCalculator // LOT Polish Airlines
    "LX" -> lxCalculator // Swiss
    "MK" -> mkCalculator // Air Mauritius
    "MS" -> msCalculator // EgyptAir
    "NH", "NQ" -> nhCalculator // ANA
    "NZ" -> nzCalculator // Air New Zealand
    "OA" -> oaCalculator // Olympic Air
    "OS" -> osCalculator // Austrian
    "OU" -> ouCalculator // Croatia Airlines
    "OZ" -> ozCalculator // Asiana Airlines
    "PB" -> pbCalculator // PAL Airlines
    "QH" -> qhCalculator // Bamboo Airways
    "SA" -> saCalculator // South African Airways
    "SN" -> snCalculator // Brussels Airlines
    "SQ" -> sqCalculator // Singapore Airlines
    "TG" -> tgCalculator // Thai Airways
    "TK" -> tkCalculator // Turkish Airlines
    "TP", "NI" -> tpCalculator // TAP Air Portugal
    "UA" -> uaCalculator // United Airlines
    "UK" -> ukCalculator // Vistara
    "VA" -> vaCalculator // Virgin Australia
    "WY" -> wyCalculator // Omar Air
    "YN" -> ynCalculator // Air Creebec
    "ZH" -> zhCalculator // Shenzhen Airlines
    "4Y" -> _4yCalculator // Eurowings Discover
    "5T" -> _5tCalculator // Canadian North
    else -> nonStarCalculator // Everything else
}

fun getEarningResult(
    operatingAirline: String,
    marketingAirline: String?,
    origin: String,
    destination: String,
    fareClass: String?,
    fareBasis: String?,
    ticketNumber: String,
    hasAeroplanStatus: Boolean,
    bonusPointsPercentage: Int,
): EarningResult? {
    val effectiveOperator = when {
        marketingAirline == "AC" && operatingAirline == "PB" -> "AC"
        marketingAirline == "LX" && operatingAirline in setOf("2L", "BT", "WK") -> "LX"
        else -> operatingAirline
    }

    val calculator = getCalculator(effectiveOperator)

    val originCountry = airports[origin]?.country
    val originContinent = countriesToContinent[originCountry]
    val destinationCountry = airports[destination]?.country
    val destinationContinent = countriesToContinent[destinationCountry]

    val distanceResult = getDistanceResult(origin, destination)

    return calculator(
        distanceResult,
        origin,
        originCountry,
        originContinent,
        destination,
        destinationCountry,
        destinationContinent,
        fareClass,
        fareBasis,
        ticketNumber,
        hasAeroplanStatus,
        bonusPointsPercentage,
    )
}

fun getDistanceResult(origin: String, destination: String) = getSegmentDistance(origin, destination).also {
    it.error?.let { error ->
        throw SqdCalculatorException("Error calculating distance for $origin-$destination: $error")
    }
}
