package com.example.timerboxing.domain.repository

import com.example.timerboxing.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WorkoutRepository {
    val workouts: StateFlow<List<Workout>>

    fun observeWorkout(workoutId: Int): Flow<Workout?>

    suspend fun getItems(): List<Workout>

    suspend fun save(workout: Workout)
}
