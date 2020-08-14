package com.canadiancow.aqd.aqm

import com.canadiancow.aqd.AqdCalculatorException
import com.canadiancow.aqd.distance.DistanceResult
import com.canadiancow.aqd.distance.airports
import com.canadiancow.aqd.distance.getSegmentDistance
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.roundToInt

open class EarningResult(
    val distanceResult: DistanceResult,
    val aqmPercent: Int,
    val sqmPercent: Int,
    val aeroplanMilesPercent: Int = aqmPercent,
    val bonusMilesPercent: Int,
    eligibleForMinimumMiles: Boolean,
    baseMinimumMiles: Int = if (eligibleForMinimumMiles) 250 else 0,
    val minimumMiles: Int = (aeroplanMilesPercent * baseMinimumMiles / 100.0).roundToInt(),
    val baseRate: Int?,
    val statusRate: Int?,
    val bonusRate: Int?,
    val isAqdEligible: Boolean,
    var sqd: Double? = null
) {
    private val distance = distanceResult.distance

    val aqm = when {
        distance == null -> null
        aqmPercent == 0 -> 0
        else -> max(distance * aqmPercent / 100, minimumMiles)
    }

    val aeroplanMiles = when {
        distance == null -> null
        aeroplanMilesPercent == 0 -> 0
        else -> max(distance * aeroplanMilesPercent / 100, minimumMiles)
    }

    val bonusMiles = when {
        aqm == null || distance == null -> null
        bonusMilesPercent == 0 -> 0
        else -> min(aqm, distance) * bonusMilesPercent / 100
    }

    val totalMiles = if (aeroplanMiles == null || bonusMiles == null) null else aeroplanMiles + bonusMiles

    val sqm = when {
        distance == null -> null
        sqmPercent == 0 -> 0
        else -> max(distance * sqmPercent / 100, minimumMiles)
    }

    val totalRate = if (baseRate != null || statusRate != null || bonusRate != null) {
        listOfNotNull(baseRate, statusRate, bonusRate).sum()
    } else {
        null
    }

    val totalPoints: Int?
        get() = sqd?.let { if (totalRate != null) (it * totalRate).toInt() else null }
}

open class StarAllianceEarningResult(
    distanceResult: DistanceResult,
    aqmPercent: Int,
    bonusMilesPercent: Int = 0,
    hasAltitudeStatus: Boolean,
    baseRate: Int = 3, // TODO
    statusRate: Int,
    bonusRate: Int?,
    ticketNumber: String
) : EarningResult(
    distanceResult = distanceResult,
    aqmPercent = aqmPercent,
    sqmPercent = aqmPercent,
    bonusMilesPercent = bonusMilesPercent,
    eligibleForMinimumMiles = hasAltitudeStatus,
    baseRate = baseRate,
    statusRate = statusRate,
    bonusRate = bonusRate,
    isAqdEligible = aqmPercent > 0 && ticketNumber.startsWith("014")
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
    hasAltitudeStatus: Boolean,
    bonusMilesPercentage: Int,
    statusRate: Int,
    bonusRate: Int
) -> EarningResult?

private enum class BonusPercentage {
    FULL, FIXED_25, NONE
}

