package com.ppimenta.notificationcommand.alerting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Schedules/cancels the single "next reminder" alarm for a conversation's alert state.
 * Reminders are re-armed one at a time (see [ContactAlertStateEntity.nextReminderAtEpochMs]) so
 * they can be reconstructed after a reboot without re-computing a whole future chain.
 */
object ReminderScheduler {
    const val EXTRA_STATE_ID = "state_id"

    fun schedule(context: Context, stateId: Long, atEpochMs: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = pendingIntentFor(context, stateId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atEpochMs, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atEpochMs, pendingIntent)
        }
    }

    fun cancel(context: Context, stateId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntentFor(context, stateId))
    }

    private fun pendingIntentFor(context: Context, stateId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_STATE_ID, stateId)
        }
        // stateId comes from a Room autoincrement column; truncation to Int is safe at any
        // realistic scale and only needs to be stable/unique per row.
        val requestCode = stateId.toInt()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
