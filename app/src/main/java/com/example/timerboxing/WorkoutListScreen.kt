package com.example.timerboxing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.timerboxing.ui.theme.Spacing
import com.example.timerboxing.ui.theme.TImerBoxingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(workouts: List<Workout> = workoutPresets) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Timer Boxing") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Text("Your workouts", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Round-based training for your next session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (workouts.isEmpty()) {
                item {
                    Text(
                        "No workouts here yet. Add a preset to get started.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = Spacing.extraLarge)
                    )
                }
            }
            items(workouts, key = { it.id }) { workout ->
                WorkoutCard(workout)
            }
            item {
                Text(
                    "Total time includes rest between rounds, with no rest after the final round.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WorkoutCard(workout: Workout, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                workout.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${workout.rounds} rounds of ${formatDuration(workout.workSeconds)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.large)
            ) {
                Text(
                    "Rest ${formatDuration(workout.restSeconds)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Total ${formatDuration(workout.totalSeconds)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutListPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutListScreen() }
}

@Preview(showBackground = true)
@Composable
fun WorkoutListDarkPreview() {
    TImerBoxingTheme(darkTheme = true, dynamicColor = false) { WorkoutListScreen() }
}

@Preview(showBackground = true)
@Composable
fun WorkoutCardPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutCard(workoutPresets.first()) }
}

@Preview(showBackground = true)
@Composable
fun EmptyWorkoutListPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutListScreen(emptyList()) }
}
