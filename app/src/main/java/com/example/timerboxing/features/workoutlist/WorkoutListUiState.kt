package com.example.timerboxing.features.workoutlist

import com.example.timerboxing.model.Workout

enum class WorkoutSortOrder { DEFAULT, NAME }

data class WorkoutListUiState(
    val workouts: List<Workout> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: WorkoutSortOrder = WorkoutSortOrder.DEFAULT,
    val isLoading: Boolean = true,
    val error: String? = null
)
