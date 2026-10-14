package com.example.timerboxing.features.workoutsettings

import com.example.timerboxing.model.Workout

enum class WorkoutSetting { ROUNDS, WORK, REST, PREPARE, WARNING, PERIODIC }

data class SettingField(
    val setting: WorkoutSetting,
    val label: String,
    val value: String,
    val isDuration: Boolean
)

data class WorkoutSettingsDraft(
    val workout: Workout,
    val fields: List<SettingField>,
    val restWarning: Boolean
)

data class WorkoutSettingsUiState(
    val draft: WorkoutSettingsDraft? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val validationErrors: Map<WorkoutSetting, String> = emptyMap(),
    val error: String? = null
)
