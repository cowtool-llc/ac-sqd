package com.cowtool.acsqd.sqm

import com.cowtool.acsqd.SqdCalculatorException
import com.cowtool.acsqd.distance.DistanceResult
import com.cowtool.acsqd.distance.airports
import com.cowtool.acsqd.distance.getSegmentDistance
import java.util.Locale

interface EarningResult {
    val distanceResult: DistanceResult
    val sqcMultiplier: Int
    val eliteBonusMultiplier: Int
    var eligibleDollars: Int?

    val sqc: Int?
    val basePoints: Int?
    val bonusPoints: Int?
    val totalPoints: Int?

    val isLqmEligible: Boolean
    val lqm: Int?
}

class EarningResultAcTicketOrFlight(
    override val distanceResult: DistanceResult,
    override val sqcMultiplier: Int,
    override val eliteBonusMultiplier: Int,
    override var eligibleDollars: Int? = null,
    override val isLqmEligible: Boolean,
) : EarningResult {
    override val sqc
        get() = eligibleDollars?.let { eligibleDollars ->
            eligibleDollars * sqcMultiplier
        }

    override val basePoints
        get() = eligibleDollars

    override val bonusPoints
        get() = calculateBonusPoints()

    private fun calculateBonusPoints(): Int? {
        val eligibleDollars = eligibleDollars
        return when {
            eligibleDollars == null -> null
            eliteBonusMultiplier == 0 -> 0
            else -> eligibleDollars * eliteBonusMultiplier
        }
    }

    override val totalPoints
        get() = calculateTotalPoints()

    private fun calculateTotalPoints(): Int? {
        val basePoints = basePoints
        val bonusPoints = bonusPoints
        return if (basePoints == null || bonusPoints == null) null else basePoints + bonusPoints
    }

    override val lqm
        get() = if (isLqmEligible) {
            distanceResult.distance
        } else {
            0
        }
}

class EarningResultStarAllianceTicketAndFlight(
    override val distanceResult: DistanceResult,
    val distanceMultiplierPercent: Int,
) : EarningResult {
    // SQC is handled differently
    override val sqcMultiplier = 0

    // No elite bonus for non-AC flights on non-AC tickets
    override val eliteBonusMultiplier = 0

    // Dollars don't matter here
    override var eligibleDollars: Int? = null

    override val basePoints = distanceResult.distance?.let { distance ->
        (distance * distanceMultiplierPercent.toDouble() / 100.toDouble()).toInt()
    }

    override val sqc = basePoints?.let { points ->
        (points / 5.toDouble()).toInt()
    }

    override val bonusPoints = 0

    override val totalPoints
        get() = basePoints

    override val isLqmEligible = false

    override val lqm = 0
}

abstract class EarningResultNonStarAllianceFlight : EarningResult {
    // No SQC
    override val sqcMultiplier = 0
    override val sqc = 0

    override val eliteBonusMultiplier = 0

    override val bonusPoints = 0

    override val totalPoints
        get() = basePoints

    override val isLqmEligible = false

    override val lqm = 0
}

class EarningResultAcTicketNonStarAllianceFlight(
    override val distanceResult: DistanceResult,
    override var eligibleDollars: Int? = null,
) : EarningResultNonStarAllianceFlight() {
    override val basePoints
        get() = eligibleDollars
}

class EarningResultNonAcTicketNonStarAllianceFlight(
    override val distanceResult: DistanceResult,
    val distanceMultiplierPercent: Int,
) : EarningResultNonStarAllianceFlight() {
    // Dollars don't matter here
    override var eligibleDollars: Int? = null

    override val basePoints = distanceResult.distance?.let { distance ->
        (distance * distanceMultiplierPercent.toDouble()).toInt()
    }
}

data class CalculatorArgs(
    val distanceResult: DistanceResult,
    val operatingAirline: String,
    val origin: String,
    val originCountry: String?,
    val originContinent: String?,
    val destination: String,
    val destinationCountry: String?,
    val destinationContinent: String?,
    val fareClass: String?,
    val fareBasis: String?,
    val ticketNumber: String,
    val eliteBonusMultiplier: Int,
)

interface EarningCalculator {
    fun calculate(args: CalculatorArgs): EarningResult?

    fun isEligibleForElitePointsBonus(ticketNumber: String): Boolean
}

