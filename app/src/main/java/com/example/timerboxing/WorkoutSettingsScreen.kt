package com.example.timerboxing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timerboxing.ui.theme.Spacing
import com.example.timerboxing.ui.theme.TImerBoxingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSettingsScreen(workout: Workout?, onBack: () -> Unit, onSave: (Workout) -> Unit) {
    var rounds by rememberSaveable(workout?.id) { mutableStateOf(workout?.rounds?.toString().orEmpty()) }
    var work by rememberSaveable(workout?.id) { mutableStateOf(formatDuration(workout?.workSeconds ?: 0)) }
    var rest by rememberSaveable(workout?.id) { mutableStateOf(formatDuration(workout?.restSeconds ?: 0)) }
    var prepare by rememberSaveable(workout?.id) { mutableStateOf(formatDuration(workout?.prepareSeconds ?: 0)) }
    var warning by rememberSaveable(workout?.id) { mutableStateOf(formatDuration(workout?.roundWarningSeconds ?: 0)) }
    var periodic by rememberSaveable(workout?.id) { mutableStateOf(formatDuration(workout?.periodicAlertSeconds ?: 0)) }
    var restWarning by rememberSaveable(workout?.id) { mutableStateOf(workout?.restWarningEnabled ?: false) }
    val workLimit = (parseDuration(work) ?: 1).coerceIn(1, 3600)
    val isValid = validSetting(rounds, 1..99) && validDuration(work, 1..3600) &&
        validDuration(rest, 0..3600) && validDuration(prepare, 0..300) &&
        validDuration(warning, 0..workLimit) && validDuration(periodic, 0..workLimit)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = "Back to workout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()
                .verticalScroll(rememberScrollState()).padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            if (workout == null) {
                Text("This workout is unavailable. Go back and choose another workout.")
            } else {
                Text(workout.name, style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                SettingNumberField("Number of rounds", rounds, { rounds = it }, 1..99)
                SettingDurationField("Round duration", work, { work = it }, 1..3600)
                SettingDurationField("Rest duration", rest, { rest = it }, 0..3600)
                SettingDurationField("Round warning (time remaining)", warning, { warning = it }, 0..workLimit)
                SettingDurationField("Periodic alert interval", periodic, { periodic = it }, 0..workLimit)
                SettingDurationField("Preparation time", prepare, { prepare = it }, 0..300)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.large)
                ) {
                    Text("Signal with 10 seconds of rest remaining",
                        modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = restWarning, onCheckedChange = { restWarning = it })
                }
                Text("Alerts use your device's media volume. Saving resets this workout's timer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    enabled = isValid,
                    onClick = {
                        onSave(workout.copy(
                            rounds = rounds.toInt(), workSeconds = requireNotNull(parseDuration(work)),
                            restSeconds = requireNotNull(parseDuration(rest)), prepareSeconds = requireNotNull(parseDuration(prepare)),
                            roundWarningSeconds = requireNotNull(parseDuration(warning)), periodicAlertSeconds = requireNotNull(parseDuration(periodic)),
                            restWarningEnabled = restWarning
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text("Save settings") }
            }
        }
    }
}

fun validSetting(value: String, range: IntRange): Boolean = value.toIntOrNull()?.let { it in range } == true

@Composable
fun SettingNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    range: IntRange
) {
    val valid = validSetting(value, range)
    OutlinedTextField(
        value = value,
        onValueChange = { input -> if (input.length <= 4 && input.all { it in '0'..'9' }) onValueChange(input) },
        label = { Text(label) },
        singleLine = true,
        isError = !valid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = {
            Text(when {
                !valid -> "Enter a value from ${range.first} to ${range.last}"
                else -> "${range.first}-${range.last} rounds"
            })
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SettingDurationField(label: String, value: String, onValueChange: (String) -> Unit, range: IntRange) {
    val valid = validDuration(value, range)
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.length <= 5 && input.all { it in '0'..'9' || it == ':' }) onValueChange(input)
        },
        label = { Text(label) },
        placeholder = { Text("00:00") },
        singleLine = true,
        isError = !valid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        supportingText = {
            Text(when {
                !valid -> "Use MM:SS, from ${formatDuration(range.first)} to ${formatDuration(range.last)}"
                parseDuration(value) == 0 -> "MM:SS • Off"
                else -> "MM:SS • Minutes:seconds"
            })
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun WorkoutSettingsPreview() {
    TImerBoxingTheme(dynamicColor = false) { WorkoutSettingsScreen(workoutPresets.first(), {}, {}) }
}

@Preview(showBackground = true)
@Composable
fun WorkoutSettingsDarkPreview() {
    TImerBoxingTheme(darkTheme = true, dynamicColor = false) {
        WorkoutSettingsScreen(workoutPresets.first(), {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun SettingNumberFieldPreview() {
    TImerBoxingTheme(dynamicColor = false) {
        SettingNumberField("Number of rounds", "10", {}, 1..99)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingDurationFieldPreview() {
    TImerBoxingTheme(dynamicColor = false) {
        SettingDurationField("Round duration", "03:00", {}, 1..3600)
    }
}
