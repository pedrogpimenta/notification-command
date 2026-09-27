package com.ppimenta.notificationcommand.ui.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class InstalledApp(val label: String, val packageName: String)

private fun loadLaunchableApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    @Suppress("DEPRECATION")
    val resolveInfos = pm.queryIntentActivities(intent, 0)
    return resolveInfos
        .map { InstalledApp(label = it.loadLabel(pm).toString(), packageName = it.activityInfo.packageName) }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}

@Composable
fun InstalledAppPickerDialog(onDismiss: () -> Unit, onSelected: (InstalledApp) -> Unit) {
    val context = LocalContext.current
    val apps = remember { loadLaunchableApps(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose an app") },
        text = {
            LazyColumn {
                items(apps, key = { it.packageName }) { app ->
                    ListItem(
                        headlineContent = { Text(app.label) },
                        supportingContent = { Text(app.packageName) },
                        modifier = Modifier
                            .clickable { onSelected(app) }
                            .padding(vertical = 2.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
