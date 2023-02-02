package com.cowtool.acsqd.sqm

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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                "J",
                "IBP00EL",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.distanceResult.distance
        )
        getEarningResult(
            "AC",
            "YVR",
            "LHR",
            "G",
            "G123LBA",
            ticketNumber = "014",
            hasAeroplanStatus = true,
            bonusPointsPercentage = 0,
            statusRate = 1,
            bonusRate = 0
        )!!.let {
            it.sqd = 100
            assertEquals(1176, it.aeroplanMiles)
            assertEquals(300, it.totalPoints)
            assertEquals(0, it.sqm)
        }
        getEarningResult(
            "AC",
            "YVR",
            "SFO",
            "G",
            "G123LBA",
            ticketNumber = "014",
            hasAeroplanStatus = true,
            bonusPointsPercentage = 0,
            statusRate = 1,
            bonusRate = 3
        )!!.let {
            it.sqd = 100
            assertEquals(200, it.aeroplanMiles)
            assertEquals(600, it.totalPoints)
            assertEquals(0, it.sqm)
        }
        getEarningResult(
            "AC",
            "YVR",
            "YYZ",
            "G",
            "G123LBA",
            ticketNumber = "014",
            hasAeroplanStatus = true,
            bonusPointsPercentage = 0,
            statusRate = 2,
            bonusRate = 2
        )!!.let {
            it.sqd = 100
            assertEquals(208, it.aeroplanMiles)
            assertEquals(600, it.totalPoints)
            assertEquals(0, it.sqm)
        }
    }

    @Test
    fun `getEarningResult() handles Aeroplan SQM`() {
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "I",
                "IBP00EL/AE2",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                "DTAEROEL/AE1",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                "J",
                "JAEROEF/AE1",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
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
                "M",
                "MAEROFL/AE1",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.sqmPercent
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
                bonusPointsPercentage = 0,
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
                bonusPointsPercentage = 0,
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
                "YOW",
                "YYZ",
                "Z",
                "Z1234EL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 4,
                bonusRate = 0
            )!!.sqm
        )
        assertEquals(
            313,
            getEarningResult(
                "AC",
                "YOW",
                "YYZ",
                "R",
                "O1234PF",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 4,
                bonusRate = 0
            )!!.sqm
        )
        assertEquals(
            288,
            getEarningResult(
                "AC",
                "YOW",
                "YYZ",
                "K",
                "K1234CO",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 4,
                bonusRate = 0
            )!!.sqm
        )
        assertEquals(
            250,
            getEarningResult(
                "AC",
                "YOW",
                "YYZ",
                "R",
                "G1234FL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.sqm
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                "YOW",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.sqm
        )
        assertEquals(
            63,
            getEarningResult(
                "AC",
                "CFK",
                "ALG",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.aeroplanMiles
        )

        assertEquals(
            188,
            getEarningResult(
                "HO",
                "HKG",
                "SZX",
                "H",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.sqm
        )

        assertEquals(
            500,
            getEarningResult(
                "YN",
                "YOW",
                "YUL",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.aeroplanMiles
        )
        assertEquals(
            375,
            getEarningResult(
                "YN",
                "YOW",
                "YUL",
                "Q",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.aeroplanMiles
        )
        assertEquals(
            250,
            getEarningResult(
                "YN",
                "YOW",
                "YUL",
                "H",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.aeroplanMiles
        )
        assertEquals(
            0,
            getEarningResult(
                "YN",
                "YOW",
                "YUL",
                "A",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
                statusRate = 1,
                bonusRate = 0
            )!!.aeroplanMiles
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
                hasAeroplanStatus = true,
                bonusPointsPercentage = 100,
                statusRate = 4,
                bonusRate = 4
            )!!.apply { sqd = 1000 }.totalPoints
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
                hasAeroplanStatus = true,
                bonusPointsPercentage = 100,
                statusRate = 4,
                bonusRate = 4
            )!!.apply { sqd = 800 }.totalPoints
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
                hasAeroplanStatus = true,
                bonusPointsPercentage = 75,
                statusRate = 3,
                bonusRate = 3
            )!!.apply { sqd = 750 }.totalPoints
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
                hasAeroplanStatus = true,
                bonusPointsPercentage = 50,
                statusRate = 2,
                bonusRate = 2
            )!!.apply { sqd = 500 }.totalPoints
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
                hasAeroplanStatus = true,
                bonusPointsPercentage = 100,
                statusRate = 4,
                bonusRate = 4
            )!!.apply { sqd = 400 }.totalPoints
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
                hasAeroplanStatus = true,
                bonusPointsPercentage = 35,
                statusRate = 1,
                bonusRate = 0
            )!!.apply { sqd = 300 }.totalPoints
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
                hasAeroplanStatus = true,
                bonusPointsPercentage = 100,
                statusRate = 4,
                bonusRate = 0
            )!!.apply { sqd = 600 }.totalPoints
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
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
                statusRate = 0,
                bonusRate = 0
            )!!.apply { sqd = 100 }.totalPoints
        )
    }

    @Test
    fun `some weird minimum points bug`() {
        assertEquals(
            288,
            getEarningResult(
                "AC",
                "YTZ",
                "YOW",
                "K",
                "K1234CO",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 100,
                statusRate = 0,
                bonusRate = 0
            )!!.sqm
        )
    }

    @Test
    fun `getEarningResult() handles AC single character fare bases`() {
        assertEquals(
            125,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "Y",
                "Y",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
                statusRate = 0,
                bonusRate = 0,
            )!!.sqmPercent,
        )
        assertEquals(
            150,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "J",
                "J",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
                statusRate = 0,
                bonusRate = 0,
            )!!.sqmPercent,
        )
    }
}
