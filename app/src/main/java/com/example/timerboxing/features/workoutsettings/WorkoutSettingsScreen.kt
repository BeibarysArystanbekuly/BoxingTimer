package com.example.timerboxing.features.workoutsettings

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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timerboxing.R
import com.example.timerboxing.model.workoutPresets
import com.example.timerboxing.ui.theme.Spacing
import com.example.timerboxing.ui.theme.TImerBoxingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSettingsScreen(
    uiState: WorkoutSettingsUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onFieldChange: (WorkoutSetting, String) -> Unit,
    onRestWarningChange: (Boolean) -> Unit,
    onRetry: () -> Unit
) {
    val draft = uiState.draft
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
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (draft == null) {
                Text(uiState.error ?: "This workout is unavailable. Go back and choose another workout.")
                if (uiState.error != null) Button(onClick = onRetry) { Text("Retry") }
            } else {
                Text(draft.workout.name, style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                draft.fields.forEach { field ->
                    SettingInputField(field, { onFieldChange(field.setting, it) }, uiState.validationErrors[field.setting], !uiState.isSaving)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.large)
                ) {
                    Text("Signal with 10 seconds of rest remaining",
                        modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = draft.restWarning, onCheckedChange = onRestWarningChange,
                        enabled = !uiState.isSaving)
                }
                Text("Alerts use your device's media volume. New settings apply after resetting the timer or starting a new session.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    enabled = uiState.validationErrors.isEmpty() && !uiState.isSaving,
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text(if (uiState.isSaving) "Saving..." else "Save settings") }
            }
        }
    }
}

@Composable
fun SettingInputField(
    field: SettingField,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = field.value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(field.label) },
        singleLine = true,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = if (field.isDuration) KeyboardType.Ascii else KeyboardType.Number),
        supportingText = { Text(error ?: if (field.isDuration) "MM:SS - Minutes:seconds" else "1-99 rounds") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsPreview(state: WorkoutSettingsUiState) {
    TImerBoxingTheme(dynamicColor = false) { WorkoutSettingsScreen(state, {}, {}, { _, _ -> }, {}, {}) }
}

@Preview(showBackground = true)
@Composable
fun WorkoutSettingsPreview() = SettingsPreview(WorkoutSettingsUiState(draft = settingsDraft(workoutPresets.first()), isLoading = false))

@Preview(showBackground = true)
@Composable
fun LoadingSettingsPreview() = SettingsPreview(WorkoutSettingsUiState())

@Preview(showBackground = true)
@Composable
fun EmptySettingsPreview() = SettingsPreview(WorkoutSettingsUiState(isLoading = false))

@Preview(showBackground = true)
@Composable
fun ErrorSettingsPreview() = SettingsPreview(WorkoutSettingsUiState(isLoading = false, error = "Could not load settings."))
