package com.ppimenta.notificationcommand.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ppimenta.notificationcommand.ui.PermissionsState

data class PermissionItem(
    val title: String,
    val description: String,
    val granted: Boolean,
    val required: Boolean,
    val onFix: () -> Unit,
)

@Composable
fun PermissionsScreen(
    state: PermissionsState,
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onRequestPostNotifications: () -> Unit,
    onOpenBatteryOptimizationSettings: () -> Unit,
    onOpenDndSettings: () -> Unit,
    onContinue: () -> Unit,
) {
    val items = listOf(
        PermissionItem(
            title = "Notification access",
            description = "Required so the app can read incoming notifications and match them against your rules.",
            granted = state.notificationAccessGranted,
            required = true,
            onFix = onOpenNotificationAccessSettings,
        ),
        PermissionItem(
            title = "Post notifications",
            description = "Needed to show the brief status notification while an alert tone plays.",
            granted = state.postNotificationsGranted,
            required = true,
            onFix = onRequestPostNotifications,
        ),
        PermissionItem(
            title = "Alarms & reminders (exact alarms)",
            description = "Lets reminder sounds fire at the exact minute you configured, instead of being delayed.",
            granted = state.exactAlarmGranted,
            required = true,
            onFix = onOpenExactAlarmSettings,
        ),
        PermissionItem(
            title = "Ignore battery optimization",
            description = "Prevents the system from killing the notification listener in the background.",
            granted = state.batteryOptimizationIgnored,
            required = true,
            onFix = onOpenBatteryOptimizationSettings,
        ),
        PermissionItem(
            title = "Do Not Disturb access (optional)",
            description = "Only needed if you rely on DND instead of silent mode: lets the app add itself to DND's priority list.",
            granted = state.dndPolicyAccessGranted,
            required = false,
            onFix = onOpenDndSettings,
        ),
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "One-time setup",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "These permissions let alerts sound even when your phone is on silent.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items) { item -> PermissionRow(item) }
        }
        Button(
            onClick = onContinue,
            enabled = state.notificationAccessGranted,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text(if (state.allGranted) "Continue" else "Continue anyway")
        }
    }
}

@Composable
private fun PermissionRow(item: PermissionItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (item.granted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (item.granted) Color(0xFF2E7D32) else if (item.required) Color(0xFFC62828) else Color(0xFFF9A825),
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(text = item.title, style = MaterialTheme.typography.titleSmall)
                Text(text = item.description, style = MaterialTheme.typography.bodySmall)
            }
            if (!item.granted) {
                TextButton(onClick = item.onFix) { Text("Fix") }
            }
        }
    }
}
