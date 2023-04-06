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
                hasBonusPointsPrivilege = true,
                segments = defaultSegments
            ).calculate()
        )
    }

    /**
     * This is a test of SQD rounding to try to match how AC credits.
     */
    @Test
    fun `SQD rounding should match AC`() {
        with(
            SqdCalculator(
                baseFare = 829.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                hasBonusPointsPrivilege = true,
                segments = """
                AC,SFO,YVR,Y,LT
                AC,YVR,YYZ,Y,LT
            """.trimIndent(),
            ).calculate()
        ) {
            assertEquals(231, itinerary!!.segments[0].earningResult!!.sqd)
            assertEquals(599, itinerary!!.segments[1].earningResult!!.sqd)
        }

        with(
            SqdCalculator(
                baseFare = 1217.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                hasBonusPointsPrivilege = true,
                segments = """
                AC,YYC,EWR,Y,LT
                AC,EWR,YYC,Y,LT
            """.trimIndent(),
            ).calculate()
        ) {
            assertEquals(609, itinerary!!.segments[0].earningResult!!.sqd)
            assertEquals(609, itinerary!!.segments[1].earningResult!!.sqd)
        }

        with(
            SqdCalculator(
                baseFare = 3610.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                hasBonusPointsPrivilege = true,
                segments = """
                AC,YYC,YUL,Y,LT
                AC,YUL,GRU,Y,LT
                AC,GRU,EZE,Y,LT
                AC,EZE,GRU,Y,LT
                AC,GRU,YUL,Y,LT
                AC,YUL,YYC,Y,LT
            """.trimIndent(),
            ).calculate()
        ) {
            assertEquals(422, itinerary!!.segments[0].earningResult!!.sqd)
            assertEquals(1142, itinerary!!.segments[1].earningResult!!.sqd)
            assertEquals(242, itinerary!!.segments[2].earningResult!!.sqd)
            assertEquals(242, itinerary!!.segments[3].earningResult!!.sqd)
            assertEquals(1142, itinerary!!.segments[4].earningResult!!.sqd)
            assertEquals(422, itinerary!!.segments[5].earningResult!!.sqd)
        }

        with(
            SqdCalculator(
                baseFare = 2792.0,
                surcharges = 0.0,
                ticket = "014",
                aeroplanStatus = "100",
                hasBonusPointsPrivilege = true,
                segments = """
                AC,YYC,YYZ,Y,LT
                AC,YYZ,YUL,Y,LT
                AC,YUL,BOG,Y,LT
                AC,BOG,YUL,Y,LT
                AC,YUL,YYC,Y,LT
            """.trimIndent(),
            ).calculate()
        ) {
            assertEquals(492, itinerary!!.segments[0].earningResult!!.sqd)
            assertEquals(93, itinerary!!.segments[1].earningResult!!.sqd)
            assertEquals(829, itinerary!!.segments[2].earningResult!!.sqd)
        }
    }
}
