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
                marketingAirline = null,
                "SFO",
                "YYZ",
                null,
                null,
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "Y",
                "YCONFPLT",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "I",
                "IJBP00",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "IBP00EL",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            150,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            150,
            getEarningResult(
                "RV",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            150,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "Z",
                "Z1234EL",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "R",
                "O1234PF",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "E",
                "E1234PL",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            100,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            115,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "K",
                "K1234CO",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            100,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "R",
                "G1234FL",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "LHR",
                "G",
                "DUNNO",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            50,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "G",
                "G1234BA",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "TPE",
                "G",
                "G123LGT",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "TPE",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "LHR",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "BOG",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "ALG",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            225,
            getEarningResult(
                "KV",
                marketingAirline = null,
                "YYZ",
                "YOW",
                "G",
                "G123LFL",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.distanceResult.distance
        )
        getEarningResult(
            "AC",
            marketingAirline = null,
            "YVR",
            "LHR",
            "G",
            "G123LBA",
            ticketNumber = "014",
            hasAeroplanStatus = true,
            bonusPointsPercentage = 0,
        )!!.let {
            it.sqd = 100
            assertEquals(1176, it.aeroplanMiles)
            assertEquals(0, it.sqm)
        }
        getEarningResult(
            "AC",
            marketingAirline = null,
            "YVR",
            "SFO",
            "G",
            "G123LBA",
            ticketNumber = "014",
            hasAeroplanStatus = true,
            bonusPointsPercentage = 0,
        )!!.let {
            it.sqd = 100
            assertEquals(200, it.aeroplanMiles)
            assertEquals(0, it.sqm)
        }
        getEarningResult(
            "AC",
            marketingAirline = null,
            "YVR",
            "YYZ",
            "G",
            "G123LBA",
            ticketNumber = "014",
            hasAeroplanStatus = true,
            bonusPointsPercentage = 0,
        )!!.let {
            it.sqd = 100
            assertEquals(208, it.aeroplanMiles)
            assertEquals(0, it.sqm)
        }
    }

    @Test
    fun `getEarningResult() handles Aeroplan SQM`() {
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "I",
                "IBP00EL/AE2",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "I",
                "DTAEROEL/AE1",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "JAEROEF/AE1",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
        assertEquals(
            0,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "M",
                "MAEROFL/AE1",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
    }

    @Test
    fun `getEarningResult() handles unknown fare classes`() {
        assertNull(
            getEarningResult(
                "A3",
                marketingAirline = null,
                "YYZ",
                "YUL",
                null,
                null,
                "014",
                true,
                bonusPointsPercentage = 0,
            )
        )
        assertEquals(
            0,
            getEarningResult(
                "A3",
                marketingAirline = null,
                "YYZ",
                "YUL",
                "F",
                null,
                "014",
                true,
                bonusPointsPercentage = 0,
            )!!.sqmPercent
        )
    }

    @Test
    fun `getEarningResult calculates the correct minimum mileage`() {
        assertEquals(
            375,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YOW",
                "YYZ",
                "Z",
                "Z1234EL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.sqm
        )
        assertEquals(
            313,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YOW",
                "YYZ",
                "R",
                "O1234PF",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.sqm
        )
        assertEquals(
            288,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YOW",
                "YYZ",
                "K",
                "K1234CO",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.sqm
        )
        assertEquals(
            250,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YOW",
                "YYZ",
                "R",
                "G1234FL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.sqm
        )
        assertEquals(
            125,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YOW",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.sqm
        )
        assertEquals(
            63,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "CFK",
                "ALG",
                "G",
                "G123LBA",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.aeroplanMiles
        )

        assertEquals(
            188,
            getEarningResult(
                "HO",
                marketingAirline = null,
                "HKG",
                "SZX",
                "H",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.sqm
        )

        assertEquals(
            500,
            getEarningResult(
                "YN",
                marketingAirline = null,
                "YOW",
                "YUL",
                "Y",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.aeroplanMiles
        )
        assertEquals(
            375,
            getEarningResult(
                "YN",
                marketingAirline = null,
                "YOW",
                "YUL",
                "Q",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.aeroplanMiles
        )
        assertEquals(
            250,
            getEarningResult(
                "YN",
                marketingAirline = null,
                "YOW",
                "YUL",
                "H",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.aeroplanMiles
        )
        assertEquals(
            0,
            getEarningResult(
                "YN",
                marketingAirline = null,
                "YOW",
                "YUL",
                "A",
                fareBasis = null,
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!.aeroplanMiles
        )
    }

    @Test
    fun `some weird minimum points bug`() {
        assertEquals(
            288,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YTZ",
                "YOW",
                "K",
                "K1234CO",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 100,
            )!!.sqm
        )
    }

    @Test
    fun `getEarningResult() handles AC single character fare bases`() {
        assertEquals(
            125,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "Y",
                "Y",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent,
        )
        assertEquals(
            150,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "J",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqmPercent,
        )
    }

    @Test
    fun `getEarningResult() handles LX marketed, WK operated segments`() {
        assertEquals(
            5661,
            getEarningResult(
                "WK",
                marketingAirline = "LX",
                "CPT",
                "ZRH",
                "M",
                "M",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqm,
        )
    }

    @Test
    fun `getEarningResult() handles AC Express (PAL) segments`() {
        assertEquals(
            415,
            getEarningResult(
                "PB",
                marketingAirline = "AC",
                "YDF",
                "YHZ",
                "M",
                "M",
                ticketNumber = "014",
                hasAeroplanStatus = false,
                bonusPointsPercentage = 0,
            )!!.sqm,
        )
    }

    @Test
    fun `getEarningResult() handles paid J seated in Y`() {
        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYJ",
                fareClass = "O",
                fareBasis = "P..EL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!
        ) {
            assertEquals(313, sqm)
            assertEquals(true, isSqmPercentEstimated)
        }

        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYJ",
                fareClass = "Y",
                fareBasis = "D..EL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!
        ) {
            assertEquals(313, sqm)
            assertEquals(isSqmPercentEstimated, true)
        }

        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYJ",
                fareClass = "M",
                fareBasis = "Z..EL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!
        ) {
            assertEquals(288, sqm)
            assertEquals(true, isSqmPercentEstimated)
        }

        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYJ",
                fareClass = "V",
                fareBasis = "P..EL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!
        ) {
            assertEquals(250, sqm)
            assertEquals(true, isSqmPercentEstimated)
        }
    }

    @Test
    fun `getEarningResult() handles paid J seated in PY`() {
        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYJ",
                fareClass = "Y",
                fareBasis = "O..PL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!
        ) {
            assertEquals(313, sqm)
            assertEquals(true, isSqmPercentEstimated)
        }

        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYJ",
                fareClass = "M",
                fareBasis = "E..PL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!
        ) {
            assertEquals(288, sqm)
            assertEquals(true, isSqmPercentEstimated)
        }

        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYJ",
                fareClass = "V",
                fareBasis = "A..EL",
                ticketNumber = "014",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!
        ) {
            assertEquals(250, sqm)
            assertEquals(true, isSqmPercentEstimated)
        }
    }

    @Test
    fun `getEarningResult() handles paid J with I-fare basis`() {
        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYZ",
                fareClass = "P",
                fareBasis = "INX00YN0",
                ticketNumber = "123",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!,
        ) {
            assertEquals(3114, sqm)
        }
    }

    @Test
    fun `partner issued X and I class earns 0 on AC`() {
        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYZ",
                fareClass = "X",
                fareBasis = "RANDOM-LT",
                ticketNumber = "016",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!,
        ) {
            assertEquals(0, sqm)
        }

        with(
            getEarningResult(
                operatingAirline = "AC",
                marketingAirline = "AC",
                origin = "YVR",
                destination = "YYZ",
                fareClass = "I",
                fareBasis = "RANDOM-EL",
                ticketNumber = "016",
                hasAeroplanStatus = true,
                bonusPointsPercentage = 0,
            )!!,
        ) {
            assertEquals(0, sqm)
        }
    }
}
