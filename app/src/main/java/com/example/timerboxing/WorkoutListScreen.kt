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
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
fun WorkoutListScreen(
    uiState: WorkoutListUiState,
    onWorkoutClick: (Int) -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onSortChange: (WorkoutSortOrder) -> Unit = {},
    onRetry: () -> Unit = {},
    onSimulateError: () -> Unit = {}
) {
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
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    label = { Text("Search workouts") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Sort by name", modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.sortOrder == WorkoutSortOrder.NAME,
                        onCheckedChange = { onSortChange(if (it) WorkoutSortOrder.NAME else WorkoutSortOrder.DEFAULT) }
                    )
                }
            }
            if (uiState.isLoading) {
                item { CircularProgressIndicator() }
            } else if (uiState.error != null) {
                item {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) { Text("Retry") }
                }
            } else if (uiState.workouts.isEmpty()) {
                item {
                    Text(
                        "No workouts match your search.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = Spacing.extraLarge)
                    )
                }
            }
            if (!uiState.isLoading && uiState.error == null) {
                items(uiState.workouts, key = { it.id }) { workout ->
                    WorkoutCard(workout, onClick = { onWorkoutClick(workout.id) })
                }
            }
            if (BuildConfig.DEBUG) {
                item { TextButton(onClick = onSimulateError) { Text("Simulate loading error") } }
            }
            item {
                Text(
                    "Total time includes preparation and rest between rounds, with no rest after the final round.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WorkoutCard(workout: Workout, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
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
    TImerBoxingTheme(dynamicColor = false) { WorkoutListScreen(WorkoutListUiState(workouts = workoutPresets, isLoading = false)) }
}

@Preview(showBackground = true)
@Composable
fun WorkoutListDarkPreview() {
    TImerBoxingTheme(darkTheme = true, dynamicColor = false) { WorkoutListScreen(WorkoutListUiState(workouts = workoutPresets, isLoading = false)) }
}

@Preview(showBackground = true)
@Composable
fun WorkoutCardPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutCard(workoutPresets.first()) }
}

@Preview(showBackground = true)
@Composable
fun EmptyWorkoutListPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutListScreen(WorkoutListUiState(isLoading = false)) }
}

@Preview(showBackground = true)
@Composable
fun LoadingWorkoutListPreview() {
    TImerBoxingTheme { WorkoutListScreen(WorkoutListUiState()) }
}

@Preview(showBackground = true)
@Composable
fun ErrorWorkoutListPreview() {
    TImerBoxingTheme { WorkoutListScreen(WorkoutListUiState(isLoading = false, error = "Could not load workouts.")) }
}
