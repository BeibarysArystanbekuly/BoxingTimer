package com.example.timerboxing.features.workoutsettings

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerboxing.data.WorkoutRepository
import com.example.timerboxing.model.Workout
import com.example.timerboxing.model.formatDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun settingsDraft(workout: Workout) = WorkoutSettingsDraft(
    workout = workout,
    restWarning = workout.restWarningEnabled,
    fields = listOf(
        SettingField(WorkoutSetting.ROUNDS, "Number of rounds", workout.rounds.toString(), false),
        SettingField(WorkoutSetting.WORK, "Round duration", formatDuration(workout.workSeconds), true),
        SettingField(WorkoutSetting.REST, "Rest duration", formatDuration(workout.restSeconds), true),
        SettingField(WorkoutSetting.WARNING, "Round warning (time remaining)", formatDuration(workout.roundWarningSeconds), true),
        SettingField(WorkoutSetting.PERIODIC, "Periodic alert interval", formatDuration(workout.periodicAlertSeconds), true),
        SettingField(WorkoutSetting.PREPARE, "Preparation time", formatDuration(workout.prepareSeconds), true)
    )
)

private fun validateDraft(draft: WorkoutSettingsDraft): Map<WorkoutSetting, String> {
    val workLimit = (parseDuration(draft.fields.first { it.setting == WorkoutSetting.WORK }.value) ?: 1).coerceIn(1, 3600)
    return buildMap {
        draft.fields.forEach { field ->
            val range = when (field.setting) {
                WorkoutSetting.ROUNDS -> 1..99
                WorkoutSetting.WORK -> 1..3600
                WorkoutSetting.REST -> 0..3600
                WorkoutSetting.PREPARE -> 0..300
                WorkoutSetting.WARNING, WorkoutSetting.PERIODIC -> 0..workLimit
            }
            val valid = if (field.isDuration) validDuration(field.value, range)
                else field.value.toIntOrNull()?.let { it in range } == true
            if (!valid) {
                put(field.setting, if (field.isDuration)
                    "Use MM:SS, from ${formatDuration(range.first)} to ${formatDuration(range.last)}"
                    else "Enter ${range.first}-${range.last} rounds")
            }
        }
    }
}

class WorkoutSettingsViewModel(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val workoutId = savedStateHandle.get<Int>("workoutId")
    private val _uiState = MutableStateFlow(WorkoutSettingsUiState())
    val uiState: StateFlow<WorkoutSettingsUiState> = _uiState.asStateFlow()
    private val _savedEvents = MutableSharedFlow<Int?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val savedEvents: SharedFlow<Int?> = _savedEvents.asSharedFlow()
    private var loadJob: Job? = null
    private var saveCompleted = false
    private var pendingSavedId: Int? = null

    init {
        Log.d("WorkoutSettingsVM", "init ${System.identityHashCode(this)}")
        onRetry()
    }

    fun onRetry() {
        if (_uiState.value.isSaving || saveCompleted) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val draft = repository.load().find { it.id == workoutId }?.let(::settingsDraft)
                _uiState.update {
                    it.copy(draft = draft, validationErrors = draft?.let(::validateDraft).orEmpty(), isLoading = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Could not load settings.") }
            }
        }
    }

    fun onFieldChange(setting: WorkoutSetting, value: String) {
        val state = _uiState.value
        val draft = state.draft ?: return
        if (state.isSaving || state.isLoading || saveCompleted) return
        val field = draft.fields.find { it.setting == setting } ?: return
        if (value.length > (if (field.isDuration) 5 else 4) ||
            !value.all { it in '0'..'9' || (field.isDuration && it == ':') }) return
        val updated = draft.copy(fields = draft.fields.map { if (it.setting == setting) it.copy(value = value) else it })
        _uiState.update { it.copy(draft = updated, validationErrors = validateDraft(updated), error = null) }
    }

    fun onRestWarningChange(enabled: Boolean) {
        if (_uiState.value.isSaving || _uiState.value.isLoading || saveCompleted) return
        _uiState.update { it.copy(draft = it.draft?.copy(restWarning = enabled)) }
    }

    fun onSave() {
        val state = _uiState.value
        val draft = state.draft ?: return
        if (state.isSaving || state.isLoading || saveCompleted) return
        val errors = validateDraft(draft)
        _uiState.update { it.copy(validationErrors = errors) }
        if (errors.isNotEmpty()) return
        fun value(setting: WorkoutSetting) = draft.fields.first { it.setting == setting }.value
        val updated = draft.workout.copy(
            rounds = value(WorkoutSetting.ROUNDS).toInt(),
            workSeconds = requireNotNull(parseDuration(value(WorkoutSetting.WORK))),
            restSeconds = requireNotNull(parseDuration(value(WorkoutSetting.REST))),
            prepareSeconds = requireNotNull(parseDuration(value(WorkoutSetting.PREPARE))),
            roundWarningSeconds = requireNotNull(parseDuration(value(WorkoutSetting.WARNING))),
            periodicAlertSeconds = requireNotNull(parseDuration(value(WorkoutSetting.PERIODIC))),
            restWarningEnabled = draft.restWarning
        )
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.save(updated)
                saveCompleted = true
                _uiState.update { it.copy(draft = draft.copy(workout = updated), isSaving = false) }
                pendingSavedId = updated.id
                _savedEvents.tryEmit(updated.id)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSaving = false, error = exception.message ?: "Could not save settings.") }
            }
        }
    }

    fun consumeSavedEvent(id: Int): Boolean {
        if (pendingSavedId != id) return false
        pendingSavedId = null
        _savedEvents.tryEmit(null)
        return true
    }

    override fun onCleared() {
        Log.d("WorkoutSettingsVM", "onCleared ${System.identityHashCode(this)}")
    }
}
