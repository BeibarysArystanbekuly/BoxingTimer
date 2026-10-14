package com.example.timerboxing.features.workoutdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timerboxing.R
import com.example.timerboxing.ui.theme.Spacing
import com.example.timerboxing.ui.theme.TImerBoxingTheme

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