private abstract class StarAllianceEarningCalculator : EarningCalculator {
    protected open val forceAcCalculation = false

    override fun calculate(args: CalculatorArgs): EarningResult? {
        return if (args.ticketNumber.startsWith("014") || forceAcCalculation) {
            getAcTicketSqcMultiplier(args)?.let {
                EarningResultAcTicketOrFlight(
                    distanceResult = args.distanceResult,
                    sqcMultiplier = it,
                    eliteBonusMultiplier = args.eliteBonusMultiplier,
                    isLqmEligible = args.operatingAirline == "AC",
                )
            }
        } else {
            getDistancePercentMultiplier(args)?.let { percentMultiplier ->
                EarningResultStarAllianceTicketAndFlight(
                    distanceResult = args.distanceResult,
                    distanceMultiplierPercent = percentMultiplier,
                )
            }
        }
    }

    protected fun getAcTicketSqcMultiplier(args: CalculatorArgs): Int? {
        if (!isEligibleForSqc(args)) {
            return 0
        }

        if (!args.fareBasis.isNullOrEmpty()) {
            if (isAeroplanFareBasis(args.fareBasis) || args.fareClass in setOf("X", "I")) {
                return 0
            }

            val split = args.fareBasis.split("/")

            if (split.size > 1) {
                val designator = split[1]
                if (designator.startsWith("AE")) {
                    return 0
                }
            }

            val trueBasis = split[0]
            val brand = trueBasis.takeLast(2)

            when (brand) {
                "BA", "GT" -> return 0

                "TG" -> return 2

                "FL", "CO", "LT", "PL", "PF", "EL", "EF" -> return 4
            }

            getSqcMultiplierFromFareClass(args, args.fareBasis)?.let {
                return it
            }
        }

        args.fareClass?.let { fareClass ->
            getSqcMultiplierFromFareClass(args, fareClass)?.let {
                return it
            }

            // If we have a fare class, and it's not valid, it's 0
            return 0
        }

        return null
    }

    private fun getSqcMultiplierFromFareClass(
        args: CalculatorArgs,
        fareClass: String,
    ): Int? {
        if (!isEligibleForSqc(args)) {
            return 0
        }

        return when (fareClass.take(1)) {
            "J", "C", "D", "Z", "P",
            "O", "E", "A",
            "Y", "B",
            "M", "U", "H", "Q", "V",
                -> 4

            "W" ->
                if ((args.originCountry == "Canada" || args.originCountry == "United States") &&
                    (args.destinationCountry == "Canada" || args.destinationCountry == "United States")
                ) {
                    4
                } else {
                    2
                }

            "S", "T", "L", "K", "G" -> 2

            else -> null
        }
    }

    abstract fun getDistancePercentMultiplier(args: CalculatorArgs): Int?

    open fun isEligibleForSqc(args: CalculatorArgs) = true
}

private abstract class NonStarAllianceEarningCalculator : EarningCalculator {
    final override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun calculate(args: CalculatorArgs): EarningResult? {
        return if (args.ticketNumber.startsWith("014")) {
            EarningResultAcTicketNonStarAllianceFlight(
                distanceResult = args.distanceResult,
            )
        } else {
            getDistancePercentMultiplier(args)?.let { percentMultiplier ->
                EarningResultNonAcTicketNonStarAllianceFlight(
                    distanceResult = args.distanceResult,
                    distanceMultiplierPercent = percentMultiplier,
                )
            }
        }
    }

    abstract fun getDistancePercentMultiplier(args: CalculatorArgs): Int?
}

fun isAeroplanFareBasis(fareBasis: String) =
    fareBasis.contains("BP00") || fareBasis.contains("AERO")

private val acCalculator = object : StarAllianceEarningCalculator() {
    override val forceAcCalculation = true

    override fun isEligibleForElitePointsBonus(ticketNumber: String) = true

    // This will never be called
    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        throw IllegalStateException("AC flights are not handled this way")
}

private val a3Calculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "A", "C", "D", "Z" -> 125
        "Y", "B", "G", "W", "H", "L", "M", "V", "Q" -> 100
        "O", "J", "S", "K" -> 50
        "U", "T", "P", "E" -> 25
        else -> 0
    }
}

