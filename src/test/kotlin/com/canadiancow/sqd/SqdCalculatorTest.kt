package com.canadiancow.sqd

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
                altitudeStatus = "100",
                hasBonusMilesPrivilege = true,
                segments = defaultSegments
            ).calculate()
        )
    }
}
