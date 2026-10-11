package com.example.timerboxing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DurationInputTest {
    @Test
    fun parsesMinutesAndSeconds() {
        assertEquals(0, parseDuration("00:00"))
        assertEquals(90, parseDuration("01:30"))
        assertEquals(3600, parseDuration("60:00"))
    }

    @Test
    fun rejectsMalformedTimesAndInvalidSeconds() {
        listOf("", "03:", "3:00", "0300", "00:60", "-1:00", "ab:cd").forEach {
            assertNull(parseDuration(it))
        }
    }

    @Test
    fun appliesDurationLimitsAndOptionalZero() {
        assertTrue(validDuration("00:00", 0..300))
        assertFalse(validDuration("00:00", 1..3600))
        assertTrue(validDuration("05:00", 0..300))
        assertFalse(validDuration("05:01", 0..300))
    }
}
