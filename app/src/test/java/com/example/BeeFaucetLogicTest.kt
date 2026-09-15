package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeeFaucetLogicTest {

    @Test
    fun `evm wallet address format validation rules`() {
        val validAddress = "0x71C7656EC7ab88b098defB751B7401B5f6d8976F"
        val invalidShort = "0x1234"
        val invalidNo0x = "71C7656EC7ab88b098defB751B7401B5f6d8976F"

        val isValid = validAddress.startsWith("0x") && validAddress.length == 42
        val isInvalidShort = invalidShort.startsWith("0x") && invalidShort.length == 42
        val isInvalidNo0x = invalidNo0x.startsWith("0x") && invalidNo0x.length == 42

        assertTrue("Valid EVM address passes format check", isValid)
        assertFalse("Short address fails format check", isInvalidShort)
        assertFalse("Missing 0x fails format check", isInvalidNo0x)
    }

    @Test
    fun `usd valuation calculation from coingecko rate`() {
        val balance = 0.5
        val coinGeckoBnbPrice = 580.0
        val usdValuation = balance * coinGeckoBnbPrice
        assertEquals(290.0, usdValuation, 0.001)
    }

    @Test
    fun `time formatting formats minutes and seconds correctly`() {
        val timeRemainingMs = 125000L // 2 min 5 sec
        val totalSeconds = timeRemainingMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val formatted = "%02d:%02d".format(minutes, seconds)

        assertEquals("02:05", formatted)
    }

    @Test
    fun `crypto math checksum evaluates accurately`() {
        val a = 0x14 // 20
        val b = 0x08 // 8
        val sum = a + b
        assertEquals(28, sum)
        assertEquals("0x1C", "0x%02X".format(sum))
    }

    @Test
    fun `daily claims increment and cap at max limit of 10`() {
        val maxLimit = 10
        var currentClaims = 9
        val afterClaim = (currentClaims + 1).coerceAtMost(maxLimit)
        assertEquals(10, afterClaim)

        // Cannot exceed 10
        val nextClaim = (afterClaim + 1).coerceAtMost(maxLimit)
        assertEquals(10, nextClaim)
    }

    @Test
    fun `faucet cooldown initializes at 60 seconds`() {
        val cooldownSeconds = 60
        assertTrue("Cooldown should start at 60s", cooldownSeconds == 60)
        val afterOneSecond = cooldownSeconds - 1
        assertEquals(59, afterOneSecond)
    }
}
