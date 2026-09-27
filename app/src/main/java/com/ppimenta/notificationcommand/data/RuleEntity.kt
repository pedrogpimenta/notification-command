package com.ppimenta.notificationcommand.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single alert rule: which notifications to watch, and how to remind about them.
 *
 * [packageName] null/blank means "any app". [senderPattern] is matched case-insensitively
 * as a substring against the notification's title (sender/contact name).
 */
@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val packageName: String? = null,
    val senderPattern: String,
    val enabled: Boolean = true,
    val reminderIntervalMinutes: Int = 5,
    val reminderCount: Int = 2,
)
