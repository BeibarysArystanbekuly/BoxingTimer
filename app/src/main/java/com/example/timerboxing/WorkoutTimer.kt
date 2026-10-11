package com.example.timerboxing

data class TimerReading(
    val round: Int,
    val remainingSeconds: Int,
    val isRest: Boolean = false,
    val isFinished: Boolean = false
)

fun Workout.nextPhaseElapsedMillis(elapsedMillis: Long): Long {
    val reading = timerReading(elapsedMillis)
    if (reading.isFinished) return totalSeconds * 1000L
    val roundStartSeconds = (reading.round - 1L) * (workSeconds + restSeconds)
    val nextPhaseSeconds = roundStartSeconds + workSeconds + if (reading.isRest) restSeconds else 0
    return nextPhaseSeconds.coerceAtMost(totalSeconds.toLong()) * 1000L
}

fun Workout.timerReading(elapsedMillis: Long): TimerReading {
    val elapsedSeconds = elapsedMillis.coerceAtLeast(0) / 1000
    if (elapsedSeconds >= totalSeconds) {
        return TimerReading(rounds, 0, isFinished = true)
    }
    val cycleSeconds = workSeconds + restSeconds
    val round = (elapsedSeconds / cycleSeconds).toInt() + 1
    val secondsInCycle = (elapsedSeconds % cycleSeconds).toInt()
    val isRest = secondsInCycle >= workSeconds
    val remaining = if (isRest) cycleSeconds - secondsInCycle else workSeconds - secondsInCycle
    return TimerReading(round, remaining, isRest)
}
