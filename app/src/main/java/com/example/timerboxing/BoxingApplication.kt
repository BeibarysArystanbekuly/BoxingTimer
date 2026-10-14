package com.example.timerboxing

import android.app.Application
import com.example.timerboxing.data.WorkoutRepository
import com.example.timerboxing.data.WorkoutStore

class BoxingApplication : Application() {
    val repository by lazy {
        val store = WorkoutStore(this)
        WorkoutRepository(store::load, store::save)
    }
}
