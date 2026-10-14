package com.example.timerboxing.data

import android.content.Context
import com.example.timerboxing.model.Workout
import com.example.timerboxing.model.workoutPresets

class WorkoutStore(context: Context) {
    private val preferences = context.getSharedPreferences("workout_settings", Context.MODE_PRIVATE)

    fun load(): List<Workout> = workoutPresets.map { preset ->
        val prefix = "${preset.id}_"
        preset.copy(
            rounds = preferences.getInt(prefix + "rounds", preset.rounds).coerceIn(1, 99),
            workSeconds = preferences.getInt(prefix + "work", preset.workSeconds).coerceIn(1, 3600),
            restSeconds = preferences.getInt(prefix + "rest", preset.restSeconds).coerceIn(0, 3600),
            prepareSeconds = preferences.getInt(prefix + "prepare", preset.prepareSeconds).coerceIn(0, 300),
            roundWarningSeconds = preferences.getInt(prefix + "warning", preset.roundWarningSeconds).coerceIn(0, 3600),
            periodicAlertSeconds = preferences.getInt(prefix + "periodic", preset.periodicAlertSeconds).coerceIn(0, 3600),
            restWarningEnabled = preferences.getBoolean(prefix + "restWarning", preset.restWarningEnabled)
        )
    }

    fun save(workout: Workout) {
        val prefix = "${workout.id}_"
        preferences.edit()
            .putInt(prefix + "rounds", workout.rounds)
            .putInt(prefix + "work", workout.workSeconds)
            .putInt(prefix + "rest", workout.restSeconds)
            .putInt(prefix + "prepare", workout.prepareSeconds)
            .putInt(prefix + "warning", workout.roundWarningSeconds)
            .putInt(prefix + "periodic", workout.periodicAlertSeconds)
            .putBoolean(prefix + "restWarning", workout.restWarningEnabled)
            .apply()
    }
}
