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
        val result = SqdCalculator(
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

        assertEquals(231, result.itinerary!!.segments[0].earningResult!!.sqd)
        assertEquals(599, result.itinerary!!.segments[1].earningResult!!.sqd)
    }
}
