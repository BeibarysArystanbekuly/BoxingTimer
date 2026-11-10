package com.example.timerboxing

import android.app.Application
import com.example.timerboxing.data.remote.ExerciseApiClient
import com.example.timerboxing.data.repository.ExerciseRepositoryImpl
import com.example.timerboxing.domain.repository.ExerciseRepository
import com.example.timerboxing.data.remote.WorkoutStore
import com.example.timerboxing.data.repository.WorkoutRepositoryImpl
import com.example.timerboxing.domain.repository.WorkoutRepository

class BoxingApplication : Application() {
    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(ExerciseApiClient.api)
    }

    val repository: WorkoutRepository by lazy {
        val store = WorkoutStore(this)
        WorkoutRepositoryImpl(store::load, store::save)
    }
}
