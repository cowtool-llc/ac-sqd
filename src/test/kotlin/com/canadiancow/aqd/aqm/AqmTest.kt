package com.canadiancow.aqd.aqm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class AqmTest {
    @Test
    fun `getEarningResult() handles AC AQM`() {
        assertNull(
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                null,
                null,
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "AC",
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "AC",
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "AC",
                "YVR",
                "TPE",
                "G",
                "G123LGT",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0
            )!!.aqmPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "AC",
                "YVR",
                "TPE",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0
            )!!.aqmPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "AC",
                "YVR",
                "LHR",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAltitudeStatus = false,
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
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
                bonusMilesPercentage = 0
            )!!.aqmPercent
        )
    }

    @Test
    fun `getEarningResult() handles unknown fare classes`() {
        assertNull(getEarningResult("A3", "YYZ", "YUL", null, null, "014", true, bonusMilesPercentage = 0))
        assertEquals(
            0,
            getEarningResult("A3", "YYZ", "YUL", "F", null, "014", true, bonusMilesPercentage = 0)!!.aqmPercent
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
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
                bonusMilesPercentage = 0
            )!!.minimumMiles
        )
    }

    @Test
    fun `getEarningResult calculates the correct bonus mileage`() {
        assertEquals(
            100,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonusPercent
        )
        assertEquals(
            75,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 75
            )!!.bonusPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 50
            )!!.bonusPercent
        )
        assertEquals(
            35,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 35
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 25
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )

        assertEquals(
            75,
            getEarningResult(
                "UA",
                "SFO",
                "LAX",
                "W",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 75
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "UA",
                "SFO",
                "LAX",
                "W",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )

        assertEquals(
            75,
            getEarningResult(
                "CM",
                "SFO",
                "PTY",
                "W",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 75
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "CM",
                "SFO",
                "PTY",
                "W",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )

        assertEquals(
            25,
            getEarningResult(
                "OS",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "OS",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 35
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "OS",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 25
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "OS",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )

        assertEquals(
            25,
            getEarningResult(
                "SN",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "SN",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 35
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "SN",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 25
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "SN",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )

        assertEquals(
            25,
            getEarningResult(
                "LH",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "LH",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 35
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "LH",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 25
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "LH",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )

        assertEquals(
            25,
            getEarningResult(
                "LX",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "LX",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 35
            )!!.bonusPercent
        )
        assertEquals(
            25,
            getEarningResult(
                "LX",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 25
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "LX",
                "FRA",
                "SFO",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )

        assertEquals(
            0,
            getEarningResult(
                "BR",
                "SFO",
                "TPE",
                "W",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 75
            )!!.bonusPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "BR",
                "SFO",
                "TPE",
                "W",
                fareBasis = null,
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 0
            )!!.bonusPercent
        )
    }

    @Test
    fun `getEarningResult calculates the proper bonus`() {
        assertEquals(
            2254,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
        assertEquals(
            2254,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "O",
                "O1234PF",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
        assertEquals(
            2254,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "N",
                "N1234PL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
        assertEquals(
            2254,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "Y",
                "Y1234LT",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
        assertEquals(
            2254,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "M",
                "M1234CO",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
        assertEquals(
            2254,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "V",
                "V1234FL",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
        assertEquals(
            1127,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                "SFO",
                "YYZ",
                "K",
                "K1234BA",
                ticketNumber = "014",
                hasAltitudeStatus = true,
                bonusMilesPercentage = 100
            )!!.bonus
        )
    }
}