private abstract class SimpleStarAllianceEarningCalculator(
    private val bonusPercentage: BonusPercentage = BonusPercentage.NONE
) : EarningCalculator {
    abstract fun getAqmPercentage(fareClass: String): Int

    fun calculate(
        distanceResult: DistanceResult,
        fareClass: String?,
        ticketNumber: String,
        hasAltitudeStatus: Boolean,
        bonusMilesPercentage: Int,
        statusRate: Int,
        bonusRate: Int
    ): EarningResult? {
        if (fareClass == null) {
            return null
        }
        val percentage = getAqmPercentage(fareClass)
        val bonusMilesPercent = if (bonusMilesPercentage > 0) {
            when (bonusPercentage) {
                BonusPercentage.FULL -> bonusMilesPercentage
                BonusPercentage.FIXED_25 -> 25
                BonusPercentage.NONE -> 0
            }
        } else {
            0
        }
        return EarningResult(
            distanceResult = distanceResult,
            aqmPercent = percentage,
            sqmPercent = percentage,
            bonusMilesPercent = bonusMilesPercent,
            eligibleForMinimumMiles = hasAltitudeStatus,
            baseRate = 3, // TODO
            statusRate = statusRate,
            bonusRate = bonusRate,
            isAqdEligible = ticketNumber.startsWith("014") && percentage > 0
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
        hasAltitudeStatus: Boolean,
        bonusMilesPercentage: Int,
        statusRate: Int,
        bonusRate: Int
    ) = calculate(
        distanceResult,
        fareClass,
        ticketNumber,
        hasAltitudeStatus,
        bonusMilesPercentage,
        statusRate,
        bonusRate
    )
}

private abstract class SimplePartnerEarningCalculator(
    private val baseMinimumMiles: Int = 250,
    private val alwaysEarnsMinimumMiles: Boolean = false
) : EarningCalculator {
    abstract fun getAeroplanMilesPercentage(fareClass: String): Int

    fun calculate(
        distanceResult: DistanceResult,
        fareClass: String?,
        hasAltitudeStatus: Boolean
    ): EarningResult? {
        if (fareClass == null) {
            return null
        }
        val aeroplanMilesPercent = getAeroplanMilesPercentage(fareClass)
        return EarningResult(
            distanceResult = distanceResult,
            aqmPercent = 0,
            sqmPercent = 0,
            aeroplanMilesPercent = aeroplanMilesPercent,
            bonusMilesPercent = 0,
            eligibleForMinimumMiles = hasAltitudeStatus || alwaysEarnsMinimumMiles,
            baseMinimumMiles = if (hasAltitudeStatus || alwaysEarnsMinimumMiles) baseMinimumMiles else 0,
            baseRate = null, // TODO
            statusRate = null, // TODO
            bonusRate = null, // TODO
            isAqdEligible = false
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
        hasAltitudeStatus: Boolean,
        bonusMilesPercentage: Int,
        statusRate: Int,
        bonusRate: Int
    ) = calculate(distanceResult, fareClass, hasAltitudeStatus)
}

private val acCalculator: EarningCalculator =
    calc@{ distanceResult, _, originCountry, originContinent, _, destinationCountry, destinationContinent, fareClass, fareBasis, _, hasAltitudeStatus, bonusMilesPercentage, statusRate, bonusRate ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirCanada#/
        class ACEarningResult(
            aqmPercent: Int,
            sqmPercent: Int,
            baseRate: Int = 3
        ) : EarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            sqmPercent = sqmPercent,
            bonusMilesPercent = bonusMilesPercentage,
            eligibleForMinimumMiles = hasAltitudeStatus,
            baseRate = baseRate,
            statusRate = statusRate,
            bonusRate = bonusRate,
            isAqdEligible = aqmPercent > 0
        )

        if (!fareBasis.isNullOrEmpty()) {
            if (fareBasis.endsWith("BP00")) {
                return@calc ACEarningResult(aqmPercent = 0, sqmPercent = 0, baseRate = 0)
            }

            val trueBasis = fareBasis.split("/").first()
            val brand = trueBasis.substring(trueBasis.length - 2, trueBasis.length)

            when (brand) {
                "BA", "GT" ->
                    return@calc if (originCountry == null || destinationCountry == null) {
                        null
                    } else if (originContinent == "Europe" || originContinent == "Asia" ||
                        destinationContinent == "Europe" || destinationContinent == "Asia"
                    ) {
                        ACEarningResult(aqmPercent = 25, sqmPercent = 0, baseRate = 2)
                    } else {
                        ACEarningResult(aqmPercent = 0, sqmPercent = 0, baseRate = 2)
                    }
                "TG" ->
                    return@calc if (originCountry == null || destinationCountry == null) {
                        null
                    } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                        ACEarningResult(aqmPercent = 25, sqmPercent = 50)
                    } else {
                        ACEarningResult(aqmPercent = 50, sqmPercent = 50)
                    }
                "FL" -> return@calc ACEarningResult(aqmPercent = 100, sqmPercent = 100)
                "CO" -> return@calc ACEarningResult(aqmPercent = 115, sqmPercent = 115)
                "LT" -> return@calc ACEarningResult(aqmPercent = 125, sqmPercent = 125)
                "PL" -> return@calc ACEarningResult(aqmPercent = 125, sqmPercent = 125)
                "PF" -> return@calc ACEarningResult(aqmPercent = 125, sqmPercent = 125)
                "EL" -> return@calc ACEarningResult(aqmPercent = 150, sqmPercent = 150)
                "EF" -> return@calc ACEarningResult(aqmPercent = 150, sqmPercent = 150)
            }
        }

        val trueFareClass = if (fareClass == "R") {
            if (fareBasis.isNullOrBlank()) {
                return@calc null
            }
            fareBasis.substring(0, 1)
        } else {
            fareClass
        }

        when (trueFareClass) {
            "J", "C", "D", "Z", "P" -> ACEarningResult(aqmPercent = 150, sqmPercent = 150)
            "O", "E", "N" -> ACEarningResult(aqmPercent = 125, sqmPercent = 125)
            "Y", "B" -> ACEarningResult(aqmPercent = 125, sqmPercent = 125)
            "M", "U", "H", "Q", "V" -> ACEarningResult(aqmPercent = 100, sqmPercent = 100)
            "W", "G" ->
                if (originCountry == null || destinationCountry == null) {
                    null
                } else if ((originCountry == "Canada" || originCountry == "United States") &&
                    (destinationCountry == "Canada" || destinationCountry == "United States")
                ) {
                    ACEarningResult(aqmPercent = 100, sqmPercent = 100)
                } else {
                    ACEarningResult(aqmPercent = 50, sqmPercent = 50)
                }
            "S", "T", "L", "A", "K" ->
                if (originCountry == null || destinationCountry == null) {
                    null
                } else if (originCountry == "Canada" && destinationCountry == "Canada") {
                    ACEarningResult(aqmPercent = 25, sqmPercent = 50)
                } else {
                    ACEarningResult(aqmPercent = 50, sqmPercent = 50)
                }
            null -> null
            else -> ACEarningResult(aqmPercent = 0, sqmPercent = 0)
        }
    }

