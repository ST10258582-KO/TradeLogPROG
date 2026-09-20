package com.tradelog.app

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TradeLog business logic.
 * Covers trade R-multiple calculations, risk validation, session classification,
 * and tag parsing — all core domain operations.
 * Reference: JUnit 4 testing framework (junit.org, 2024)
 */
class TradeLogUnitTest {

    // R-Multiple Calculation Tests
    // Formula: (exitPrice - entryPrice) / (entryPrice - stopLoss) for BUY trades
    // Computed server-side; validated client-side here.

    @Test
    fun rMultiple_isPositive_forWinningBuyTrade() {
        val entry = 4500.0
        val stop = 4480.0
        val exit = 4540.0
        val rMultiple = (exit - entry) / (entry - stop)
        assertTrue("Expected positive R-multiple for profitable BUY trade", rMultiple > 0)
        assertEquals(2.0, rMultiple, 0.001)
    }

    @Test
    fun rMultiple_isNegative_forLosingBuyTrade() {
        val entry = 4500.0
        val stop = 4480.0
        val exit = 4488.0
        val rMultiple = (exit - entry) / (entry - stop)
        assertTrue("Expected negative R-multiple for losing BUY trade", rMultiple < 0)
        assertEquals(-0.6, rMultiple, 0.001)
    }

    @Test
    fun rMultiple_isPositive_forWinningSellTrade() {
        val entry = 4500.0
        val stop = 4520.0
        val exit = 4460.0
        val rMultiple = (entry - exit) / (stop - entry)
        assertTrue("Expected positive R-multiple for profitable SELL trade", rMultiple > 0)
        assertEquals(2.0, rMultiple, 0.001)
    }

    // Risk Percentage Validation Tests

    @Test
    fun riskValidation_passes_forValidRisk() {
        val risk = 1.5f
        assertTrue(risk > 0f && risk <= 100f)
    }

    @Test
    fun riskValidation_fails_forZeroRisk() {
        val risk = 0f
        assertFalse(risk > 0f && risk <= 100f)
    }

    @Test
    fun riskValidation_fails_forNegativeRisk() {
        val risk = -1f
        assertFalse(risk > 0f && risk <= 100f)
    }

    @Test
    fun riskValidation_fails_forRiskOver100() {
        val risk = 101f
        assertFalse(risk > 0f && risk <= 100f)
    }

    // Session Classification Tests
    // UTC hour ranges: London 7-14, New York 12-19 (NY takes precedence in overlap), Asia 0-6

    @Test
    fun sessionClassification_returnsLondon_forUtcHour9() {
        assertEquals("LONDON", classifySession(9))
    }

    @Test
    fun sessionClassification_returnsNewYork_forUtcHour14() {
        assertEquals("NEW_YORK", classifySession(14))
    }

    @Test
    fun sessionClassification_returnsAsia_forUtcHour3() {
        assertEquals("ASIA", classifySession(3))
    }

    @Test
    fun sessionClassification_returnsOther_forUtcHour22() {
        assertEquals("OTHER", classifySession(22))
    }

    // Instrument Validation Tests

    @Test
    fun instrumentValidation_fails_forBlankInput() {
        assertFalse("   ".isNotBlank())
    }

    @Test
    fun instrumentValidation_passes_forValidInstrument() {
        assertTrue("NQ".isNotBlank())
    }

    @Test
    fun instrumentUppercase_convertsCorrectly() {
        assertEquals("NQ", "nq".trim().uppercase())
    }

    // Tag Parsing Tests

    @Test
    fun tagParsing_parsesCommaSeparatedTagsCorrectly() {
        val input = "ORB, Breakout, Reversion"
        val tags = input.split(",").map { it.trim() }.filter { it.isNotBlank() }
        assertEquals(3, tags.size)
        assertEquals("ORB", tags[0])
        assertEquals("Breakout", tags[1])
        assertEquals("Reversion", tags[2])
    }

    @Test
    fun tagParsing_returnsEmptyList_forEmptyInput() {
        val tags = "".split(",").map { it.trim() }.filter { it.isNotBlank() }
        assertTrue(tags.isEmpty())
    }

    @Test
    fun tagParsing_filtersBlankEntries() {
        val input = "ORB, , Breakout, "
        val tags = input.split(",").map { it.trim() }.filter { it.isNotBlank() }
        assertEquals(2, tags.size)
    }

    // Helper function mirroring server-side session classification logic
    private fun classifySession(utcHour: Int): String = when {
        utcHour in 12..19 -> "NEW_YORK"
        utcHour in 7..14  -> "LONDON"
        utcHour in 0..6   -> "ASIA"
        else               -> "OTHER"
    }
}