private val adCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "C", "D", "I" -> 150
        "Y", "B", "A", "E", "F", "G", "H", "K", "L", "M", "N", "O" -> 100
        "P", "Q" -> 75
        "S", "T", "U" -> 50
        "X", "Z" -> 25
        else -> 0
    }
}

private val aiCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originCountry == null || args.destinationCountry == null) {
            null
        } else if (args.originCountry == "India" && args.destinationCountry == "India") {
            when (args.fareClass) {
                "F" -> 150
                "C", "D", "J", "Z" -> 125
                "R", "A", "N" -> 110
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> 100
                "L" -> 50
                "U", "T", "S" -> 25
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "F" -> 150
                "C", "D", "J", "Z" -> 125
                "R", "A", "N" -> 110
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> 100
                "L", "U", "T", "S" -> 50
                else -> 0
            }
        }
}

private val avCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs): Int? {
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
            "Panama",
        )

        return if (args.originCountry == null || args.destinationCountry == null) {
            null
        } else if (args.originCountry in domesticCountries && args.destinationCountry in domesticCountries) {
            when (args.fareClass) {
                "C", "J", "D", "A", "K" -> 125
                "Y", "B", "M", "H", "Q", "V", "E", "G", "L", "O", "P", "Z" -> 100
                "T", "W", "S" -> 25
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "C", "J", "D", "A", "K" -> 125
                "Y", "B", "M", "H", "Q", "V", "E", "G", "L", "O", "P", "Z" -> 100
                "T", "W" -> 50
                "S" -> 25
                else -> 0
            }
        }
    }
}

private val brCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "C", "J", "D" -> 125
        "K", "L", "T", "P" -> 100
        "Y", "B" -> 100
        "M", "H" -> 75
        "Q", "S" -> 50
        else -> 0
    }
}

private val caCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
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

private val cmCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = ticketNumber.startsWith("014")

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "C", "J", "D", "R" -> 125
        "Y", "B", "M", "H", "Q", "K", "V", "U", "S", "W", "E", "L", "T" -> 100
        "O", "A" -> 100
        else -> 0
    }
}

private val cxCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs): Int? {
        val other = when {
            args.origin == "HKG" -> args.destination
            args.destination == "HKG" -> args.origin
            else -> null
        }

        if (other !in setOf("CNX", "HKT", "BKK", "CEB", "MNL", "KUL", "SGN", "HAN")) {
            return 0
        }

        // TODO: Must be codeshare
        // Assume 014 is good enough
        if (!args.ticketNumber.startsWith("014")) {
            return 0
        }

        return when (args.fareClass) {
            "F", "A" -> 150
            "J", "C", "D", "P", "I" -> 125
            "W", "R", "E" -> 110
            "Y", "B", "H", "K", "M" -> 100
            "L" -> 50
            "V" -> 25
            else -> 0
        }
    }
}

private val ekCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "F", "A" -> 150
        "J", "C", "I", "O" -> 125
        "H", "W", "E" -> 110
        "R", "Y", "P", "X" -> 100
        "U", "B", "M", "K" -> 50
        "G", "T", "L", "Q" -> 15
        else -> 0
    }
}

private val enCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "C" -> 150
        "D", "Z", "P" -> 125
        "Y", "B", "M" -> 100
        "U", "H", "Q", "V" -> 75
        "W", "S", "T", "L", "K" -> 50
        else -> 0
    }
}

private val etCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "C", "J", "D" -> 125
        "Y", "G", "S", "B" -> 100
        "M", "K", "L", "V" -> 75
        "H", "U", "Q", "T" -> 50
        "W", "E", "O" -> 25
        else -> 0
    }
}

private val ewCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "D" -> 150
        "E", "N" -> 125
        "I", "C", "H", "Q", "V", "W", "S", "G", "K", "L", "T", "X", "Y", "B", "M", "F", "O", "R" -> 50
        else -> 0
    }
}

private val eyCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs): Int {
        // Miles earned on Etihad Airways is limited to flights ticketed and operated by Etihad Airways or flights
        // marketed and ticketed by Air Canada, operated by Etihad Airways
        // TODO: Deal with EY-marketed AC-ticketed
        if (!(args.ticketNumber.startsWith("014") || args.ticketNumber.startsWith("607"))) {
            return 0
        }

        return when (args.fareClass) {
            "P" -> 400
            "F", "A" -> 250
            "J", "C", "D" -> 150
            "W", "Z" -> 125
            "R" -> 110
            "Y", "B" -> 100
            "H", "K", "M", "Q" -> 75
            "L", "V", "U", "E", "G" -> 50
            "T" -> 25
            else -> 0
        }
    }
}

