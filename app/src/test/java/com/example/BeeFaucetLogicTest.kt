package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BeeFaucetLogicTest {

    @Test
    fun `automated withdrawal triggers when balance meets or exceeds threshold`() {
        val currentBalance = 0.0450
        val claimReward = 0.0080
        val threshold = 0.0500
        val newBalance = currentBalance + claimReward

        val shouldAutoWithdraw = newBalance >= threshold
        assertTrue("Auto withdrawal should be triggered", shouldAutoWithdraw)

        val remainingBalance = newBalance - threshold
        assertEquals(0.0030, remainingBalance, 0.0001)
    }

    @Test
    fun `automated withdrawal does not trigger when balance below threshold`() {
        val currentBalance = 0.0200
        val claimReward = 0.0100
        val threshold = 0.0500
        val newBalance = currentBalance + claimReward

        val shouldAutoWithdraw = newBalance >= threshold
        assertFalse("Auto withdrawal should not trigger prematurely", shouldAutoWithdraw)
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
}
