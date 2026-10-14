package com.example.timerboxing.features.workoutdetail

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timerboxing.data.WorkoutRepository
import com.example.timerboxing.features.workoutdetail.timer.nextPhaseElapsedMillis
import com.example.timerboxing.features.workoutdetail.timer.shouldSignal
import com.example.timerboxing.features.workoutdetail.timer.timerReading
import com.example.timerboxing.model.Workout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutSoundEvent(val id: Long, val createdAtMillis: Long)

class WorkoutDetailViewModel(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle,
    private val clock: () -> Long = SystemClock::elapsedRealtime
) : ViewModel() {
    private val workoutId = savedStateHandle.get<Int>("workoutId")
    private val _uiState = MutableStateFlow(WorkoutDetailUiState())
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()
    private val _signals = MutableSharedFlow<WorkoutSoundEvent?>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val signals: SharedFlow<WorkoutSoundEvent?> = _signals.asSharedFlow()
    private var pendingSignal: WorkoutSoundEvent? = null
    private var nextSignalId = 0L
    private var latestWorkout: Workout? = null
    private var sessionWorkout: Workout? = null
    private var elapsedMillis = 0L
    private var ticker: Job? = null
    private var loadJob: Job? = null
    private var clockOriginMillis = 0L

    init {
        Log.d("WorkoutDetailViewModel", "init ${System.identityHashCode(this)}")
        if (workoutId != null) {
            viewModelScope.launch {
                repository.observeWorkout(workoutId).collect { workout ->
                    latestWorkout = workout
                    if (sessionWorkout == null) publishReady()
                }
            }
        }
        onRetry()
    }

    fun onRetry() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                latestWorkout = repository.load().find { it.id == workoutId }
                if (sessionWorkout == null) publishReady()
                _uiState.update { it.copy(isLoading = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Could not load workout.") }
            }
        }
    }

    fun onPlayPause() {
        if (_uiState.value.isRunning) {
            onPause()
        } else {
            onStart()
        }
    }

    fun onStart() {
        val state = _uiState.value
        if (state.isRunning || state.isLoading || state.error != null) return
        val newSession = sessionWorkout == null || state.phase == WorkoutPhase.FINISHED
        if (newSession) {
            sessionWorkout = latestWorkout ?: return
            elapsedMillis = 0L
            clearSignal()
        }
        val workout = sessionWorkout ?: return
        clockOriginMillis = clock() - elapsedMillis
        publishReading(workout, running = true)
        if (newSession && workout.shouldSignal(0)) emitSignal()
        ticker?.cancel()
        ticker = viewModelScope.launch {
            while (_uiState.value.isRunning) {
                updateElapsed()
                if (_uiState.value.isRunning) delay(100L)
            }
        }
    }

    fun onPause() {
        updateElapsed()
        ticker?.cancel()
        ticker = null
        _uiState.update { it.copy(isRunning = false) }
        clearSignal()
    }

    fun onStop() {
        ticker?.cancel()
        ticker = null
        sessionWorkout = null
        elapsedMillis = 0L
        clearSignal()
        publishReady()
    }

    fun onNext() {
        val state = _uiState.value
        val workout = sessionWorkout ?: return
        if (state.isRunning || state.phase == WorkoutPhase.FINISHED) return
        elapsedMillis = workout.nextPhaseElapsedMillis(elapsedMillis)
        clearSignal()
        publishReading(workout, running = false)
    }

    fun consumeSignal(event: WorkoutSoundEvent): Boolean {
        if (pendingSignal?.id != event.id) return false
        val isFresh = clock() - event.createdAtMillis in 0L..1500L
        clearSignal()
        return isFresh
    }

    private fun emitSignal(createdAtMillis: Long = clock()) {
        val event = WorkoutSoundEvent(++nextSignalId, createdAtMillis)
        pendingSignal = event
        _signals.tryEmit(event)
    }

    private fun clearSignal() {
        pendingSignal = null
        _signals.tryEmit(null)
    }

    private fun publishReady() {
        val workout = latestWorkout
        _uiState.update {
            it.copy(workout = workout, phase = WorkoutPhase.READY, currentRound = 1,
                remainingSeconds = workout?.timerReading(0L)?.remainingSeconds ?: 0, isRunning = false)
        }
    }

    private fun publishReading(workout: Workout, running: Boolean) {
        val reading = workout.timerReading(elapsedMillis)
        val phase = when {
            reading.isFinished -> WorkoutPhase.FINISHED
            reading.isPreparing -> WorkoutPhase.PREPARATION
            reading.isRest -> WorkoutPhase.REST
            else -> WorkoutPhase.WORK
        }
        _uiState.update {
            it.copy(workout = workout, phase = phase, currentRound = reading.round,
                remainingSeconds = reading.remainingSeconds, isRunning = running && !reading.isFinished)
        }
    }

    private fun updateElapsed() {
        val workout = sessionWorkout ?: return
        if (!_uiState.value.isRunning) return
        val previousSecond = (elapsedMillis / 1000L).toInt()
        elapsedMillis = (clock() - clockOriginMillis).coerceIn(0L, workout.totalSeconds * 1000L)
        val currentSecond = (elapsedMillis / 1000L).toInt()
        if (currentSecond > previousSecond) {
            (currentSecond downTo previousSecond + 1).firstOrNull(workout::shouldSignal)?.let { signalSecond ->
                emitSignal(clockOriginMillis + signalSecond * 1000L)
            }
        }
        publishReading(workout, running = true)
    }

    override fun onCleared() {
        Log.d("WorkoutDetailViewModel", "onCleared ${System.identityHashCode(this)}")
    }
}
