package com.example.timerboxing.domain.repository

import com.example.timerboxing.domain.model.Exercise

interface ExerciseRepository {
    suspend fun getItems(): List<Exercise>
}
