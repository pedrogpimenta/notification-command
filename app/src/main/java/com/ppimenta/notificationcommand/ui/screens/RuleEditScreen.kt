package com.ppimenta.notificationcommand.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ppimenta.notificationcommand.data.RuleEntity
import com.ppimenta.notificationcommand.ui.components.InstalledAppPickerDialog

private fun resolveAppLabel(context: android.content.Context, packageName: String?): String {
    if (packageName.isNullOrBlank()) return "Any app"
    return try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
    } catch (e: Exception) {
        packageName
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    initialRule: RuleEntity?,
    onSave: (RuleEntity) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf(initialRule?.label ?: "") }
    var senderPattern by remember { mutableStateOf(initialRule?.senderPattern ?: "") }
    var packageName by remember { mutableStateOf(initialRule?.packageName) }
    var packageLabel by remember { mutableStateOf(resolveAppLabel(context, initialRule?.packageName)) }
    var intervalMinutesText by remember { mutableStateOf((initialRule?.reminderIntervalMinutes ?: 5).toString()) }
    var reminderCountText by remember { mutableStateOf((initialRule?.reminderCount ?: 2).toString()) }
    var enabled by remember { mutableStateOf(initialRule?.enabled ?: true) }
    var showAppPicker by remember { mutableStateOf(false) }

    if (showAppPicker) {
        InstalledAppPickerDialog(
            onDismiss = { showAppPicker = false },
            onSelected = { app ->
                packageName = app.packageName
                packageLabel = app.label
                showAppPicker = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialRule == null) "New rule" else "Edit rule") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Name (e.g. \"Wife\")") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = senderPattern,
                onValueChange = { senderPattern = it },
                label = { Text("Sender name contains") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )

            Text(
                text = "App",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = packageLabel, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { showAppPicker = true }) { Text("Choose") }
                if (packageName != null) {
                    TextButton(onClick = { packageName = null; packageLabel = "Any app" }) { Text("Clear") }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                OutlinedTextField(
                    value = intervalMinutesText,
                    onValueChange = { intervalMinutesText = it.filter(Char::isDigit) },
                    label = { Text("Remind every (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = reminderCountText,
                    onValueChange = { reminderCountText = it.filter(Char::isDigit) },
                    label = { Text("Repeat (times)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Enabled", modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Button(
                onClick = {
                    onSave(
                        RuleEntity(
                            id = initialRule?.id ?: 0,
                            label = label.ifBlank { senderPattern },
                            packageName = packageName,
                            senderPattern = senderPattern,
                            enabled = enabled,
                            reminderIntervalMinutes = intervalMinutesText.toIntOrNull()?.coerceAtLeast(1) ?: 5,
                            reminderCount = reminderCountText.toIntOrNull()?.coerceAtLeast(0) ?: 2,
                        ),
                    )
                },
                enabled = senderPattern.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Text("Save")
            }

            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Delete rule")
                }
            }
        }
    }
}
