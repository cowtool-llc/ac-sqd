package com.cowtool.acsqd

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SqdCalculatorTest {
    /**
     * Used to test that basic functionality doesn't throw exceptions before bothering to attempt a deployment.
     */
    @Test
    fun `test basic functionality`() {
        println(
            SqdCalculator(
                baseFare = 1000.0,
                surcharges = 500.0,
                ticket = "014",
                aeroplanStatus = "100",
                segments = defaultSegments,
            ).calculate(),
        )
    }

    /**
     * This is a test of SQC rounding to try to match how AC credits.
     */
    @Test
    fun `SQC rounding should match AC`() {
        with(
            SqdCalculator(
                baseFare = 829.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                segments = """
                    AC,SFO,YVR,Y,LT
                    AC,YVR,YYZ,Y,LT
                """.trimIndent(),
            ).calculate(),
        ) {
            assertEquals(924, itinerary!!.segments[0].earningResult!!.sqc)
            assertEquals(2396, itinerary!!.segments[1].earningResult!!.sqc)
        }

        with(
            SqdCalculator(
                baseFare = 1217.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                segments = """
                    AC,YYC,EWR,Y,LT
                    AC,EWR,YYC,Y,LT
                """.trimIndent(),
            ).calculate(),
        ) {
            assertEquals(2436, itinerary!!.segments[0].earningResult!!.sqc)
            assertEquals(2436, itinerary!!.segments[1].earningResult!!.sqc)
        }

        with(
            SqdCalculator(
                baseFare = 3610.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                segments = """
                    AC,YYC,YUL,Y,LT
                    AC,YUL,GRU,Y,LT
                    AC,GRU,EZE,Y,LT
                    AC,EZE,GRU,Y,LT
                    AC,GRU,YUL,Y,LT
                    AC,YUL,YYC,Y,LT
                """.trimIndent(),
            ).calculate(),
        ) {
            assertEquals(1688, itinerary!!.segments[0].earningResult!!.sqc)
            assertEquals(4568, itinerary!!.segments[1].earningResult!!.sqc)
            assertEquals(968, itinerary!!.segments[2].earningResult!!.sqc)
            assertEquals(968, itinerary!!.segments[3].earningResult!!.sqc)
            assertEquals(4568, itinerary!!.segments[4].earningResult!!.sqc)
            assertEquals(1688, itinerary!!.segments[5].earningResult!!.sqc)
        }

        with(
            SqdCalculator(
                baseFare = 2792.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                segments = """
                    AC,YYC,YYZ,Y,LT
                    AC,YYZ,YUL,Y,LT
                    AC,YUL,BOG,Y,LT
                    AC,BOG,YUL,Y,LT
                    AC,YUL,YYC,Y,LT
                """.trimIndent(),
            ).calculate(),
        ) {
            assertEquals(1968, itinerary!!.segments[0].earningResult!!.sqc)
            assertEquals(372, itinerary!!.segments[1].earningResult!!.sqc)
            assertEquals(3316, itinerary!!.segments[2].earningResult!!.sqc)
        }

        with(
            SqdCalculator(
                baseFare = 4245.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                segments = """
                    AC,YEG,YVR,P,EL
                    AC,YVR,YYZ,P,EL
                    AC,YYZ,DUB,P,EL
                    AC,DUB,YYZ,P,EL
                    AC,YYZ,YVR,P,EL
                    AC,YVR,YEG,P,EL
                """.trimIndent(),
            ).calculate(),
        ) {
            assertEquals(732, itinerary!!.segments[0].earningResult!!.sqc)
            assertEquals(3020, itinerary!!.segments[1].earningResult!!.sqc)
            assertEquals(4748, itinerary!!.segments[2].earningResult!!.sqc)
            assertEquals(4748, itinerary!!.segments[3].earningResult!!.sqc)
            assertEquals(3020, itinerary!!.segments[4].earningResult!!.sqc)
            assertEquals(732, itinerary!!.segments[5].earningResult!!.sqc)
        }
    }
}
