package com.example.timerboxing

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
internal fun rememberWorkoutSignal(): () -> Unit {
    if (LocalInspectionMode.current) return {}
    val signal = remember { WorkoutSignal() }
    DisposableEffect(signal) {
        signal.initialize()
        onDispose { signal.release() }
    }
    return remember(signal) { { signal.play() } }
}

private class WorkoutSignal {
    private var tone: ToneGenerator? = null

    fun initialize() {
        tone = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: RuntimeException) {
            null
        }
    }

    fun play() {
        tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
    }

    fun release() {
        tone?.release()
        tone = null
    }
}
