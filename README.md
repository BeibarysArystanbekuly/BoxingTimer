TimerBoxing

## MVVM architecture

[design/architecture.mmd](design/architecture.mmd) is the editable Mermaid diagram.
The dependency chain is Screen -> Route -> ViewModel -> Repository -> Store.
The original Timer / Profiles / Settings names correspond to WorkoutDetail /
WorkoutList / WorkoutSettings in this project.

Screens receive only immutable state and callbacks. Routes in
`app/src/main/java/com/example/timerboxing/WorkoutRoutes.kt` create navigation-entry
ViewModels and collect their public `StateFlow` using `collectAsStateWithLifecycle()`.
Every ViewModel keeps its `MutableStateFlow` private and updates UiState with `copy()`.
All UiState properties use `val`; Android effects and navigation stay outside Screens.

| UiState | Fields |
| --- | --- |
| WorkoutListUiState | workouts, searchQuery, sortOrder, isLoading, error |
| WorkoutDetailUiState | workout, phase, currentRound, remainingSeconds, isRunning, isLoading, error |
| WorkoutSettingsUiState | draft, isLoading, isSaving, validationErrors, error |

`WorkoutSortOrder` supports DEFAULT and NAME. `WorkoutPhase` distinguishes READY,
PREPARATION, WORK, REST, and FINISHED. Pausing retains the phase with `isRunning = false`.
Each screen has loading/content/empty/error previews; absent workout IDs produce
an empty detail/settings screen, and an unmatched search produces an empty list.

### Repository, settings, and session parameters

`BoxingApplication.kt` owns a shared `WorkoutRepository`. The repository owns the
workout data, uses `WorkoutStore.kt` for SharedPreferences, simulates 500 ms request
latency, and can simulate a list loading error. Retry performs a normal request.

`WorkoutSettingsViewModel.onFieldChange()` changes only its immutable
`WorkoutSettingsDraft`. Invalid and partial input remains in the draft and is
reported through `validationErrors`. `onSave()` validates again, creates a Workout,
and calls `WorkoutRepository.save()` only if valid. Duplicate saves are blocked.
A save failure keeps the draft and exposes an error; successful saving emits a
separate acknowledged `savedEvents` event for navigation.

`WorkoutRepository.observeWorkout(workoutId)` maps the repository StateFlow to the
selected workout and suppresses unchanged emissions. `WorkoutDetailViewModel`
observes it throughout its lifetime. `BoxingApp.kt` pops only the settings entry
after saving, retaining the existing detail ViewModel instead of recreating it.

Before a session starts, new parameters are displayed immediately. `onStart()`
captures an immutable `sessionWorkout` snapshot. The entire current session,
including paused/resumed rounds, uses this snapshot for durations, round counts,
and alerts. New repository values are kept as `latestWorkout` and apply after
`onStop()` resets the timer or after starting a new session from FINISHED.
Resume means continuing the current session, not applying new settings.
During a session, UiState.workout displays the snapshot so its totals match the clock.

### Lifecycle and one-shot sound events

The monotonic clock origin, elapsed milliseconds, snapshot, and single ticker Job
belong to `WorkoutDetailViewModel`. `onStart()` is idempotent while running.
Reconnecting a Route only collects existing state; it never starts a ticker.

`WorkoutDetailRoute` registers `WorkoutLifecycleObserver` on the host Activity.
ON_STOP pauses the timer unless `Activity.isChangingConfigurations` is true.
Removing the observer during Route disposal does not pause or restart the timer.
Back and Settings callbacks explicitly pause before navigation. Returning from the
background or settings stays paused until Resume. Activity recreation retains the
navigation entry's ViewModel; process-death restoration of a running session or
unsaved draft is not promised. Persisted workout settings survive process death.

Sound is separate from UiState: `SharedFlow<WorkoutSoundEvent?>` retains at most
one pending command with an ID and monotonic timestamp. With no subscriber, the
latest command remains available for up to 1.5 seconds, allowing a short rotation
gap. A newer command supersedes an older one; missed boundaries are deliberately
coalesced into the latest beep rather than played as a burst. Boundary timestamps
reflect when they happened, so a delayed tick cannot make an old beep fresh.

