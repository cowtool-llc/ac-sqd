package com.canadiancow.sqd.sqm

import com.canadiancow.sqd.SqdCalculatorException
import com.canadiancow.sqd.distance.DistanceResult
import com.canadiancow.sqd.distance.airports
import com.canadiancow.sqd.distance.getSegmentDistance
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.roundToInt

open class EarningResult(
    val distanceResult: DistanceResult,
    val sqmPercent: Int,
    val aeroplanPointsPercent: Int = sqmPercent,
    val bonusPointsPercent: Int,
    eligibleForMinimumPoints: Boolean,
    baseMinimumPoints: Int = if (eligibleForMinimumPoints) 250 else 0,
    val minimumPoints: Int = (aeroplanPointsPercent * baseMinimumPoints / 100.0).roundToInt(),
    val baseRate: Int?,
    val statusRate: Int?,
    val bonusRate: Int?,
    val isSqdEligible: Boolean,
    var sqd: Double? = null
) {
    private val distance = distanceResult.distance

    val sqm = when {
        distance == null -> null
        sqmPercent == 0 -> 0
        else -> max(distance * sqmPercent / 100, minimumPoints)
    }

    val aeroplanMiles = when {
        distance == null -> null
        aeroplanPointsPercent == 0 -> 0
        else -> max(distance * aeroplanPointsPercent / 100, minimumPoints)
    }

    val bonusPoints = when {
        sqm == null || distance == null -> null
        bonusPointsPercent == 0 -> 0
        else -> min(sqm, distance) * bonusPointsPercent / 100
    }

    val totalMiles = if (aeroplanMiles == null || bonusPoints == null) null else aeroplanMiles + bonusPoints

    val totalRate = if (baseRate != null || statusRate != null || bonusRate != null) {
        listOfNotNull(baseRate, statusRate, bonusRate).sum()
    } else {
        null
    }

    val totalPoints: Int?
        get() {
            sqd?.let {
                if (totalRate != null) {
                    return (it * totalRate).toInt()
                }
            }
            return null
        }
}

open class StarAllianceEarningResult(
    distanceResult: DistanceResult,
    sqmPercent: Int,
    bonusPointsPercent: Int = 0,
    hasAeroplanStatus: Boolean,
    baseRate: Int = 3, // TODO
    statusRate: Int,
    bonusRate: Int?,
    ticketNumber: String
) : EarningResult(
    distanceResult = distanceResult,
    sqmPercent = sqmPercent,
    bonusPointsPercent = bonusPointsPercent,
    eligibleForMinimumPoints = hasAeroplanStatus,
    baseRate = baseRate,
    statusRate = statusRate,
    bonusRate = bonusRate,
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
    statusRate: Int,
    bonusRate: Int
) -> EarningResult?

private enum class BonusPercentage {
    FULL, FIXED_25, NONE
}

private abstract class SimpleStarAllianceEarningCalculator(
    private val bonusPercentage: BonusPercentage = BonusPercentage.NONE
) : EarningCalculator {
    abstract fun getSqmPercentage(fareClass: String): Int

    fun calculate(
        distanceResult: DistanceResult,
        fareClass: String?,
        ticketNumber: String,
        hasAeroplanStatus: Boolean,
        bonusPointsPercentage: Int,
        statusRate: Int,
        bonusRate: Int
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
            bonusPointsPercent = bonusPointsPercent,
            eligibleForMinimumPoints = hasAeroplanStatus,
            baseRate = 3, // TODO
            statusRate = statusRate,
            bonusRate = bonusRate,
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
        statusRate: Int,
        bonusRate: Int
    ) = calculate(
        distanceResult,
        fareClass,
        ticketNumber,
        hasAeroplanStatus,
        bonusPointsPercentage,
        statusRate,
        bonusRate
    )
}

