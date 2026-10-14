# AI usage

## Tool I used

I used OpenAI Codex in this project. The assistance included writing and changing
code, tests, and documentation. It was not only explanations or autocomplete.

## What I asked AI to do

I asked Codex to read the SIS4 PDF and explain the requirements.
I provided my architecture image and asked it to adapt the app to MVVM.
Later I gave more specific requirements for observing saved workouts, keeping a
snapshot of timer settings, handling rotation and background pause, separating
sound events, and defining the UiState classes.

## What AI implemented

- The shared WorkoutRepository and the list, detail, and settings ViewModels.
- UiState classes, private MutableStateFlow, public StateFlow, and Route composables.
- Moving timer logic and settings validation out of the screen composables.
- Search, sorting, loading, empty and error states, retry, and previews.
- Workout observation and a session snapshot that stays unchanged while running
  or paused.
- Lifecycle handling and separate sound events with one-time consumption and a
  1.5-second freshness limit.
- Automated tests, lifecycle logging, the adapted Mermaid diagram, and README text.

AI also helped troubleshoot the emulator, build the app, install it on my phone,
and inspect startup logs. I asked it to write this file and update the README
using my actual test confirmations.

## What I contributed

I provided the project, assignment PDF, and architecture image.
I specified the additional MVVM behavior I wanted and asked follow-up questions
about the architecture and the changes.
I connected my phone and manually checked the app.
I am not claiming that I wrote the AI-generated refactor or tests independently.

## Wrong-layer example and corrections

Before the refactor, WorkoutDetailScreen contained timer state, the countdown
loop, and start/pause/reset/skip logic. That put application logic inside a screen
composable. With AI assistance, this logic moved into WorkoutDetailViewModel.
WorkoutDetailScreen now receives WorkoutDetailUiState and callbacks. The Route
handles Android lifecycle and audio effects through the platform component.

This is a real wrong-layer example from the starting code. The conversation does
not establish who originally wrote that timer logic, so I cannot honestly claim
that AI introduced it.

There was also a problem in the first AI-written MVVM version: saving settings
removed and recreated the detail destination to refresh the workout. That made
the update depend on navigation. After my follow-up requirements, Codex added
WorkoutRepository.observeWorkout(workoutId), retained the detail ViewModel, and
kept a separate snapshot for the current timer session.

The first version also paused the timer from Route disposal and used sound
events that were lost without a subscriber. Codex revised those behaviors after
my requirements: Route reconnection does not pause the timer, and fresh pending
sound events can be consumed once after a short subscription gap.

## What was checked

Codex successfully ran the debug APK build, Android lint, and 31 JVM tests.
It ran the tests directly through JUnitCore because the Gradle test worker failed
to launch on this computer. Those tests check model and event behavior; they do
not prove what happens on a real phone.

On 13 October 2026, I tested the app on my Samsung SM-S936B.
I confirmed that the timer continues through rotation, pauses when I go Home and
return, keeps the current session settings until reset, applies saved settings
after reset, and plays sound without duplicates.
My chat replies were "yes" and "yes it works".
These are my manual confirmations, not device tests performed by Codex.

Actual rotation Logcat output still needs to be added to the README.

## What I still need to do

I need to review and understand the code well enough to explain it and make the
live change during the defense without AI. This file does not claim that I have
already demonstrated that.

The architecture diagram was adapted during the refactor. An architecture-first
commit was not created before the coding work, so I should not describe it as one.

## Folder organization - 14 October 2026

I asked why the shared model and data files sit outside the screen folders.
After discussing the structure, I asked Codex to apply it and document it in README.
Codex moved the files into feature packages, split the Routes and UiState files,
extracted the workout card and timer controls with their previews, and updated
imports. This folder refactor was AI-assisted.
Codex verified the debug APK build, lint, and all 31 JVM tests through JUnitCore.
The existing Gradle test worker launch failure remained. I have not reported
another phone test after this folder refactor.
