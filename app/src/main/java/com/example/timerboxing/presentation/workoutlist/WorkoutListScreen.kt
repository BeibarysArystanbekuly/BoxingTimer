package com.example.timerboxing.presentation.workoutlist

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.timerboxing.BuildConfig
import com.example.timerboxing.data.remote.workoutPresets
import com.example.timerboxing.presentation.UiState
import com.example.timerboxing.presentation.workoutlist.components.WorkoutCard
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
    onSimulateError: () -> Unit = {},
    onBrowseExercises: () -> Unit = {}
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
                TextButton(onClick = onBrowseExercises) { Text("Browse exercises") }
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
            when (val content = uiState.content) {
                UiState.Loading -> item { CircularProgressIndicator() }
                is UiState.Error -> item {
                    Text(content.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry) { Text("Retry") }
                }
                is UiState.Success -> {
                    if (content.items.isEmpty()) {
                        item {
                            Text(
                                "No workouts match your search.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = Spacing.extraLarge)
                            )
                        }
                    }
                    items(content.items, key = { it.id }) { workout ->
                        WorkoutCard(workout, onClick = { onWorkoutClick(workout.id) })
                    }
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

@Preview(showBackground = true)
@Composable
fun WorkoutListPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutListScreen(WorkoutListUiState(content = UiState.Success(workoutPresets))) }
}

@Preview(showBackground = true)
@Composable
fun WorkoutListDarkPreview() {
    TImerBoxingTheme(darkTheme = true, dynamicColor = false) { WorkoutListScreen(WorkoutListUiState(content = UiState.Success(workoutPresets))) }
}

@Preview(showBackground = true)
@Composable
fun EmptyWorkoutListPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutListScreen(WorkoutListUiState(content = UiState.Success(emptyList()))) }
}

@Preview(showBackground = true)
@Composable
fun LoadingWorkoutListPreview() {
    TImerBoxingTheme { WorkoutListScreen(WorkoutListUiState()) }
}

@Preview(showBackground = true)
@Composable
fun ErrorWorkoutListPreview() {
    TImerBoxingTheme { WorkoutListScreen(WorkoutListUiState(content = UiState.Error("Could not load workouts."))) }
}
