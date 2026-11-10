package com.example.timerboxing.presentation.workoutlist

import com.example.timerboxing.presentation.UiState
import com.example.timerboxing.domain.model.Workout

enum class WorkoutSortOrder { DEFAULT, NAME }

data class WorkoutListUiState(
    val content: UiState<Workout> = UiState.Loading,
    val searchQuery: String = "",
    val sortOrder: WorkoutSortOrder = WorkoutSortOrder.DEFAULT
)
