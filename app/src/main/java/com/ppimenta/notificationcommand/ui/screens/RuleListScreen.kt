package com.ppimenta.notificationcommand.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ppimenta.notificationcommand.data.RuleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleListScreen(
    rules: List<RuleEntity>,
    permissionsAllGranted: Boolean,
    onOpenPermissions: () -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (RuleEntity) -> Unit,
    onDeleteRule: (RuleEntity) -> Unit,
    onSetEnabled: (RuleEntity, Boolean) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Notification Command") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRule) {
                Icon(Icons.Filled.Add, contentDescription = "Add rule")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (!permissionsAllGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable(onClick = onOpenPermissions),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null)
                        Text(
                            text = "Some permissions are missing — tap to fix setup",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (rules.isEmpty()) {
                Text(
                    text = "No rules yet. Tap + to alert on a contact's messages.",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(rules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        onClick = { onEditRule(rule) },
                        onDelete = { onDeleteRule(rule) },
                        onSetEnabled = { enabled -> onSetEnabled(rule, enabled) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleRow(
    rule: RuleEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSetEnabled: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "matches \"${rule.senderPattern}\" in ${rule.packageName ?: "any app"}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "then every ${rule.reminderIntervalMinutes} min, up to ${rule.reminderCount}x",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(checked = rule.enabled, onCheckedChange = onSetEnabled)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete rule")
            }
        }
    }
}
