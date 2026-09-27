package com.ppimenta.notificationcommand.alerting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.ppimenta.notificationcommand.data.RuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fires when a scheduled reminder is due; plays a tone and re-arms the next one if any remain. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val stateId = intent.getLongExtra(ReminderScheduler.EXTRA_STATE_ID, -1L)
        if (stateId < 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(context, stateId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(context: Context, stateId: Long) {
        val repository = RuleRepository(context)
        val state = repository.getStateById(stateId) ?: return
        if (state.acknowledged) return

        val rule = repository.getRule(state.ruleId) ?: return
        if (!rule.enabled) return

        ContextCompat.startForegroundService(context, SoundPlaybackService.newIntent(context))

        val firedCount = state.remindersFired + 1
        val hasMoreReminders = firedCount < rule.reminderCount
        val nextAt = if (hasMoreReminders) {
            System.currentTimeMillis() + rule.reminderIntervalMinutes * 60_000L
        } else {
            null
        }

        repository.saveState(
            state.copy(remindersFired = firedCount, nextReminderAtEpochMs = nextAt),
        )

        if (nextAt != null) {
            ReminderScheduler.schedule(context, stateId, nextAt)
        }
    }
}
