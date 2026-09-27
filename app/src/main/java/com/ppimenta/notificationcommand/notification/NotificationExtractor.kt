package com.ppimenta.notificationcommand.notification

import android.app.Notification
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

/** Pulls a best-effort sender name out of a posted notification. */
object NotificationExtractor {

    /** True for notifications we should never treat as chat messages (summaries, ongoing/foreground). */
    fun shouldIgnore(sbn: StatusBarNotification): Boolean {
        val flags = sbn.notification.flags
        if (flags and Notification.FLAG_GROUP_SUMMARY != 0) return true
        if (sbn.isOngoing) return true
        return false
    }

    fun extractSender(sbn: StatusBarNotification): String {
        val notification = sbn.notification
        val messagingStyleSender = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            NotificationCompat.MessagingStyle
                .extractMessagingStyleFromNotification(notification)
                ?.messages
                ?.lastOrNull()
                ?.person
                ?.name
                ?.toString()
        } else {
            null
        }
        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        return (messagingStyleSender ?: title ?: "").trim()
    }
}
