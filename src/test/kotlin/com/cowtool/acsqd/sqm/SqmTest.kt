package com.cowtool.acsqd.sqm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class SqmTest {
    @Test
    fun `getEarningResult() handles AC flights`() {
        assertNull(
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                null,
                null,
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            ),
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "Y",
                "YCONFPLT",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "RV",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "J1234EF",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "Z",
                "Z1234EL",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "R",
                "O1234PF",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "E",
                "E1234PL",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "M",
                "M1234FL",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "K",
                "K1234CO",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "R",
                "G1234FL",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            2,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            2,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            2,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "LHR",
                "G",
                "GFAKE",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            2,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            2,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "YVR",
                "YYZ",
                "G",
                "G1234TG",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.distanceResult.distance,
        )
        getEarningResult(
            "AC",
            marketingAirline = null,
            "YVR",
            "LHR",
            "G",
            "G123LBA",
            ticketNumber = "014",
            eliteBonusMultiplier = 3,
        )!!.let {
            it.eligibleDollars = 100
            assertEquals(400, it.totalPoints)
            assertEquals(0, it.sqc)
        }
        getEarningResult(
            "AC",
            marketingAirline = null,
            "YVR",
            "SFO",
            "G",
            "G123LBA",
            ticketNumber = "014",
            eliteBonusMultiplier = 2,
        )!!.let {
            it.eligibleDollars = 100
            assertEquals(300, it.totalPoints)
            assertEquals(0, it.sqc)
        }
        getEarningResult(
            "AC",
            marketingAirline = null,
            "YVR",
            "YYZ",
            "G",
            "G123LBA",
            ticketNumber = "014",
            eliteBonusMultiplier = 1,
        )!!.let {
            it.eligibleDollars = 100
            assertEquals(200, it.totalPoints)
            assertEquals(0, it.sqc)
        }
    }

    @Test
    fun `getEarningResult() handles non-014 partner flights`() {
        getEarningResult(
            "TG",
            marketingAirline = null,
            "MUC",
            "BKK",
            "M",
            "FL",
            ticketNumber = "016",
            eliteBonusMultiplier = 3,
        )!!.let {
            it.eligibleDollars = 100
            assertEquals(5_449, it.totalPoints)
            assertEquals(1089, it.sqc)
            assertEquals(0, it.eliteBonusMultiplier)
        }
    }

    @Test
    fun `getEarningResult() handles elite points bonus`() {
        getEarningResult(
            operatingAirline = "TG",
            marketingAirline = null,
            origin = "MUC",
            destination = "BKK",
            fareClass = "M",
            fareBasis = "FL",
            ticketNumber = "014",
            eliteBonusMultiplier = 3,
        )!!.let {
            it.eligibleDollars = 100
            assertEquals(0, it.eliteBonusMultiplier)
        }
    }

    @Test
    fun `getEarningResult() handles Aeroplan SQC`() {
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
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
                eliteBonusMultiplier = 0,
            ),
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
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
    }

    @Test
    fun `getEarningResult() handles AC single character fare bases`() {
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "Y",
                "Y",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
        assertEquals(
            4,
            getEarningResult(
                "AC",
                marketingAirline = null,
                "SFO",
                "YYZ",
                "J",
                "J",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!.sqcMultiplier,
        )
    }

    @Test
    fun `getEarningResult() handles LX marketed, WK operated segments`() {
        with(
            getEarningResult(
                "WK",
                marketingAirline = "LX",
                "CPT",
                "ZRH",
                "M",
                "M",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
        }
    }

    @Test
    fun `getEarningResult() handles AC Express (PAL) segments`() {
        with(
            getEarningResult(
                "PB",
                marketingAirline = "AC",
                "YDF",
                "YHZ",
                "M",
                "M",
                ticketNumber = "014",
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
        }
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(400, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(0, sqc)
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
                eliteBonusMultiplier = 0,
            )!!,
        ) {
            eligibleDollars = 100
            assertEquals(0, sqc)
        }
    }
}
