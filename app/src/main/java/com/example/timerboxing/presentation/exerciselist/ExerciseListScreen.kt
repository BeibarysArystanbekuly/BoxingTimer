package com.example.timerboxing.presentation.exerciselist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.timerboxing.R
import com.example.timerboxing.domain.model.Exercise
import com.example.timerboxing.presentation.UiState
import com.example.timerboxing.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    state: UiState<Exercise>,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "Back to workouts")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> {
                    if (state.items.isEmpty()) {
                        Text("No exercises available.", modifier = Modifier.padding(Spacing.large))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.large),
                            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                        ) {
                            items(state.items, key = { it.id }) { exercise ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(Spacing.large),
                                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                                    ) {
                                        Text("Exercise #${exercise.id}", style = MaterialTheme.typography.titleMedium)
                                        Text("Muscle groups: ${exercise.muscleIds.size}")
                                        Text("Equipment types: ${exercise.equipmentIds.size}")
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.padding(Spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Button(onClick = onRetry) { Text("Retry") }
                    }
                }
            }
        }
    }
}
