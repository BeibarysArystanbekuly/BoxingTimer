package com.example.timerboxing.features.workoutdetail.timer

import com.example.timerboxing.model.Workout

data class TimerReading(
    val round: Int,
    val remainingSeconds: Int,
    val isRest: Boolean = false,
    val isFinished: Boolean = false,
    val isPreparing: Boolean = false
)

fun Workout.nextPhaseElapsedMillis(elapsedMillis: Long): Long {
    val reading = timerReading(elapsedMillis)
    if (reading.isFinished) return totalSeconds * 1000L
    if (reading.isPreparing) return prepareSeconds * 1000L
    val roundStartSeconds = prepareSeconds + (reading.round - 1L) * (workSeconds + restSeconds)
    val nextPhaseSeconds = roundStartSeconds + workSeconds + if (reading.isRest) restSeconds else 0
    return nextPhaseSeconds.coerceAtMost(totalSeconds.toLong()) * 1000L
}

fun Workout.timerReading(elapsedMillis: Long): TimerReading {
    val elapsedSeconds = elapsedMillis.coerceAtLeast(0) / 1000
    if (elapsedSeconds >= totalSeconds) {
        return TimerReading(rounds, 0, isFinished = true)
    }
    if (elapsedSeconds < prepareSeconds) {
        return TimerReading(1, prepareSeconds - elapsedSeconds.toInt(), isPreparing = true)
    }
    val trainingSeconds = elapsedSeconds - prepareSeconds
    val cycleSeconds = workSeconds + restSeconds
    val round = (trainingSeconds / cycleSeconds).toInt() + 1
    val secondsInCycle = (trainingSeconds % cycleSeconds).toInt()
    val isRest = secondsInCycle >= workSeconds
    val remaining = if (isRest) cycleSeconds - secondsInCycle else workSeconds - secondsInCycle
    return TimerReading(round, remaining, isRest)
}

fun Workout.shouldAlert(elapsedSeconds: Int): Boolean {
    val reading = timerReading(elapsedSeconds * 1000L)
    if (reading.isFinished || reading.isPreparing) return false
    if (reading.isRest) return restWarningEnabled && reading.remainingSeconds == 10
    val secondsIntoRound = workSeconds - reading.remainingSeconds
    return (roundWarningSeconds > 0 && reading.remainingSeconds == roundWarningSeconds) ||
        (periodicAlertSeconds > 0 && secondsIntoRound > 0 && secondsIntoRound % periodicAlertSeconds == 0)
}

fun Workout.shouldSignal(elapsedSeconds: Int): Boolean {
    if (elapsedSeconds < 0 || elapsedSeconds > totalSeconds) return false
    if (elapsedSeconds == totalSeconds) return true
    if (shouldAlert(elapsedSeconds)) return true
    if (elapsedSeconds < prepareSeconds) return false
    val secondsInCycle = (elapsedSeconds - prepareSeconds) % (workSeconds + restSeconds)
    return secondsInCycle == 0 || secondsInCycle == workSeconds
}
