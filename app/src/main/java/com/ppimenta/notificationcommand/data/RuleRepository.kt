package com.ppimenta.notificationcommand.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/** Single entry point for reading/writing rules and per-conversation alert state. */
class RuleRepository(context: Context) {
    private val db = AppDatabase.get(context)
    private val ruleDao = db.ruleDao()
    private val stateDao = db.contactAlertStateDao()

    fun observeRules(): Flow<List<RuleEntity>> = ruleDao.observeRules()

    suspend fun getEnabledRules(): List<RuleEntity> = ruleDao.getEnabledRules()

    suspend fun getRule(ruleId: Long): RuleEntity? = ruleDao.getRule(ruleId)

    suspend fun saveRule(rule: RuleEntity): Long =
        if (rule.id == 0L) ruleDao.insert(rule) else { ruleDao.update(rule); rule.id }

    suspend fun deleteRule(rule: RuleEntity) {
        ruleDao.delete(rule)
        stateDao.deleteForRule(rule.id)
    }

    suspend fun getState(ruleId: Long, conversationKey: String): ContactAlertStateEntity? =
        stateDao.get(ruleId, conversationKey)

    suspend fun getStateById(id: Long): ContactAlertStateEntity? = stateDao.getById(id)

    suspend fun saveState(state: ContactAlertStateEntity): Long = stateDao.upsert(state)

    suspend fun getPendingReminderStates(): List<ContactAlertStateEntity> = stateDao.getPendingReminders()
}