private abstract class SimplePartnerEarningCalculator(
    private val baseMinimumPoints: Int = 250,
    private val alwaysEarnsMinimumPoints: Boolean = false
) : EarningCalculator {
    abstract fun getAeroplanMilesPercentage(fareClass: String): Int

    fun calculate(
        distanceResult: DistanceResult,
        fareClass: String?,
        hasAeroplanStatus: Boolean
    ): EarningResult? {
        if (fareClass == null) {
            return null
        }
        val aeroplanMilesPercent = getAeroplanMilesPercentage(fareClass)
        return EarningResult(
            distanceResult = distanceResult,
            sqmPercent = 0,
            aeroplanPointsPercent = aeroplanMilesPercent,
            bonusPointsPercent = 0,
            eligibleForMinimumPoints = hasAeroplanStatus || alwaysEarnsMinimumPoints,
            baseMinimumPoints = if (hasAeroplanStatus || alwaysEarnsMinimumPoints) baseMinimumPoints else 0,
            baseRate = null, // TODO
            statusRate = null, // TODO
            bonusRate = null, // TODO
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
        statusRate: Int,
        bonusRate: Int
    ) = calculate(distanceResult, fareClass, hasAeroplanStatus)
}

private val acCalculator: EarningCalculator =
    calc@{ distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, fareBasis, _, hasAeroplanStatus, bonusPointsPercentage, statusRate, bonusRate ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirCanada#/
        class ACEarningResult(
            sqmPercent: Int,
            aeroplanPointsPercent: Int = sqmPercent,
            baseRate: Int = 3
        ) : EarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            aeroplanPointsPercent = aeroplanPointsPercent,
            bonusPointsPercent = bonusPointsPercentage,
            eligibleForMinimumPoints = hasAeroplanStatus,
            baseRate = baseRate,
            statusRate = statusRate,
            bonusRate = bonusRate,
            isSqdEligible = sqmPercent > 0
        )

        if (!fareBasis.isNullOrEmpty()) {
            // TODO: Remove BP00 after 2022-11-06
            if (fareBasis.endsWith("BP00") || fareBasis.contains("AERO")) {
                return@calc ACEarningResult(sqmPercent = 0, baseRate = 0)
            }

            val trueBasis = fareBasis.split("/").first()
            val brand = trueBasis.substring(trueBasis.length - 2, trueBasis.length)

            when (brand) {
                "BA", "GT" ->
                    return@calc if (originCountry == null || destinationCountry == null) {
                        null
                    } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                        ACEarningResult(sqmPercent = 0, aeroplanPointsPercent = 10, baseRate = 2)
                    } else if ((originCountry == "Canada" && destinationCountry == "United States") ||
                        (originCountry == "United States" && destinationCountry == "Canada")
                    ) {
                        ACEarningResult(sqmPercent = 0, aeroplanPointsPercent = 25, baseRate = 2)
                    } else {
                        ACEarningResult(sqmPercent = 25, baseRate = 2)
                    }
                "TG" ->
                    return@calc if (originCountry == null || destinationCountry == null) {
                        null
                    } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                        ACEarningResult(sqmPercent = 25)
                    } else {
                        ACEarningResult(sqmPercent = 50)
                    }
                "FL" -> return@calc ACEarningResult(sqmPercent = 100)
                "CO" -> return@calc ACEarningResult(sqmPercent = 115)
                "LT" -> return@calc ACEarningResult(sqmPercent = 125)
                "PL" -> return@calc ACEarningResult(sqmPercent = 125)
                "PF" -> return@calc ACEarningResult(sqmPercent = 125)
                "EL" -> return@calc ACEarningResult(sqmPercent = 150)
                "EF" -> return@calc ACEarningResult(sqmPercent = 150)
            }
        }

        val trueFareClass = if (fareClass == "R") {
            if (fareBasis.isNullOrBlank()) {
                fareClass // Not really, but we want to continue
            } else {
                fareBasis.substring(0, 1)
            }
        } else {
            fareClass
        }

        when (trueFareClass) {
            "J", "C", "D", "Z", "P" -> ACEarningResult(sqmPercent = 150)
            "O", "E", "N" -> ACEarningResult(sqmPercent = 125)
            "Y", "B" -> ACEarningResult(sqmPercent = 125)
            "M", "U", "H", "Q", "V" -> ACEarningResult(sqmPercent = 100)
            "W", "G" ->
                if (originCountry == null || destinationCountry == null) {
                    null
                } else if ((originCountry == "Canada" || originCountry == "United States") &&
                    (destinationCountry == "Canada" || destinationCountry == "United States")
                ) {
                    ACEarningResult(sqmPercent = 100)
                } else {
                    ACEarningResult(sqmPercent = 50)
                }
            "S", "T", "L", "A", "K" ->
                if (originCountry == null || destinationCountry == null) {
                    null
                } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                    ACEarningResult(sqmPercent = 25)
                } else {
                    ACEarningResult(sqmPercent = 50)
                }
            null -> null
            else -> ACEarningResult(sqmPercent = 0)
        }
    }

private val a3Calculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Aegean#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "A", "C", "D", "Z" -> 125
        "Y", "B", "G", "W", "H", "L", "M", "V", "Q" -> 100
        "O", "J", "S", "K" -> 50
        "U", "T", "P", "E" -> 25
        else -> 0
    }
}

