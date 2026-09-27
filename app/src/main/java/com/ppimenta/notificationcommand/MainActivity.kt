package com.ppimenta.notificationcommand

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ppimenta.notificationcommand.data.AlertPreferences
import com.ppimenta.notificationcommand.data.RuleEntity
import com.ppimenta.notificationcommand.ui.PermissionsState
import com.ppimenta.notificationcommand.ui.RuleViewModel
import com.ppimenta.notificationcommand.ui.screens.PermissionsScreen
import com.ppimenta.notificationcommand.ui.screens.RuleEditScreen
import com.ppimenta.notificationcommand.ui.screens.RuleListScreen
import com.ppimenta.notificationcommand.ui.screens.SettingsScreen
import com.ppimenta.notificationcommand.ui.theme.NotificationCommandTheme

class MainActivity : ComponentActivity() {
    private val viewModel: RuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationCommandTheme {
                Surface { AppRoot(viewModel) }
            }
        }
    }
}

private sealed interface Screen {
    data object Permissions : Screen
    data object List : Screen
    data object Settings : Screen
    data class Edit(val rule: RuleEntity?) : Screen
}

private fun alertToneLabel(context: Context, toneUri: Uri?): String {
    val effectiveUri = toneUri ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
    val title = effectiveUri?.let { RingtoneManager.getRingtone(context, it)?.getTitle(context) }
    return title ?: "System default"
}

private fun extractPickedRingtoneUri(intent: Intent?): Uri? {
    intent ?: return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
    }
}

@Composable
private fun AppRoot(viewModel: RuleViewModel) {
    val context = LocalContext.current
    var permissionsState by remember { mutableStateOf(PermissionsState.snapshot(context)) }
    var screen by remember {
        mutableStateOf<Screen>(
            if (permissionsState.notificationAccessGranted) Screen.List else Screen.Permissions,
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsState = PermissionsState.snapshot(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val postNotificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { permissionsState = PermissionsState.snapshot(context) }

    var toneUri by remember { mutableStateOf(AlertPreferences.getToneUri(context)) }
    val toneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedUri = extractPickedRingtoneUri(result.data)
            AlertPreferences.setToneUri(context, pickedUri)
            toneUri = pickedUri
        }
    }

    val rules by viewModel.rules.collectAsState()

    when (val current = screen) {
        Screen.Permissions -> PermissionsScreen(
            state = permissionsState,
            onOpenNotificationAccessSettings = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            onOpenExactAlarmSettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:${context.packageName}"),
                        ),
                    )
                }
            },
            onRequestPostNotifications = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    postNotificationsLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onOpenBatteryOptimizationSettings = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            },
            onOpenDndSettings = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            },
            onContinue = { screen = Screen.List },
        )

        Screen.List -> RuleListScreen(
            rules = rules,
            permissionsAllGranted = permissionsState.allGranted,
            onOpenPermissions = { screen = Screen.Permissions },
            onAddRule = { screen = Screen.Edit(null) },
            onEditRule = { rule -> screen = Screen.Edit(rule) },
            onDeleteRule = { rule -> viewModel.deleteRule(rule) },
            onSetEnabled = { rule, enabled -> viewModel.setEnabled(rule, enabled) },
            onOpenSettings = { screen = Screen.Settings },
        )

        Screen.Settings -> SettingsScreen(
            currentToneLabel = alertToneLabel(context, toneUri),
            isDefaultTone = toneUri == null,
            onPickTone = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        toneUri ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM),
                    )
                }
                toneLauncher.launch(intent)
            },
            onResetTone = {
                AlertPreferences.setToneUri(context, null)
                toneUri = null
            },
            onBack = { screen = Screen.List },
        )

        is Screen.Edit -> RuleEditScreen(
            initialRule = current.rule,
            onSave = { rule ->
                viewModel.saveRule(rule)
                screen = Screen.List
            },
            onDelete = current.rule?.let { rule ->
                {
                    viewModel.deleteRule(rule)
                    screen = Screen.List
                }
            },
            onCancel = { screen = Screen.List },
        )
    }
}
