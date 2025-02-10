package com.corps.healthmate.notification

import com.corps.healthmate.receivers.ReminderActionReceiver


class NotificationHelper(private val context: Context) {
    // ...existing code...

    fun sendReminderNotification(title: String?, pills: List<String>, soundUri: Uri) {
        // ...existing code...

        val notification = NotificationCompat.Builder(context, channelId)
            // ...existing code...
            .addAction(
                R.drawable.ic_check,
                "Taken",
                createActionPendingIntent("taken", reminderId)
            )
            .addAction(
                R.drawable.ic_snooze_24,
                "Snooze",
                createActionPendingIntent("snooze", reminderId)
            )
            .addAction(
                R.drawable.ic_cancel,
                "Dismiss",
                createActionPendingIntent("dismiss", reminderId)
            )
            .build()

        notificationManager.notify(reminderId, notification)
    }

    private fun createActionPendingIntent(action: String, reminderId: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra("reminder_id", reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            when (action) {
                "taken" -> 0
                "snooze" -> 1
                "dismiss" -> 2
                else -> 3
            },
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ...existing code...
}