private val a3Calculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=Aegean#/
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAltitudeStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirIndia#/
        class AIEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "India" && destinationCountry == "India") {
            when (fareClass) {
                "F", "A" -> AIEarningResult(aqmPercent = 150)
                "C", "D", "J", "Z" -> AIEarningResult(aqmPercent = 125)
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> AIEarningResult(aqmPercent = 100)
                "L" -> AIEarningResult(aqmPercent = 50)
                "U", "T", "S", "E" -> AIEarningResult(aqmPercent = 25)
                else -> AIEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F", "A" -> AIEarningResult(aqmPercent = 150)
                "C", "D", "J", "Z" -> AIEarningResult(aqmPercent = 125)
                "Y", "B", "M", "H", "K", "Q", "V", "W", "G" -> AIEarningResult(aqmPercent = 100)
                "L", "U", "T", "S", "E" -> AIEarningResult(aqmPercent = 50)
                else -> AIEarningResult(aqmPercent = 0)
            }
        }
    }

private val avCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAltitudeStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AviancaTaca#/
        class AVEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            hasAltitudeStatus = hasAltitudeStatus,
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
                "C", "J", "D", "A", "K" -> AVEarningResult(aqmPercent = 125)
                "Y", "B", "M", "H", "Q", "V", "E", "G", "L", "O", "P", "Z" -> AVEarningResult(aqmPercent = 100)
                "T", "W", "S" -> AVEarningResult(aqmPercent = 25)
                else -> AVEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "J", "D", "A", "K" -> AVEarningResult(aqmPercent = 125)
                "Y", "B", "M", "H", "Q", "V", "E", "G", "L", "O", "P", "Z" -> AVEarningResult(aqmPercent = 100)
                "T", "W" -> AVEarningResult(aqmPercent = 50)
                "S" -> AVEarningResult(aqmPercent = 25)
                else -> AVEarningResult(aqmPercent = 0)
            }
        }
    }

private val brCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=EvaAir#/
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
        "C", "J", "D", "R" -> 125
        "Y", "B", "M", "H", "Q", "K", "V", "U", "S", "W", "E", "L", "T" -> 100
        "O", "A" -> 100
        else -> 0
    }
}

private val cxCalculator: EarningCalculator =
    calc@{ distanceResult, origin, _, _, destination, _, _, fareClass, _, ticketNumber, hasAltitudeStatus, _, _, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=CathayPacific#/
        class CXEarningResult(
            aeroplanMilesPercent: Int
        ) : EarningResult(
            distanceResult = distanceResult,
            aqmPercent = 0,
            sqmPercent = 0,
            aeroplanMilesPercent = aeroplanMilesPercent,
            bonusMilesPercent = 0,
            eligibleForMinimumMiles = hasAltitudeStatus,
            baseRate = null, // TODO
            statusRate = null, // TODO
            bonusRate = null, // TODO
            isAqdEligible = false
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
        "J", "D" -> 150
        "E", "N" -> 125
        "I", "C", "H", "Q", "V", "W", "S", "G", "K", "L", "T", "X", "Y", "B", "M", "F", "O", "R" -> 50
        else -> 0
    }
}

