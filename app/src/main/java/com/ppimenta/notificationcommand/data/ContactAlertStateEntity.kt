package com.ppimenta.notificationcommand.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Per (rule, conversation) alert state. [conversationKey] is the normalized sender name for
 * that rule's matches, so the same rule can track multiple people independently.
 *
 * [nextReminderAtEpochMs] mirrors the alarm we last scheduled with [ReminderScheduler] so it can
 * be re-armed after a reboot; it is cleared once the reminder chain finishes or the user
 * acknowledges (opens/dismisses) the notification.
 */
@Entity(
    tableName = "contact_alert_states",
    indices = [Index(value = ["ruleId", "conversationKey"], unique = true)],
)
data class ContactAlertStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleId: Long,
    val conversationKey: String,
    val lastSoundAtEpochMs: Long = 0,
    val lastNotificationAtEpochMs: Long = 0,
    val remindersFired: Int = 0,
    val nextReminderAtEpochMs: Long? = null,
    val acknowledged: Boolean = false,
)
