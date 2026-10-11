package com.example.timerboxing

import android.os.SystemClock
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.timerboxing.ui.theme.Spacing
import com.example.timerboxing.ui.theme.TImerBoxingTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(workout: Workout?, onBack: () -> Unit, onSettings: () -> Unit = {}) {
    var elapsedMillis by rememberSaveable(workout?.id) { mutableStateOf(0L) }
    var isRunning by remember(workout?.id) { mutableStateOf(false) }
    var hasStarted by rememberSaveable(workout?.id) { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var alertTone by remember { mutableStateOf<ToneGenerator?>(null) }
    DisposableEffect(Unit) {
        val tone = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: RuntimeException) {
            null
        }
        alertTone = tone
        onDispose { tone?.release() }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) isRunning = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isRunning, workout?.id) {
        if (isRunning && workout != null) {
            val initialElapsed = elapsedMillis
            val startedAt = SystemClock.elapsedRealtime()
            val durationMillis = workout.totalSeconds * 1000L
            while (isRunning) {
                val previousSecond = (elapsedMillis / 1000).toInt()
                elapsedMillis = (initialElapsed + SystemClock.elapsedRealtime() - startedAt)
                    .coerceAtMost(durationMillis)
                val currentSecond = (elapsedMillis / 1000).toInt()
                if (currentSecond > previousSecond &&
                    (previousSecond + 1..currentSecond).any { workout.shouldAlert(it) }) {
                    alertTone?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                }
                if (elapsedMillis >= durationMillis) isRunning = false
                else delay(100)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        workout?.name ?: "Workout not found",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "Back to workouts")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState()).padding(Spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
        ) {
            if (workout == null) {
                Text("This workout is unavailable. Go back and choose another workout.")
            } else {
                val reading = workout.timerReading(elapsedMillis)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.large)
                ) {
                    Text(
                        "Work ${formatDuration(workout.workSeconds)}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Rest ${formatDuration(workout.restSeconds)}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    when {
                        reading.isFinished -> "Workout complete"
                        reading.isPreparing -> "Get ready"
                        reading.isRest -> "Rest"
                        else -> "Work"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    formatDuration(reading.remainingSeconds),
                    style = MaterialTheme.typography.displayLarge
                )
                Text("Round ${reading.round}/${workout.rounds}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    when {
                        reading.isFinished -> "Great work! Press play to train again."
                        isRunning -> "Session in progress"
                        hasStarted -> "Paused"
                        else -> "Ready when you are"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                TimerControls(
                    isRunning = isRunning,
                    isPaused = hasStarted && !isRunning && !reading.isFinished,
                    isFinished = reading.isFinished,
                    onPlayPause = {
                        if (reading.isFinished) elapsedMillis = 0L
                        hasStarted = true
                        isRunning = !isRunning
                    },
                    onStop = {
                        isRunning = false
                        elapsedMillis = 0L
                        hasStarted = false
                    },
                    onNext = { elapsedMillis = workout.nextPhaseElapsedMillis(elapsedMillis) }
                )
                Text(
                    "The timer pauses when you leave the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    IconButton(onClick = {
                        isRunning = false
                        onSettings()
                    }) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = "Open workout settings")
                    }
                    Text("Workout settings", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun TimerControls(
    isRunning: Boolean,
    isPaused: Boolean,
    isFinished: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        if (isPaused) {
            IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                Icon(
                    painterResource(R.drawable.ic_next),
                    contentDescription = "Skip to next round or rest"
                )
            }
        }
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(96.dp),
            shape = CircleShape
        ) {
            Icon(
                painterResource(if (isRunning) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = when {
                    isRunning -> "Pause workout"
                    isFinished -> "Restart workout"
                    isPaused -> "Resume workout"
                    else -> "Start workout"
                },
                modifier = Modifier.size(48.dp)
            )
        }
        if (isPaused) {
            FilledIconButton(
                onClick = onStop,
                modifier = Modifier.size(96.dp),
                shape = CircleShape
            ) {
                Icon(
                    painterResource(R.drawable.ic_stop),
                    contentDescription = "Stop and reset workout",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PausedTimerControlsPreview() {
    TImerBoxingTheme(dynamicColor = false) {
        TimerControls(false, true, false, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun RunningTimerControlsPreview() {
    TImerBoxingTheme(dynamicColor = false) {
        TimerControls(true, false, false, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutDetailPreview() {
    TImerBoxingTheme(dynamicColor = false) {
        WorkoutDetailScreen(workoutPresets.first(), onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutDetailDarkPreview() {
    TImerBoxingTheme(darkTheme = true, dynamicColor = false) {
        WorkoutDetailScreen(workoutPresets.first(), onBack = {})
    }
}
