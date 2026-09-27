package com.ppimenta.notificationcommand.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ppimenta.notificationcommand.alerting.AlertTonePlayer
import com.ppimenta.notificationcommand.alerting.ReminderScheduler
import com.ppimenta.notificationcommand.data.ContactAlertStateEntity
import com.ppimenta.notificationcommand.data.DEBOUNCE_WINDOW_MS
import com.ppimenta.notificationcommand.data.RuleEntity
import com.ppimenta.notificationcommand.data.RuleRepository
import com.ppimenta.notificationcommand.matching.RuleMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Reads every posted/removed notification on the device (once the user grants notification
 * access) and drives rule matching, the 1-per-minute debounce, and the reminder chain.
 */
class NotificationCommandListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: RuleRepository

    override fun onCreate() {
        super.onCreate()
        repository = RuleRepository(applicationContext)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        if (NotificationExtractor.shouldIgnore(sbn)) return

        val sender = NotificationExtractor.extractSender(sbn)
        if (sender.isBlank()) return
        val notifyingPackage = sbn.packageName

        serviceScope.launch {
            val now = System.currentTimeMillis()
            repository.getEnabledRules()
                .filter { RuleMatcher.matches(it, notifyingPackage, sender) }
                .forEach { rule -> handleMatch(rule, sender, now) }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        if (NotificationExtractor.shouldIgnore(sbn)) return

        val sender = NotificationExtractor.extractSender(sbn)
        if (sender.isBlank()) return
        val notifyingPackage = sbn.packageName

        serviceScope.launch {
            repository.getEnabledRules()
                .filter { RuleMatcher.matches(it, notifyingPackage, sender) }
                .forEach { rule -> handleAcknowledged(rule, sender) }
        }
    }

    private suspend fun handleMatch(rule: RuleEntity, sender: String, now: Long) {
        val conversationKey = RuleMatcher.conversationKey(sender)
        val existing = repository.getState(rule.id, conversationKey)
        val withinDebounce = existing != null &&
            existing.lastSoundAtEpochMs != 0L &&
            (now - existing.lastSoundAtEpochMs) < DEBOUNCE_WINDOW_MS

        if (withinDebounce) {
            // Same sender within the debounce window: no extra sound, but the conversation is
            // clearly active again, so any earlier "acknowledged" state no longer holds.
            repository.saveState(existing!!.copy(lastNotificationAtEpochMs = now, acknowledged = false))
            return
        }

        AlertTonePlayer.play(applicationContext)

        val nextReminderAt = if (rule.reminderCount > 0) {
            now + rule.reminderIntervalMinutes * 60_000L
        } else {
            null
        }
        val stateId = repository.saveState(
            (existing ?: ContactAlertStateEntity(ruleId = rule.id, conversationKey = conversationKey)).copy(
                lastSoundAtEpochMs = now,
                lastNotificationAtEpochMs = now,
                remindersFired = 0,
                nextReminderAtEpochMs = nextReminderAt,
                acknowledged = false,
            ),
        )

        if (nextReminderAt != null) {
            ReminderScheduler.schedule(applicationContext, stateId, nextReminderAt)
        } else {
            ReminderScheduler.cancel(applicationContext, stateId)
        }
    }

    private suspend fun handleAcknowledged(rule: RuleEntity, sender: String) {
        val conversationKey = RuleMatcher.conversationKey(sender)
        val existing = repository.getState(rule.id, conversationKey) ?: return
        if (existing.acknowledged) return

        repository.saveState(existing.copy(acknowledged = true, nextReminderAtEpochMs = null))
        ReminderScheduler.cancel(applicationContext, existing.id)
    }
}
