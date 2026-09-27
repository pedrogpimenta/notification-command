package com.ppimenta.notificationcommand.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactAlertStateDao {
    @Query("SELECT * FROM contact_alert_states WHERE ruleId = :ruleId AND conversationKey = :conversationKey LIMIT 1")
    suspend fun get(ruleId: Long, conversationKey: String): ContactAlertStateEntity?

    @Query("SELECT * FROM contact_alert_states WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ContactAlertStateEntity?

    @Query("SELECT * FROM contact_alert_states WHERE nextReminderAtEpochMs IS NOT NULL AND acknowledged = 0")
    suspend fun getPendingReminders(): List<ContactAlertStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ContactAlertStateEntity): Long

    @Update
    suspend fun update(state: ContactAlertStateEntity)

    @Query("DELETE FROM contact_alert_states WHERE ruleId = :ruleId")
    suspend fun deleteForRule(ruleId: Long)
}