The STARTED Route calls `consumeSignal(event)` and then `WorkoutSignal.play()`
synchronously on the main thread. Consumption clears the pending event before
playback, preventing replay after reconnection or duplicate handling by collectors.
Expired events are discarded. Pause, leaving the screen, and reset discard pending
sound, preventing delayed background beeps. This is one playback command per
accepted event, not a guarantee that muted or unavailable audio hardware plays it.
`WorkoutSignal.kt` owns ToneGenerator; the Route owns its lifecycle and keep-screen-on.

### Trace one click: search

1. The search field in `WorkoutListScreen.kt` invokes its `onSearchChange` callback.
2. `WorkoutListRoute` in `WorkoutRoutes.kt` wires that callback to
   `WorkoutListViewModel.onSearchChange()` in `WorkoutListViewModel.kt`.
3. `onSearchChange()` updates `searchQuery` through `copy()`, then
   `updateResults()` filters the repository list and applies `sortOrder`.
4. The ViewModel publishes the updated `WorkoutListUiState` through `uiState: StateFlow`.
5. `WorkoutListRoute` collects it with `collectAsStateWithLifecycle()` and supplies
   it to `WorkoutListScreen`.
6. The screen shows matching workout cards or "No workouts match your search."

### Verification

Run `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
`WorkoutViewModelTest.kt` covers draft isolation, invalid saves, failure recovery,
updates to the existing detail ViewModel, active/paused session snapshots, reset
and next-session adoption, configuration-stop handling, background pause, subscriber
reconnection, signal expiry and duplicate consumption. JVM lifecycle tests simulate
callbacks; they are not device rotation or audible playback tests.

Codex ran the debug APK build, Android lint, and all 31 JVM tests successfully
(15 ViewModel/repository/lifecycle/event tests plus 16 existing tests).
The tests ran through JUnitCore with the freshly built Gradle runtime classpath
because the Gradle test worker had failed to launch on this computer.

### My phone test - 13 October 2026

The emulator was stuck, so I connected my Samsung phone (SM-S936B).
Codex installed the latest debug build and checked that the app started.
I tested the app on my phone and confirmed these results in the chat:

- The timer keeps running when I rotate the phone.
- When I go Home and return, the timer is paused.
- Saving settings works. The paused session keeps its old settings, and the new
  settings apply after reset.
- The sound works without duplicate signals.

My replies were "yes" for rotation and background pause, then "yes it works" for
the settings/reset and sound checks. These are my manual test confirmations.
Codex did not independently observe those interactions or hear the sound.

### Why rotation keeps the timer

Android recreates the Activity when I rotate the phone.
The navigation entry keeps the same ViewModel, so the timer state stays there.
The new Route collects that state instead of starting another countdown.
The lifecycle observer skips the background pause when the Activity is changing
configuration.

Filter Logcat by `MainActivity`, `WorkoutDetailViewModel`, `WorkoutListViewModel`,
and `WorkoutSettingsVM`. These log lifecycle methods and instance identities.
Actual rotation Logcat output still needs to be added for SIS4. The phone test
confirms the visible behavior, but it does not replace logs showing the Activity
and ViewModel instance identities.

AI assistance is documented in [AI_USAGE.md](AI_USAGE.md).

TimerBoxing is a customizable boxing and interval training timer for Android. Users can create and save different training timers with their own round duration, rest duration, and number of rounds.

Functionality
Create a custom boxing timer.
Set the duration of each round.
Set the duration of each rest period.
Set the total number of rounds.
Save timer presets locally.
View saved timer presets.
Edit existing timer presets.
Delete timer presets.
Automatically switch between round and rest states.
Display the remaining time during a workout.

BUILD AND RUN
Clone the repository or download the project.
Open the project in Android Studio.
Wait for Gradle synchronization to finish.
Select an Android emulator or connect an Android device.
Click the Run button in Android Studio.
The application will build and launch on the selected device.
            gradle-wrapper.jar
            gradle-wrapper.properties
            