private val adCalculator = object : SimplePartnerEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AzulAirlines#/
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "J", "C", "D", "I" -> 150
        "Y", " B", " A", " E", " F", " G", " H", " K", " L", " M", " N", " O" -> 100
        "P", "Q" -> 75
        "S", "T", "U" -> 50
        "X", "Z" -> 25
        else -> 0
    }
}

private val aiCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirIndia#/
        class AIEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "India" && destinationCountry == "India") {
            when (fareClass) {
                "F", "A" -> AIEarningResult(sqmPercent = 150)
                "C", "D", "J", "Z" -> AIEarningResult(sqmPercent = 125)
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> AIEarningResult(sqmPercent = 100)
                "L" -> AIEarningResult(sqmPercent = 50)
                "U", "T", "S", "E" -> AIEarningResult(sqmPercent = 25)
                else -> AIEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F", "A" -> AIEarningResult(sqmPercent = 150)
                "C", "D", "J", "Z" -> AIEarningResult(sqmPercent = 125)
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> AIEarningResult(sqmPercent = 100)
                "L", "U", "T", "S", "E" -> AIEarningResult(sqmPercent = 50)
                else -> AIEarningResult(sqmPercent = 0)
            }
        }
    }

private val avCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AviancaTaca#/
        class AVEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=EvaAir#/
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirChina#/
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Copa#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "J", "D", "R" -> 125
        "Y", "B", "M", "H", "Q", "K", "V", "U", "S", "W", "E", "L", "T" -> 100
        "O", "A" -> 100
        else -> 0
    }
}

private val cxCalculator: EarningCalculator =
    calc@{ distanceResult, origin, _, _, destination, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, _, _, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=CathayPacific#/
        class CXEarningResult(
            aeroplanMilesPercent: Int
        ) : EarningResult(
            distanceResult = distanceResult,
            sqmPercent = 0,
            aeroplanPointsPercent = aeroplanMilesPercent,
            bonusPointsPercent = 0,
            eligibleForMinimumPoints = hasAeroplanStatus,
            baseRate = null, // TODO
            statusRate = null, // TODO
            bonusRate = null, // TODO
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

private val etCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=EthiopianAirlines#/
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Eurowings#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "J", "D" -> 150
        "E", "N" -> 125
        "I", "C", "H", "Q", "V", "W", "S", "G", "K", "L", "T", "X", "Y", "B", "M", "F", "O", "R" -> 50
        else -> 0
    }
}

private val eyCalculator: EarningCalculator =
    { distanceResult, _, _, _, _, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, _, _, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=EtihadAirways#/
        class EYEarningResult(
            aeroplanMilesPercent: Int
        ) : EarningResult(
            distanceResult = distanceResult,
            sqmPercent = 0,
            aeroplanPointsPercent = aeroplanMilesPercent,
            bonusPointsPercent = 0,
            eligibleForMinimumPoints = hasAeroplanStatus,
            baseRate = null, // TODO
            statusRate = null, // TODO
            bonusRate = null, // TODO
            isSqdEligible = false
        )

        // Miles earned on Etihad Airways is limited to flights ticketed and operated by Etihad Airways or flights marketed
        // and ticketed by Air Canada, operated by Etihad Airways
        // TODO: Deal with EY-marketed AC-ticketed
        if (!(ticketNumber.startsWith("014") || ticketNumber.startsWith("607"))) {
            EYEarningResult(aeroplanMilesPercent = 0)
        }

        when (fareClass) {
            "P" -> EYEarningResult(aeroplanMilesPercent = 250)
            "F", "A", "R" -> EYEarningResult(aeroplanMilesPercent = 150)
            "J", "C", "D", "W" -> EYEarningResult(aeroplanMilesPercent = 125)
            "Z" -> EYEarningResult(aeroplanMilesPercent = 115)
            "Y", "B", "H" -> EYEarningResult(aeroplanMilesPercent = 100)
            "K", "M", "Q", "L" -> EYEarningResult(aeroplanMilesPercent = 75)
            "V", "U", "G" -> EYEarningResult(aeroplanMilesPercent = 50)
            "E", "T" -> EYEarningResult(aeroplanMilesPercent = 25)
            else -> EYEarningResult(aeroplanMilesPercent = 0)
        }
    }

private val g3Calculator = object : SimplePartnerEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=GOL#/
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "C", "L" -> 150
        "F", "D" -> 125
        "Y", "T", "J" -> 100
        "W", "P", "E", "A" -> 75
        "U", "N", "B" -> 50
        else -> 0
    }
}

