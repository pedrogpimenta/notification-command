package com.ppimenta.notificationcommand.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentToneLabel: String,
    isDefaultTone: Boolean,
    onPickTone: () -> Unit,
    onResetTone: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Alert tone", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Played on every matching notification and reminder, for every rule.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                    )
                    Text(text = currentToneLabel, style = MaterialTheme.typography.bodyMedium)
                    Row(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedButton(onClick = onPickTone) { Text("Choose tone") }
                        if (!isDefaultTone) {
                            TextButton(
                                onClick = onResetTone,
                                modifier = Modifier.padding(start = 8.dp),
                                content = { Text("Reset to default") },
                            )
                        }
                    }
                }
            }
            Text(
                text = "Note: the tone always plays on the alarm volume, regardless of ringer/silent mode.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
