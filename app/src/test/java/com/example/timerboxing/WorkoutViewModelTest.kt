package com.example.timerboxing

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val workout = Workout(1, "Boxing", "Boxing", 2, 3, 2)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun cleanup() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun <Model : ViewModel> retain(model: Model): Model {
        store.put(model.javaClass.name, model)
        return model
    }

    private fun repository(save: (Workout) -> Unit = {}) =
        WorkoutRepository({ listOf(workout, workout.copy(id = 2, name = "Amateur")) }, save, 500L, dispatcher)

    private fun detail(repository: WorkoutRepository = repository()) =
        retain(WorkoutDetailViewModel(repository, SavedStateHandle(mapOf("workoutId" to 1))) {
            dispatcher.scheduler.currentTime
        })

    @Test
    fun searchAndSortSurviveErrorAndRetry() = runTest(dispatcher) {
        val model = retain(WorkoutListViewModel(repository()))
        assertTrue(model.uiState.value.isLoading)
        advanceUntilIdle()
        model.onSortChange(WorkoutSortOrder.NAME)
        assertEquals(listOf("Amateur", "Boxing"), model.uiState.value.workouts.map { it.name })
        model.onSearchChange("box")
        assertEquals(listOf(workout), model.uiState.value.workouts)
        model.onSimulateError()
        advanceUntilIdle()
        assertNotNull(model.uiState.value.error)
        model.onRetry()
        advanceUntilIdle()
        assertNull(model.uiState.value.error)
        assertEquals("box", model.uiState.value.searchQuery)
        assertEquals(WorkoutSortOrder.NAME, model.uiState.value.sortOrder)
        assertEquals(listOf(workout), model.uiState.value.workouts)
        model.onSearchChange("missing")
        assertTrue(model.uiState.value.workouts.isEmpty())
        assertFalse(model.uiState.value.isLoading)
    }

    @Test
    fun draftDoesNotPersistUntilSaveAndUpdatesExistingDetailOnce() = runTest(dispatcher) {
        var persisted: Workout? = null
        var saves = 0
        val repository = repository { persisted = it; saves++ }
        val list = retain(WorkoutListViewModel(repository))
        val detail = detail(repository)
        val settings = retain(WorkoutSettingsViewModel(repository, SavedStateHandle(mapOf("workoutId" to 1))))
        advanceUntilIdle()
        settings.onFieldChange(WorkoutSetting.ROUNDS, "5")
        assertNull(persisted)
        assertEquals(2, detail.uiState.value.workout?.rounds)
        settings.onSave()
        settings.onSave()
        assertTrue(settings.uiState.value.isSaving)
        advanceUntilIdle()
        assertEquals(1, saves)
        assertEquals(5, persisted?.rounds)
        assertEquals(5, list.uiState.value.workouts.first().rounds)
        assertEquals(5, detail.uiState.value.workout?.rounds)
        assertSame(detail, store.get(WorkoutDetailViewModel::class.java.name))
        assertTrue(settings.consumeSavedEvent(1))
        assertFalse(settings.consumeSavedEvent(1))
    }

    @Test
    fun settingsRejectInvalidDraftAndKeepItAfterSaveFailure() = runTest(dispatcher) {
        var saves = 0
        val model = retain(WorkoutSettingsViewModel(repository { saves++; error("Storage unavailable") },
            SavedStateHandle(mapOf("workoutId" to 1))))
        advanceUntilIdle()
        model.onFieldChange(WorkoutSetting.WARNING, "00:04")
        assertTrue(model.uiState.value.validationErrors.containsKey(WorkoutSetting.WARNING))
        model.onSave()
        advanceUntilIdle()
        assertEquals(0, saves)
        model.onFieldChange(WorkoutSetting.WORK, "00:05")
        assertTrue(model.uiState.value.validationErrors.isEmpty())
        model.onSave()
        advanceUntilIdle()
        assertEquals("Storage unavailable", model.uiState.value.error)
        assertFalse(model.uiState.value.isSaving)
        assertFalse(model.consumeSavedEvent(1))
        assertEquals("00:05", model.uiState.value.draft?.fields?.first { it.setting == WorkoutSetting.WORK }?.value)
    }

    @Test
    fun runningAndResumedSessionKeepSnapshotUntilReset() = runTest(dispatcher) {
        val repository = repository()
        val model = detail(repository)
        val settings = retain(WorkoutSettingsViewModel(repository, SavedStateHandle(mapOf("workoutId" to 1))))
        advanceUntilIdle()
        model.onStart()
        advanceTimeBy(1000L)
        settings.onFieldChange(WorkoutSetting.WORK, "00:10")
        settings.onSave()
        advanceTimeBy(500L)
        runCurrent()
        assertEquals(10, repository.workouts.value.first().workSeconds)
        assertEquals(3, model.uiState.value.workout?.workSeconds)
        advanceTimeBy(1500L)
        runCurrent()
        assertEquals(WorkoutPhase.REST, model.uiState.value.phase)
        model.onPause()
        model.onStart()
        advanceTimeBy(2000L)
        runCurrent()
        assertEquals(WorkoutPhase.WORK, model.uiState.value.phase)
        assertEquals(2, model.uiState.value.currentRound)
        assertEquals(3, model.uiState.value.remainingSeconds)
        model.onStop()
        assertEquals(WorkoutPhase.READY, model.uiState.value.phase)
        assertEquals(10, model.uiState.value.remainingSeconds)
        model.onStart()
        assertEquals(10, model.uiState.value.workout?.workSeconds)
        model.onPause()
    }

    @Test
    fun returnFromSettingsKeepsPausedSnapshotAndNextSessionUsesLatest() = runTest(dispatcher) {
        val repository = repository()
        val model = detail(repository)
        advanceUntilIdle()
        model.onStart()
        advanceTimeBy(1250L)
        model.onPause()
        repository.save(workout.copy(workSeconds = 10))
        runCurrent()
        assertEquals(3, model.uiState.value.workout?.workSeconds)
        assertEquals(2, model.uiState.value.remainingSeconds)
        assertFalse(model.uiState.value.isRunning)
        model.onStart()
        advanceTimeBy(6850L)
        runCurrent()
        assertEquals(WorkoutPhase.FINISHED, model.uiState.value.phase)
        model.onStart()
        assertEquals(10, model.uiState.value.remainingSeconds)
        assertEquals(10, model.uiState.value.workout?.workSeconds)
        model.onPause()
    }

    @Test
    fun timerPausesSkipsResumesFinishesAndRestarts() = runTest(dispatcher) {
        val model = detail()
        advanceUntilIdle()
        model.onStart()
        advanceTimeBy(1250L)
        model.onPause()
        val paused = model.uiState.value
        advanceTimeBy(10000L)
        assertEquals(paused, model.uiState.value)
        model.onNext()
        assertEquals(WorkoutPhase.REST, model.uiState.value.phase)
        assertEquals(2, model.uiState.value.remainingSeconds)
        model.onStart()
        advanceTimeBy(5000L)
        runCurrent()
        assertEquals(WorkoutPhase.FINISHED, model.uiState.value.phase)
        assertFalse(model.uiState.value.isRunning)
        model.onStart()
        assertEquals(3, model.uiState.value.remainingSeconds)
        assertTrue(model.uiState.value.isRunning)
        model.onStop()
        assertEquals(WorkoutPhase.READY, model.uiState.value.phase)
    }

    @Test
    fun configurationStopAndReattachmentKeepOneTickerAndBackgroundPauses() = runTest(dispatcher) {
        val model = detail()
        advanceUntilIdle()
        var changingConfigurations = true
        val observer = WorkoutLifecycleObserver({ changingConfigurations }, model::onPause)
        val owner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = error("Lifecycle is not accessed by the observer")
        }
        var signalCount = 0
        var subscription = backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        model.onStart()
        runCurrent()
        advanceTimeBy(1000L)
        observer.onStateChanged(owner, Lifecycle.Event.ON_STOP)
        subscription.cancel()
        runCurrent()
        assertTrue(model.uiState.value.isRunning)
        advanceTimeBy(500L)
        subscription = backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        model.onStart()
        advanceTimeBy(1500L)
        runCurrent()
        assertEquals(WorkoutPhase.REST, model.uiState.value.phase)
        assertEquals(2, signalCount)
        changingConfigurations = false
        observer.onStateChanged(owner, Lifecycle.Event.ON_STOP)
        val paused = model.uiState.value
        assertFalse(paused.isRunning)
        advanceTimeBy(10000L)
        observer.onStateChanged(owner, Lifecycle.Event.ON_START)
        observer.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        assertEquals(paused, model.uiState.value)
        subscription.cancel()
    }

    @Test
    fun absentSubscriberReceivesFreshSignalOnceAndReconnectDoesNotReplayIt() = runTest(dispatcher) {
        val model = detail()
        advanceUntilIdle()
        model.onStart()
        advanceTimeBy(200L)
        var signalCount = 0
        var subscription = backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        assertEquals(1, signalCount)
        subscription.cancel()
        runCurrent()
        advanceTimeBy(3000L)
        runCurrent()
        subscription = backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        assertEquals(2, signalCount)
        subscription.cancel()
        runCurrent()
        backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        assertEquals(2, signalCount)
        model.onPause()
    }

    @Test
    fun staleAndBackgroundSignalsAreNotPlayed() = runTest(dispatcher) {
        val model = detail()
        advanceUntilIdle()
        model.onStart()
        advanceTimeBy(1600L)
        var signalCount = 0
        val subscription = backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        assertEquals(0, signalCount)
        subscription.cancel()
        runCurrent()
        advanceTimeBy(1500L)
        runCurrent()
        model.onPause()
        backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        assertEquals(0, signalCount)
        model.onStart()
        runCurrent()
        assertEquals(0, signalCount)
        model.onPause()
    }

    @Test
    fun delayedTickDoesNotPlayOldBoundarySound() = runTest(dispatcher) {
        var now = 0L
        val model = retain(WorkoutDetailViewModel(repository(), SavedStateHandle(mapOf("workoutId" to 1))) { now })
        advanceUntilIdle()
        model.onStart()
        runCurrent()
        now = 20000L
        advanceTimeBy(100L)
        runCurrent()
        var signalCount = 0
        backgroundScope.launch {
            model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
        }
        runCurrent()
        assertEquals(WorkoutPhase.FINISHED, model.uiState.value.phase)
        assertEquals(0, signalCount)
    }

    @Test
    fun signalCanOnlyBeConsumedOnceEvenWithTwoCollectors() = runTest(dispatcher) {
        val model = detail()
        advanceUntilIdle()
        var signalCount = 0
        repeat(2) {
            backgroundScope.launch {
                model.signals.collect { if (it != null && model.consumeSignal(it)) signalCount++ }
            }
        }
        runCurrent()
        model.onStart()
        runCurrent()
        assertEquals(1, signalCount)
        advanceTimeBy(3000L)
        runCurrent()
        assertEquals(2, signalCount)
        model.onPause()
    }

    @Test
    fun retryDoesNotResetActiveSession() = runTest(dispatcher) {
        val model = detail()
        advanceUntilIdle()
        model.onStart()
        advanceTimeBy(1000L)
        model.onRetry()
        advanceTimeBy(500L)
        runCurrent()
        assertEquals(2, model.uiState.value.remainingSeconds)
        assertTrue(model.uiState.value.isRunning)
        assertFalse(model.uiState.value.isLoading)
        model.onPause()
    }

    @Test
    fun observesOnlySelectedWorkoutChanges() = runTest(dispatcher) {
        val repository = repository()
        val observed = mutableListOf<Workout?>()
        backgroundScope.launch { repository.observeWorkout(1).collect { observed.add(it) } }
        runCurrent()
        repository.load()
        runCurrent()
        repository.save(workout.copy(id = 2, name = "Changed other workout"))
        runCurrent()
        assertEquals(listOf(null, workout), observed)
        repository.save(workout.copy(workSeconds = 10))
        runCurrent()
        assertEquals(10, observed.last()?.workSeconds)
    }

    @Test
    fun missingWorkoutCannotStartTimer() = runTest(dispatcher) {
        val model = retain(WorkoutDetailViewModel(repository(), SavedStateHandle(mapOf("workoutId" to 99))))
        advanceUntilIdle()
        assertFalse(model.uiState.value.isLoading)
        assertNull(model.uiState.value.workout)
        model.onStart()
        assertFalse(model.uiState.value.isRunning)
    }

    @Test
    fun detailLoadFailureCanBeRetried() = runTest(dispatcher) {
        var fail = true
        val repository = WorkoutRepository(
            { if (fail) error("Loading failed") else listOf(workout) }, {}, 500L, dispatcher
        )
        val model = detail(repository)
        advanceUntilIdle()
        assertEquals("Loading failed", model.uiState.value.error)
        model.onStart()
        assertFalse(model.uiState.value.isRunning)
        fail = false
        model.onRetry()
        advanceUntilIdle()
        assertNull(model.uiState.value.error)
        assertEquals(workout, model.uiState.value.workout)
    }
}