private val hoCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Juneyao#/
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
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=LufthansaAirways#/
        class LHEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = if (bonusPointsPercentage > 0) 25 else 0,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=LotAirlines#/
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
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=SwissAir#/
        class LXEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = if (bonusPointsPercentage > 0) 25 else 0,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null,
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> LXEarningResult(sqmPercent = 150)
                "P" -> LXEarningResult(sqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L", "G" -> LXEarningResult(sqmPercent = 50)
                else -> LXEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F", "A" -> LXEarningResult(sqmPercent = 150)
                "J", "C", "D", "Z" -> LXEarningResult(sqmPercent = 150)
                "P" -> LXEarningResult(sqmPercent = 100)
                "Y", "B" -> LXEarningResult(sqmPercent = 125)
                "M", "U", "H", "Q", "V" -> LXEarningResult(sqmPercent = 100)
                "W", "S", "T", "L", "G" -> LXEarningResult(sqmPercent = 50)
                else -> LXEarningResult(sqmPercent = 0)
            }
        }
    }

private val msCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=EgyptAir#/
        class MSEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=ANA#/
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
    { distanceResult, _, originCountry, originContinent, _, destinationCountry, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirNewZealand#/
        class NZEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=OlympicAir#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "A", "C", "D", "Z" -> 125
        "Y", "B", "G", "W", "H", "L", "M", "V", "Q" -> 100
        "O", "J", "S", "K" -> 50
        "U", "T", "P", "E" -> 25
        else -> 0
    }
}

private val osCalculator: EarningCalculator =
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AustrianAirlines#/
        class OSEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = if (bonusPointsPercentage > 0) 25 else 0,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=CroatiaAirlines#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D", "Z" -> 125
        "Y", "B" -> 100
        "M", "H", "K", "V", "Q", "A", "F" -> 75
        "W", "S", "J", "O", "P", "G" -> 50
        "T", "E" -> 25
        else -> 0
    }
}

private val ozCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Asiana#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D", "J", "Z" -> 125
        "U" -> 100
        "Y", "B", "M" -> 100
        "H", "E", "Q", "K", "S" -> 50
        "V", "W", "G", "T" -> 25
        else -> 0
    }
}

private val saCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAeroplanStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=SouthAfricanAirways#/
        class SAEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
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

private val skCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=SASScandinavian#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D", "Z" -> 125
        "A", "J" -> 125
        "Y", "S", "B", "P" -> 125
        "M", "H", "Q", "V", "E", "W" -> 100
        "U", "K", "L", "G" -> 50
        // TODO SAS Go Light Fares are 0
        else -> 0
    }
}

