package com.example.timerboxing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class WorkoutRepository(
    private val loadWorkouts: () -> List<Workout>,
    private val saveWorkout: (Workout) -> Unit,
    private val delayMillis: Long = 500L,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val mutex = Mutex()
    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()
    private var loaded = false

    fun observeWorkout(workoutId: Int): Flow<Workout?> =
        workouts.map { items -> items.find { it.id == workoutId } }.distinctUntilChanged()

    suspend fun load(simulateError: Boolean = false): List<Workout> {
        delay(delayMillis)
        check(!simulateError) { "Could not load workouts. Please retry." }
        return mutex.withLock {
            if (!loaded) {
                _workouts.value = withContext(ioDispatcher) { loadWorkouts().toList() }
                loaded = true
            }
            _workouts.value
        }
    }

    suspend fun save(workout: Workout) {
        delay(delayMillis)
        mutex.withLock {
            withContext(ioDispatcher) { saveWorkout(workout) }
            _workouts.value = _workouts.value.map { if (it.id == workout.id) workout else it }
        }
    }
}
