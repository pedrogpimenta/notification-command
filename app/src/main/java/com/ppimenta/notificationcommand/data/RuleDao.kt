package com.ppimenta.notificationcommand.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY label ASC")
    fun observeRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE enabled = 1")
    suspend fun getEnabledRules(): List<RuleEntity>

    @Query("SELECT * FROM rules WHERE id = :ruleId")
    suspend fun getRule(ruleId: Long): RuleEntity?

    @Insert
    suspend fun insert(rule: RuleEntity): Long

    @Update
    suspend fun update(rule: RuleEntity)

    @Delete
    suspend fun delete(rule: RuleEntity)
}
