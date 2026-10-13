package com.example.timerboxing

import android.app.Application

class BoxingApplication : Application() {
    val repository by lazy {
        val store = WorkoutStore(this)
        WorkoutRepository(store::load, store::save)
    }
}
