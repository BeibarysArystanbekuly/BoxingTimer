package com.example.timerboxing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutTimerTest {
    private val workout = Workout(1, "Test", "Boxing", 2, 3, 2)

    @Test
    fun skipMovesToExactRestAndWorkBoundaries() {
        assertEquals(3000L, workout.nextPhaseElapsedMillis(1250))
        assertEquals(5000L, workout.nextPhaseElapsedMillis(3500))
        assertEquals(TimerReading(2, 3), workout.timerReading(workout.nextPhaseElapsedMillis(3500)))
    }

    @Test
    fun skipFinalRoundCompletesWorkout() {
        assertEquals(8000L, workout.nextPhaseElapsedMillis(5500))
        assertEquals(8000L, workout.nextPhaseElapsedMillis(8000))
    }

    @Test
    fun skipWithNoRestMovesDirectlyToNextRound() {
        val noRest = workout.copy(restSeconds = 0)
        val skippedTime = noRest.nextPhaseElapsedMillis(1250)
        assertEquals(3000L, skippedTime)
        assertEquals(TimerReading(2, 3), noRest.timerReading(skippedTime))
    }

    @Test
    fun transitionsFromWorkToRestToNextRound() {
        assertEquals(TimerReading(1, 3), workout.timerReading(0))
        assertEquals(TimerReading(1, 1), workout.timerReading(2999))
        assertEquals(TimerReading(1, 2, isRest = true), workout.timerReading(3000))
        assertEquals(TimerReading(2, 3), workout.timerReading(5000))
    }

    @Test
    fun finishesWithoutRestAfterLastRound() {
        assertEquals(8, workout.totalSeconds)
        assertFalse(workout.timerReading(7999).isFinished)
        assertEquals(TimerReading(2, 0, isFinished = true), workout.timerReading(8000))
        assertTrue(workout.timerReading(100000).isFinished)
    }

    @Test
    fun zeroRestStartsNextRoundImmediately() {
        val noRest = workout.copy(restSeconds = 0)
        assertEquals(TimerReading(2, 3), noRest.timerReading(3000))
        assertTrue(noRest.timerReading(6000).isFinished)
    }

    @Test
    fun singleRoundHasNoRest() {
        val singleRound = workout.copy(rounds = 1)
        assertEquals(3, singleRound.totalSeconds)
        assertTrue(singleRound.timerReading(3000).isFinished)
    }
}
