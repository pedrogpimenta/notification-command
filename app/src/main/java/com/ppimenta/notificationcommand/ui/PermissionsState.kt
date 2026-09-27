package com.ppimenta.notificationcommand.ui

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

data class PermissionsState(
    val notificationAccessGranted: Boolean,
    val exactAlarmGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val postNotificationsGranted: Boolean,
    val dndPolicyAccessGranted: Boolean,
) {
    val allGranted: Boolean
        get() = notificationAccessGranted && exactAlarmGranted && batteryOptimizationIgnored && postNotificationsGranted

    companion object {
        fun snapshot(context: Context): PermissionsState {
            val notificationAccessGranted = NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)

            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val exactAlarmGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()

            val powerManager = context.getSystemService(PowerManager::class.java)
            val batteryOptimizationIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)

            val postNotificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

            val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
            val dndPolicyAccessGranted = notificationManager.isNotificationPolicyAccessGranted

            return PermissionsState(
                notificationAccessGranted = notificationAccessGranted,
                exactAlarmGranted = exactAlarmGranted,
                batteryOptimizationIgnored = batteryOptimizationIgnored,
                postNotificationsGranted = postNotificationsGranted,
                dndPolicyAccessGranted = dndPolicyAccessGranted,
            )
        }
    }
}
