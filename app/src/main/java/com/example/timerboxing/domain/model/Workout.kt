package com.example.timerboxing.domain.model

data class Workout(
    val id: Int,
    val name: String,
    val category: String,
    val rounds: Int,
    val workSeconds: Int,
    val restSeconds: Int,
    val prepareSeconds: Int = 0,
    val roundWarningSeconds: Int = 0,
    val periodicAlertSeconds: Int = 0,
    val restWarningEnabled: Boolean = false
) {
    val totalSeconds: Int
        get() = prepareSeconds + rounds * workSeconds + (rounds - 1).coerceAtLeast(0) * restSeconds
}