private val g3Calculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "C", "L" -> 150
        "F", "D" -> 125
        "Y", "T", "J" -> 100
        "W", "P", "E", "A" -> 75
        "U", "N", "B" -> 50
        else -> 0
    }
}

private val gfCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "C", "D", "I" -> 125
        "Y" -> 100
        "L", "M", "B", "H" -> 50
        "U", "V", "E", "O", "N", "S", "K", "X", "Q", "W" -> 25
        else -> 0
    }
}

private val hoCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "C" -> 150
        "D", "A" -> 125
        "R" -> 110
        "Y", "B", "M", "U" -> 100
        "H", "Q", "V" -> 75
        "W" -> 50
        else -> 0
    }
}

private val lhCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = ticketNumber.startsWith("014")

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originContinent == null || args.destinationContinent == null) {
            null
        } else if (args.originContinent == "Europe" && args.destinationContinent == "Europe") {
            when (args.fareClass) {
                "J", "C", "D", "Z" -> 150
                "P" -> 50
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> 50
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "F", "A" -> 150
                "J", "C", "D", "Z" -> 150
                "P" -> 100
                "G", "E" -> 125
                "N" -> 100
                "Y", "B" -> 125
                "M", "U", "H", "Q", "V" -> 100
                "W", "S", "T", "L" -> 50
                else -> 0
            }
        }
}

private val loCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
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

private val lxCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = ticketNumber.startsWith("014")

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originContinent == null || args.destinationContinent == null) {
            null
        } else if (args.originContinent == "Europe" && args.destinationContinent == "Europe") {
            when (args.fareClass) {
                "J", "C", "D", "Z" -> 150
                "P" -> 50
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> 50
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "F", "A" -> 150
                "J", "C", "D", "Z" -> 150
                "P" -> 100
                "G", "E" -> 125
                "N" -> 100
                "Y", "B" -> 125
                "M", "U", "H", "Q", "V" -> 100
                "W", "S", "T", "L" -> 50
                else -> 0
            }
        }
}

private val mkCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "D", "C", "R", "I" -> 125
        "Y", "K" -> 100
        "H", "T" -> 75
        "U", "V", "S", "L" -> 50
        "Q", "M", "O", "X", "G", "B", "E", "N" -> 25
        else -> 0
    }
}

private val msCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originCountry == null || args.destinationCountry == null) {
            null
        } else if (args.originCountry == "Egypt" && args.destinationCountry == "Egypt") {
            when (args.fareClass) {
                "C", "D", "J", "Z" -> 125
                "Y", "B", "M", "H" -> 100
                "Q", "K" -> 75
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "C", "D", "J", "Z" -> 125
                "Y", "B", "M", "H" -> 100
                "Q", "K" -> 75
                "V", "L" -> 50
                "G", "S", "W", "T" -> 25
                else -> 0
            }
        }
}

private val nhCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
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

private val nzCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originCountry == null || args.destinationCountry == null ||
            args.originContinent == null || args.destinationContinent == null
        ) {
            null
        } else if (args.originCountry == "New Zealand" && args.destinationCountry == "New Zealand") {
            when (args.fareClass) {
                "C", "D", "J", "Z" -> 125
                "U", "E", "O", "A", "Y", "B" -> 100
                "M", "H", "Q", "V" -> 70
                else -> 0
            }
        } else if (args.originContinent == "Oceania" && args.destinationContinent == "Oceania") {
            when (args.fareClass) {
                "C", "D", "J", "Z" -> 125
                "U", "E", "O", "A", "Y", "B" -> 100
                "M", "H", "Q" -> 70
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "C", "D", "J", "Z" -> 125
                "U", "E", "O", "A", "Y", "B" -> 100
                "M", "H", "Q", "V", "W", "T" -> 70
                else -> 0
            }
        }
}

private val oaCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "A", "C", "D", "Z" -> 125
        "Y", "B", "G", "W", "H", "L", "M", "V", "Q" -> 100
        "O", "J", "S", "K" -> 50
        "U", "T", "P", "E" -> 25
        else -> 0
    }
}