private val eyCalculator: EarningCalculator =
    { distanceResult, _, _, _, _, _, _, fareClass, _, ticketNumber, hasAltitudeStatus, _, _, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=EtihadAirways#/
        class EYEarningResult(
            aeroplanMilesPercent: Int
        ) : EarningResult(
            distanceResult = distanceResult,
            aqmPercent = 0,
            sqmPercent = 0,
            aeroplanMilesPercent = aeroplanMilesPercent,
            bonusMilesPercent = 0,
            eligibleForMinimumMiles = hasAltitudeStatus,
            baseRate = null, // TODO
            statusRate = null, // TODO
            bonusRate = null, // TODO
            isAqdEligible = false
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAltitudeStatus, bonusMilesPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=LufthansaAirways#/
        class LHEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            bonusMilesPercent = if (bonusMilesPercentage > 0) 25 else 0,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> LHEarningResult(aqmPercent = 150)
                "P" -> LHEarningResult(aqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> LHEarningResult(aqmPercent = 50)
                else -> LHEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F", "A" -> LHEarningResult(aqmPercent = 150)
                "J", "C", "D", "Z" -> LHEarningResult(aqmPercent = 150)
                "P" -> LHEarningResult(aqmPercent = 100)
                "G", "E" -> LHEarningResult(aqmPercent = 125)
                "N" -> LHEarningResult(aqmPercent = 100)
                "Y", "B" -> LHEarningResult(aqmPercent = 125)
                "M", "U", "H", "Q", "V" -> LHEarningResult(aqmPercent = 100)
                "W", "S", "T", "L" -> LHEarningResult(aqmPercent = 50)
                else -> LHEarningResult(aqmPercent = 0)
            }
        }
    }

private val loCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=LotAirlines#/
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAltitudeStatus, bonusMilesPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=SwissAir#/
        class LXEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            bonusMilesPercent = if (bonusMilesPercentage > 0) 25 else 0,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null,
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> LXEarningResult(aqmPercent = 150)
                "P" -> LXEarningResult(aqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L", "G" -> LXEarningResult(aqmPercent = 50)
                else -> LXEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "F", "A" -> LXEarningResult(aqmPercent = 150)
                "J", "C", "D", "Z" -> LXEarningResult(aqmPercent = 150)
                "P" -> LXEarningResult(aqmPercent = 100)
                "Y", "B" -> LXEarningResult(aqmPercent = 125)
                "M", "U", "H", "Q", "V" -> LXEarningResult(aqmPercent = 100)
                "W", "S", "T", "L", "G" -> LXEarningResult(aqmPercent = 50)
                else -> LXEarningResult(aqmPercent = 0)
            }
        }
    }

private val msCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAltitudeStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=EgyptAir#/
        class MSEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "Egypt" && destinationCountry == "Egypt") {
            when (fareClass) {
                "C", "D", "J", "Z" -> MSEarningResult(aqmPercent = 125)
                "Y", "B", "M", "H" -> MSEarningResult(aqmPercent = 100)
                "Q", "K" -> MSEarningResult(aqmPercent = 75)
                else -> MSEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "J", "Z" -> MSEarningResult(aqmPercent = 125)
                "Y", "B", "M", "H" -> MSEarningResult(aqmPercent = 100)
                "Q", "K" -> MSEarningResult(aqmPercent = 75)
                "V", "L" -> MSEarningResult(aqmPercent = 50)
                "G", "S", "W", "T" -> MSEarningResult(aqmPercent = 25)
                else -> MSEarningResult(aqmPercent = 0)
            }
        }
    }

