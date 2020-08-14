package com.canadiancow.aqd.aqm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class SqmTest {
    @Test
    fun `getEarningResult() handles AC SQM`() {
        assertNull(
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                null,
                null,
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "Y",
                "YCONFPLT",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "I",
                "IJBP00",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            150,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            150,
            getEarningResult(
                "RV",
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            150,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "Z",
                "Z1234EL",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "R",
                "O1234PF",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "E",
                "E1234PL",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            100,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            115,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "K",
                "K1234CO",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            100,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "R",
                "G1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                "YVR",
                "LHR",
                "G",
                "DUNNO",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "G",
                "G1234BA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "YVR",
                "TPE",
                "G",
                "G123LGT",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "YVR",
                "TPE",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "YVR",
                "LHR",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "YVR",
                "BOG",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "YVR",
                "ALG",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
        )
        assertEquals(
            225,
            getEarningResult(
                "KV",
                "YYZ",
                "YOW",
                "G",
                "G123LFL",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.distanceResult.distance
        )
    }

    @Test
    fun `getEarningResult() handles unknown fare classes`() {
        assertNull(
            getEarningResult(
                "A3",
                "YYZ",
                "YUL",
                null,
                null,
                "014",
                true,
                bonusMilesPercentage = 0,
                statusRate = 3,
                bonusRate = 0
            )
        )
        assertEquals(
            0,
            getEarningResult(
                "A3",
                "YYZ",
                "YUL",
                "F",
                null,
                "014",
                true,
                bonusMilesPercentage = 0,
                statusRate = 3,
                bonusRate = 0
            )!!.sqmPercent
        )
    }

    @Test
    fun `getEarningResult calculates the correct minimum mileage`() {
        assertEquals(
            375,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "Z",
                "Z1234EL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 4,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            313,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "R",
                "O1234PF",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 4,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            288,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "K",
                "K1234CO",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 4,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            250,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "R",
                "G1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            63,
            getEarningResult(
                "AC",
                "YVR",
                "LHR",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "YVR",
                "ALG",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )

        assertEquals(
            275,
            getEarningResult(
                "CX",
                "HKG",
                "BKK",
                "W",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            188,
            getEarningResult(
                "HO",
                "HKG",
                "BKK",
                "H",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )

        assertEquals(
            500,
            getEarningResult(
                "YN",
                "YOW",
                "YFB",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            375,
            getEarningResult(
                "YN",
                "YOW",
                "YFB",
                "Q",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            250,
            getEarningResult(
                "YN",
                "YOW",
                "YFB",
                "H",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
        assertEquals(
            0,
            getEarningResult(
                "YN",
                "YOW",
                "YFB",
                "A",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.minimumMiles
        )
    }

    @Test
    fun `getEarningResult calculates the proper total`() {
        assertEquals(
            11_000,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100,
                statusRate = 4,
                bonusRate = 4
            )!!.apply { sqd = 1000.00 }.totalPoints
        )
        assertEquals(
            8800,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "O",
                "O1234PF",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100,
                statusRate = 4,
                bonusRate = 4
            )!!.apply { sqd = 800.00 }.totalPoints
        )
        assertEquals(
            6750,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "N",
                "N1234PL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 75,
                statusRate = 3,
                bonusRate = 3
            )!!.apply { sqd = 750.00 }.totalPoints
        )
        assertEquals(
            3500,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "Y",
                "Y1234LT",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 50,
                statusRate = 2,
                bonusRate = 2
            )!!.apply { sqd = 500.00 }.totalPoints
        )
        assertEquals(
            4400,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234CO",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100,
                statusRate = 4,
                bonusRate = 4
            )!!.apply { sqd = 400.00 }.totalPoints
        )
        assertEquals(
            1200,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "V",
                "V1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 35,
                statusRate = 1,
                bonusRate = 0
            )!!.apply { sqd = 300.00 }.totalPoints
        )
        assertEquals(
            4200,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100,
                statusRate = 4,
                bonusRate = 0
            )!!.apply { sqd = 600.00 }.totalPoints
        )
        assertEquals(
            200,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "K",
                "K1234BA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.apply { sqd = 100.00 }.totalPoints
        )
    }
}