private val osCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = ticketNumber.startsWith("014")

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originContinent == null || args.destinationContinent == null) {
            null
        } else if (args.originContinent == "Europe" && args.destinationContinent == "Europe") {
            when (args.fareClass) {
                "J", "C", "D", "Z" -> 150
                "P" -> 50
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> 50
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "J", "C", "D", "Z" -> 150
                "P" -> 100
                "G", "E" -> 125
                "N" -> 100
                "Y", "B" -> 125
                "M", "U", "H", "Q", "V" -> 100
                "W", "S", "T", "L" -> 50
                else -> 0
            }
        }
}

private val ouCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "C", "D", "Z" -> 125
        "Y", "B" -> 100
        "M", "H", "K", "V", "Q", "A", "F" -> 75
        "W", "S", "J", "O", "P", "G" -> 50
        "T", "E" -> 25
        else -> 0
    }
}

private val ozCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originCountry == null || args.destinationCountry == null) {
            null
        } else if (args.originCountry == "South Korea" && args.destinationCountry == "South Korea") {
            when (args.fareClass) {
                "C" -> 125
                "U", "Y", "B", "A" -> 100
                "M", "H", "E", "Q", "K", "S" -> 50
                "V" -> 25
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "C", "D", "J", "Z" -> 125
                "U", "Y", "B", "M" -> 100
                "A", "H", "E", "Q", "K", "S" -> 50
                "V", "W", "G", "T" -> 25
                else -> 0
            }
        }
}

private val pbCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "Y", "R" -> 125
        "V", "L", "H" -> 75
        "T", "K", "D" -> 25
        else -> 0
    }
}

private val qhCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "C", "I" -> 125
        "Z", "X", "E" -> 110
        "Y", "W", "S", "B" -> 100
        "H", "K", "L", "M", "N" -> 50
        "Q", "T", "O", "R" -> 25
        else -> 0
    }
}

private val saCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originCountry == null || args.destinationCountry == null) {
            null
        } else if (args.originCountry == "South Africa" && args.destinationCountry == "South Africa") {
            when (args.fareClass) {
                "C", "J" -> 150
                "Z" -> 125
                "D" -> 100
                "Y", "B", "M", "K" -> 100
                "H", "S", "Q" -> 50
                "T", "V" -> 50
                "L", "W", "G" -> 25
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "C", "J" -> 150
                "Z", "D", "P" -> 125
                "Y", "B", "M", "K" -> 100
                "H", "S", "Q" -> 50
                "T", "V" -> 50
                "L", "W", "G" -> 25
                else -> 0
            }
        }
}

private val snCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = ticketNumber.startsWith("014")

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
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

private val sqCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "A", "F" -> 150
        "Z", "C", "J", "D", "U" -> 125
        "S", "T", "P", "R", "L" -> 100
        "Y", "B", "E" -> 100
        "M", "H", "W" -> 75
        else -> 0
    }
}

private val tgCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "F", "A", "P" -> 150
        "C", "D", "J", "Z" -> 125
        "Y", "B" -> 110
        "M", "H", "Q", "U" -> 100
        "T", "K", "S" -> 50
        else -> 0
    }
}

private val tkCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "C", "D", "Z", "K" -> 125
        "J" -> 110
        "Y", "B", "M", "A", "H" -> 100
        "S", "O", "E", "Q", "T", "L" -> 70
        "V" -> 25
        else -> 0
    }
}

private val tpCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs): Int {
        val specialDestinations = setOf("LIS", "OPO", "PXO", "FNC")
        return if (args.origin in specialDestinations && args.destination in specialDestinations) {
            when (args.fareClass) {
                "C", "D", "Z", "J" -> 150
                "Y", "B" -> 100
                "M", "H", "Q", "W", "K", "U" -> 100
                "V", "S", "L", "G", "A", "P", "E" -> 50
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "C", "D", "Z", "J" -> 150
                "Y", "B" -> 100
                "M", "H", "Q" -> 100
                "V", "W", "S", "L", "K", "U", "G", "A", "P" -> 50
                else -> 0
            }
        }
    }
}

