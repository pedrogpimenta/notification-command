package com.ppimenta.notificationcommand.matching

import com.ppimenta.notificationcommand.data.RuleEntity

/** Decides whether an incoming notification matches a given rule. */
object RuleMatcher {
    fun matches(rule: RuleEntity, packageName: String, sender: String): Boolean {
        if (!rule.enabled) return false
        val pkg = rule.packageName
        if (!pkg.isNullOrBlank() && !pkg.equals(packageName, ignoreCase = true)) return false
        if (rule.senderPattern.isBlank()) return false
        return sender.contains(rule.senderPattern, ignoreCase = true)
    }

    /** Normalizes a sender name into a stable key used to track per-conversation alert state. */
    fun conversationKey(sender: String): String = sender.trim().lowercase()
}
