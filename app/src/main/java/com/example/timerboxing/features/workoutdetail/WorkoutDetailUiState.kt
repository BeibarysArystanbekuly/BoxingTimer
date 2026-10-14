package com.example.timerboxing.features.workoutdetail

import com.example.timerboxing.model.Workout

enum class WorkoutPhase { READY, PREPARATION, WORK, REST, FINISHED }

data class WorkoutDetailUiState(
    val workout: Workout? = null,
    val phase: WorkoutPhase = WorkoutPhase.READY,
    val currentRound: Int = 1,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