private val nhCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=ANA#/
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    { distanceResult, _, originCountry, originContinent, _, destinationCountry, destinationContinent, fareClass, _, ticketNumber, hasAltitudeStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AirNewZealand#/
        class NZEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            hasAltitudeStatus = hasAltitudeStatus,
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
                "C", "D", "J", "Z" -> NZEarningResult(aqmPercent = 125)
                "U", "E", "O", "A", "Y", "B" -> NZEarningResult(aqmPercent = 100)
                "M", "H", "Q", "V" -> NZEarningResult(aqmPercent = 70)
                else -> NZEarningResult(aqmPercent = 0)
            }
        } else if (originContinent == "Oceania" && destinationContinent == "Oceania") {
            when (fareClass) {
                "C", "D", "J", "Z" -> NZEarningResult(aqmPercent = 125)
                "U", "E", "O", "A", "Y", "B" -> NZEarningResult(aqmPercent = 100)
                "M", "H", "Q" -> NZEarningResult(aqmPercent = 70)
                else -> NZEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "J", "Z" -> NZEarningResult(aqmPercent = 125)
                "U", "E", "O", "A", "Y", "B" -> NZEarningResult(aqmPercent = 100)
                "M", "H", "Q", "V", "W", "T" -> NZEarningResult(aqmPercent = 70)
                else -> NZEarningResult(aqmPercent = 0)
            }
        }
    }

private val oaCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=OlympicAir#/
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
        "A", "C", "D", "Z" -> 125
        "Y", "B", "G", "W", "H", "L", "M", "V", "Q" -> 100
        "O", "J", "S", "K" -> 50
        "U", "T", "P", "E" -> 25
        else -> 0
    }
}

private val osCalculator: EarningCalculator =
    { distanceResult, _, _, originContinent, _, _, destinationContinent, fareClass, _, ticketNumber, hasAltitudeStatus, bonusMilesPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=AustrianAirlines#/
        class OSEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            bonusMilesPercent = if (bonusMilesPercentage > 0) 25 else 0,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        if (originContinent == null || destinationContinent == null) {
            null
        } else if (originContinent == "Europe" && destinationContinent == "Europe") {
            when (fareClass) {
                "J", "C", "D", "Z" -> OSEarningResult(aqmPercent = 150)
                "P" -> OSEarningResult(aqmPercent = 50)
                "Y", "B", "M", "U", "H", "Q", "V", "W", "S", "T", "L" -> OSEarningResult(aqmPercent = 50)
                else -> OSEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "J", "C", "D", "Z" -> OSEarningResult(aqmPercent = 150)
                "P" -> OSEarningResult(aqmPercent = 100)
                "G", "E" -> OSEarningResult(aqmPercent = 125)
                "N" -> OSEarningResult(aqmPercent = 100)
                "Y", "B" -> OSEarningResult(aqmPercent = 125)
                "M", "U", "H", "Q", "V" -> OSEarningResult(aqmPercent = 100)
                "W", "S", "T", "L" -> OSEarningResult(aqmPercent = 50)
                else -> OSEarningResult(aqmPercent = 0)
            }
        }
    }

private val ouCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=CroatiaAirlines#/
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D", "J", "Z" -> 125
        "U" -> 100
        "Y", "B", "M" -> 100
        "H", "E", "Q", "K", "S" -> 50
        "V", "W", "G", "T" -> 25
        else -> 0
    }
}

private val saCalculator: EarningCalculator =
    { distanceResult, _, originCountry, _, _, destinationCountry, _, fareClass, _, ticketNumber, hasAltitudeStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=SouthAfricanAirways#/
        class SAEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        if (originCountry == null || destinationCountry == null) {
            null
        } else if (originCountry == "South Africa" && destinationCountry == "South Africa") {
            when (fareClass) {
                "C", "J" -> SAEarningResult(aqmPercent = 150)
                "Z" -> SAEarningResult(aqmPercent = 125)
                "D" -> SAEarningResult(aqmPercent = 100)
                "Y", "B", "M", "K" -> SAEarningResult(aqmPercent = 100)
                "H", "S", "Q" -> SAEarningResult(aqmPercent = 50)
                "T", "V" -> SAEarningResult(aqmPercent = 50)
                "L", "W", "G" -> SAEarningResult(aqmPercent = 25)
                else -> SAEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "J" -> SAEarningResult(aqmPercent = 150)
                "Z", "D", "P" -> SAEarningResult(aqmPercent = 125)
                "Y", "B", "M", "K" -> SAEarningResult(aqmPercent = 100)
                "H", "S", "Q" -> SAEarningResult(aqmPercent = 50)
                "T", "V" -> SAEarningResult(aqmPercent = 50)
                "L", "W", "G" -> SAEarningResult(aqmPercent = 25)
                else -> SAEarningResult(aqmPercent = 0)
            }
        }
    }