private val snCalculator = object : SimpleStarAllianceEarningCalculator(
    bonusPercentage = BonusPercentage.FIXED_25
) {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=BrusselsAirlines#/
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=SingaporeAirlines#/
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Thai#/
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
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=TurkishAirlines#/
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
    { distanceResult, origin, _, _, destination, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=TapPortugal#/
        class TPEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        val specialDestinations = setOf("LIS", "OPO", "PXO", "FNC")
        if (origin in specialDestinations && destination in specialDestinations) {
            when (fareClass) {
                "C", "D", "Z", "J" -> TPEarningResult(sqmPercent = 200)
                "Y", "B" -> TPEarningResult(sqmPercent = 125)
                "M", "H", "Q", "W", "K", "U" -> TPEarningResult(sqmPercent = 100)
                "V", "S", "L", "G", "A", "P", "E", "T" -> TPEarningResult(sqmPercent = 50)
                "O" -> TPEarningResult(sqmPercent = 10)
                else -> TPEarningResult(sqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "Z", "J" -> TPEarningResult(sqmPercent = 200)
                "Y", "B" -> TPEarningResult(sqmPercent = 125)
                "M", "H", "Q" -> TPEarningResult(sqmPercent = 100)
                "V", "W", "S", "L", "K", "U", "G", "A", "P" -> TPEarningResult(sqmPercent = 50)
                "O", "E", "T" -> TPEarningResult(sqmPercent = 10)
                else -> TPEarningResult(sqmPercent = 0)
            }
        }
    }

private val uaCalculator: EarningCalculator =
    { distanceResult, _, _, _, _, _, _, fareClass, _, ticketNumber, hasAeroplanStatus, bonusPointsPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=United#/
        class UAEarningResult(
            sqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            sqmPercent = sqmPercent,
            bonusPointsPercent = bonusPointsPercentage,
            hasAeroplanStatus = hasAeroplanStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        when (fareClass) {
            "J", "C", "D", "Z", "P" -> UAEarningResult(sqmPercent = 150)
            "O", "A", "R" -> UAEarningResult(sqmPercent = 125)
            "Y", "B" -> UAEarningResult(sqmPercent = 125)
            "M", "E", "U", "H", "Q", "V", "W" -> UAEarningResult(sqmPercent = 100)
            "S", "T", "L", "K", "G" -> UAEarningResult(sqmPercent = 50)
            "N" -> EarningResult(
                distanceResult = distanceResult,
                sqmPercent = 0,
                aeroplanPointsPercent = 50,
                bonusPointsPercent = 0,
                eligibleForMinimumPoints = hasAeroplanStatus,
                baseRate = null, // TODO
                statusRate = null, // TODO
                bonusRate = null, // TODO
                isSqdEligible = false
            )
            else -> UAEarningResult(sqmPercent = 0)
        }
    }

private val ynCalculator = object : SimplePartnerEarningCalculator(
    baseMinimumPoints = 500,
    alwaysEarnsMinimumPoints = true
) {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirCreebec#/
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "Y" -> 100
        "V", "Q", "B" -> 75
        "E", "H", "L" -> 50
        else -> 0
    }
}

private val zhCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Shenzhen#/
    override fun getSqmPercentage(fareClass: String) = when (fareClass) {
        "J", "C" -> 150
        "D", "Z", "R" -> 125
        "G" -> 100
        "Y", "B", "M", "U" -> 100
        "H", "Q" -> 70
        "V", "W", "S", "E", "T", "P" -> 50
        else -> 0
    }
}

private val _5tCalculator = object : SimplePartnerEarningCalculator(
    baseMinimumPoints = 500,
    alwaysEarnsMinimumPoints = true
) {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=CanadianNorth#/
    override fun getAeroplanMilesPercentage(fareClass: String) = when (fareClass) {
        "Y" -> 100
        "C", "H", "V", "P" -> 75
        "M", "O" -> 50
        "B", "A", "T" -> 25
        else -> 0
    }
}

private fun getCalculator(operatingAirline: String) = when (operatingAirline.toUpperCase()) {
    "AC", "KV", "QK", "RV", "ZX" -> acCalculator // Air Canada
    "A3" -> a3Calculator // Aegean Airlines
    "AD" -> adCalculator // Azul Airlines
    "AI" -> aiCalculator // Air India
    "AV" -> avCalculator // Avianca
    "BR" -> brCalculator // EVA Air
    "CA" -> caCalculator // Air China
    "CM" -> cmCalculator // COPA Airlines
    "CX", "KA" -> cxCalculator // Cathay Pacific
    "ET" -> etCalculator // Ethiopian Airlines
    "EW" -> ewCalculator // Eurowings
    "EY" -> eyCalculator // Etihad Airways
    "G3" -> g3Calculator // GOL
    "HO" -> hoCalculator // Juneyao Airlines
    "LH" -> lhCalculator // Lufthansa
    "LO" -> loCalculator // LOT Polish Airlines
    "LX" -> lxCalculator // Swiss
    "MS" -> msCalculator // EgyptAir
    "NH", "NQ" -> nhCalculator // ANA
    "NZ" -> nzCalculator // Air New Zealand
    "OA" -> oaCalculator // Olympic Air
    "OS" -> osCalculator // Austrian
    "OU" -> ouCalculator // Croatia Airlines
    "OZ" -> ozCalculator // Asiana Airlines
    "SA" -> saCalculator // South African Airways
    "SK" -> skCalculator // Scandinavian Airlines
    "SN" -> snCalculator // Brussels Airlines
    "SQ" -> sqCalculator // Singapore Airlines
    "TG" -> tgCalculator // Thai Airways
    "TK" -> tkCalculator // Turkish Airlines
    "TP" -> tpCalculator // TAP Air Portugal
    "UA" -> uaCalculator // United Airlines
    "YN" -> ynCalculator // Air Creebec
    "ZH" -> zhCalculator // Shenzhen Airlines
    "5T" -> _5tCalculator // Canadian North
    else -> null
}

fun getEarningResult(
    operatingAirline: String,
    origin: String,
    destination: String,
    fareClass: String?,
    fareBasis: String?,
    ticketNumber: String,
    hasAeroplanStatus: Boolean,
    bonusPointsPercentage: Int,
    statusRate: Int,
    bonusRate: Int
): EarningResult? {
    val calculator = getCalculator(operatingAirline) ?: return null

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
        statusRate,
        bonusRate
    )
}

fun getDistanceResult(origin: String, destination: String) = getSegmentDistance(origin, destination).also {
    it.error?.let { error ->
        throw SqdCalculatorException("Error calculating distance for $origin-$destination: $error")
    }
}
