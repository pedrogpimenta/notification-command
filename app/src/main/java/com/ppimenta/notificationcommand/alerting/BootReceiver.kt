package com.ppimenta.notificationcommand.alerting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ppimenta.notificationcommand.data.RuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** AlarmManager alarms are wiped on reboot, so re-arm any reminder chains still in flight. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = RuleRepository(context)
                val now = System.currentTimeMillis()
                repository.getPendingReminderStates().forEach { state ->
                    val rule = repository.getRule(state.ruleId) ?: return@forEach
                    if (!rule.enabled) return@forEach
                    val nextAt = state.nextReminderAtEpochMs ?: return@forEach
                    ReminderScheduler.schedule(context, state.id, maxOf(nextAt, now + 5_000L))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
