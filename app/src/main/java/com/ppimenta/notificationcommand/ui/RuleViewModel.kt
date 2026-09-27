package com.ppimenta.notificationcommand.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ppimenta.notificationcommand.data.RuleEntity
import com.ppimenta.notificationcommand.data.RuleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RuleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RuleRepository(application)

    val rules: StateFlow<List<RuleEntity>> = repository.observeRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveRule(rule: RuleEntity) {
        viewModelScope.launch { repository.saveRule(rule) }
    }

    fun deleteRule(rule: RuleEntity) {
        viewModelScope.launch { repository.deleteRule(rule) }
    }

    fun setEnabled(rule: RuleEntity, enabled: Boolean) {
        viewModelScope.launch { repository.saveRule(rule.copy(enabled = enabled)) }
    }
}