private val uaCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = ticketNumber.startsWith("014")

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J", "C", "D", "Z", "P" -> 150
        "O", "A" -> 125
        "R" -> 100
        "Y", "B" -> 125
        "M", "E", "U", "H" -> 100
        "Q", "V", "W" -> 75
        "S", "T", "L" -> 50
        "K", "G" -> 25
        "N" -> 25

        else -> 0
    }

    override fun isEligibleForSqc(args: CalculatorArgs) = args.fareClass != "N"
}

private val ukCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "C", "J", "D", "Z" -> 125
        "S", "T", "P", "R", "Y", "B", "M" -> 100
        "A", "H", "N", "Q", "V" -> 50
        "E", "O" -> 20
        else -> 0
    }
}


private val vaCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "J" -> 150
        "C", "D" -> 125
        "A", "Y", "B", "W", "H", "K", "L" -> 100
        "R", "E", "O", "N", "V", "P", "Q", "T", "I", "S", "F", "U" -> 50
        "M" -> 25
        "G" -> 50
        else -> 0
    }
}

private val wyCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "P", "F" -> 150
        "J", "C", "D", "R" -> 125
        "Y", "H", "M", "B" -> 100
        "K", "I", "Q", "T" -> 50
        "N", "L", "U", "O", "G" -> 25
        "E" -> 10
        else -> 0
    }
}

private val ynCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "Y" -> 100
        "V", "Q", "B" -> 75
        "E", "H", "L" -> 50
        else -> 0
    }
}

private val zhCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
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

private val _4yCalculator = object : StarAllianceEarningCalculator() {
    override fun isEligibleForElitePointsBonus(ticketNumber: String) = false

    override fun getDistancePercentMultiplier(args: CalculatorArgs) =
        if (args.originContinent == null || args.destinationContinent == null) {
            null
        } else if (args.originContinent == "Europe" && args.destinationContinent == "Europe") {
            when (args.fareClass) {
                "J", "C", "D", "Z" -> 150
                "P" -> 50
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> 50
                else -> 0
            }
        } else {
            when (args.fareClass) {
                "J", "C", "D", "Z" -> 150
                "P" -> 100
                "G", "E" -> 125
                "N" -> 100
                "Y", "B" -> 125
                "M", "U", "H", "Q", "V" -> 100
                "W", "S", "T", "L" -> 50
                else -> 0
            }
        }
}

private val _5tCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = when (args.fareClass) {
        "Y" -> 100
        "C", "H", "P" -> 75
        "M", "O" -> 50
        "B", "A", "T" -> 25
        else -> 0
    }
}

private val nonPartnerCalculator = object : NonStarAllianceEarningCalculator() {
    override fun getDistancePercentMultiplier(args: CalculatorArgs) = 0
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
    else -> nonPartnerCalculator // Everything else
}

fun getEarningResult(
    operatingAirline: String,
    marketingAirline: String?,
    origin: String,
    destination: String,
    fareClass: String?,
    fareBasis: String?,
    ticketNumber: String,
    eliteBonusMultiplier: Int,
): EarningResult? {
    val effectiveOperator = when (marketingAirline) {
        "AC" if operatingAirline == "PB" -> "AC"
        "LX" if operatingAirline in setOf("2L", "BT", "WK") -> "LX"
        else -> operatingAirline
    }

    val calculator = getCalculator(effectiveOperator)

    val originCountry = airports[origin]?.country
    val originContinent = countriesToContinent[originCountry]
    val destinationCountry = airports[destination]?.country
    val destinationContinent = countriesToContinent[destinationCountry]

    val distanceResult = getDistanceResult(origin, destination)

    return calculator.calculate(
        CalculatorArgs(
            operatingAirline = effectiveOperator,
            distanceResult = distanceResult,
            origin = origin,
            originCountry = originCountry,
            originContinent = originContinent,
            destination = destination,
            destinationCountry = destinationCountry,
            destinationContinent = destinationContinent,
            fareClass = fareClass,
            fareBasis = fareBasis,
            ticketNumber = ticketNumber,
            eliteBonusMultiplier = eliteBonusMultiplier,
        ),
    )
}

fun getDistanceResult(origin: String, destination: String) = getSegmentDistance(origin, destination).also {
    it.error?.let { error ->
        throw SqdCalculatorException("Error calculating distance for $origin-$destination: $error")
    }
}