private val skCalculator = object : SimpleStarAllianceEarningCalculator() {
    // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=SASScandinavian#/
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
        "C", "D", "Z", "K" -> 125
        "J" -> 110
        "Y", "B", "M", "A", "H" -> 100
        "S", "O", "E", "Q", "T", "L" -> 70
        "V" -> 25
        else -> 0
    }
}

private val tpCalculator: EarningCalculator =
    { distanceResult, origin, _, _, destination, _, _, fareClass, _, ticketNumber, hasAltitudeStatus, _, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=TapPortugal#/
        class TPEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        val specialDestinations = setOf("LIS", "OPO", "PXO", "FNC")
        if (origin in specialDestinations && destination in specialDestinations) {
            when (fareClass) {
                "C", "D", "Z", "J" -> TPEarningResult(aqmPercent = 200)
                "Y", "B" -> TPEarningResult(aqmPercent = 125)
                "M", "H", "Q", "W", "K", "U" -> TPEarningResult(aqmPercent = 100)
                "V", "S", "L", "G", "A", "P", "E", "T" -> TPEarningResult(aqmPercent = 50)
                "O" -> TPEarningResult(aqmPercent = 10)
                else -> TPEarningResult(aqmPercent = 0)
            }
        } else {
            when (fareClass) {
                "C", "D", "Z", "J" -> TPEarningResult(aqmPercent = 200)
                "Y", "B" -> TPEarningResult(aqmPercent = 125)
                "M", "H", "Q" -> TPEarningResult(aqmPercent = 100)
                "V", "W", "S", "L", "K", "U", "G", "A", "P" -> TPEarningResult(aqmPercent = 50)
                "O", "E", "T" -> TPEarningResult(aqmPercent = 10)
                else -> TPEarningResult(aqmPercent = 0)
            }
        }
    }

private val uaCalculator: EarningCalculator =
    { distanceResult, _, _, _, _, _, _, fareClass, _, ticketNumber, hasAltitudeStatus, bonusMilesPercentage, statusRate, _ ->
        // https://www.aeroplan.com/earn_miles/our_partners/partner_details.do?Partner=United#/
        class UAEarningResult(
            aqmPercent: Int
        ) : StarAllianceEarningResult(
            distanceResult = distanceResult,
            aqmPercent = aqmPercent,
            bonusMilesPercent = bonusMilesPercentage,
            hasAltitudeStatus = hasAltitudeStatus,
            statusRate = statusRate,
            bonusRate = null, // TODO
            ticketNumber = ticketNumber
        )

        when (fareClass) {
            "J", "C", "D", "Z", "P" -> UAEarningResult(aqmPercent = 150)
            "O", "A", "R" -> UAEarningResult(aqmPercent = 125)
            "Y", "B" -> UAEarningResult(aqmPercent = 125)
            "M", "E", "U", "H", "Q", "V", "W" -> UAEarningResult(aqmPercent = 100)
            "S", "T", "L", "K", "G" -> UAEarningResult(aqmPercent = 50)
            "N" -> EarningResult(
                distanceResult = distanceResult,
                aqmPercent = 0,
                sqmPercent = 0,
                aeroplanMilesPercent = 50,
                bonusMilesPercent = 0,
                eligibleForMinimumMiles = hasAltitudeStatus,
                baseRate = null, // TODO
                statusRate = null, // TODO
                bonusRate = null, // TODO
                isAqdEligible = false
            )
            else -> UAEarningResult(aqmPercent = 0)
        }
    }

private val ynCalculator = object : SimplePartnerEarningCalculator(
    baseMinimumMiles = 500,
    alwaysEarnsMinimumMiles = true
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
    override fun getAqmPercentage(fareClass: String) = when (fareClass) {
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
    baseMinimumMiles = 500,
    alwaysEarnsMinimumMiles = true
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
    "NH" -> nhCalculator // ANA
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
    hasAltitudeStatus: Boolean,
    bonusMilesPercentage: Int,
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
        hasAltitudeStatus,
        bonusMilesPercentage,
        statusRate,
        bonusRate
    )
}

fun getDistanceResult(origin: String, destination: String) = getSegmentDistance(origin, destination).also {
    it.error?.let { error ->
        throw AqdCalculatorException("Error calculating distance for $origin-$destination: $error")
    }
}
