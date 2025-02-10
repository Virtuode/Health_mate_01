package com.corps.healthmate.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.corps.healthmate.database.ReminderDatabase
import com.corps.healthmate.receivers.ReminderScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = ReminderDatabase.getDatabase(context)
            val reminders = db.reminderDao()?.getAllRemindersForUserSync(null)
            reminders?.forEach { reminder ->
                // Reschedule alarms for active reminders
                if (reminder?.isActive == true) {
                    ReminderScheduler.scheduleReminder(
                        context,
                        reminder.triggerAtMillis,
                        reminder.title,
                        reminder.message,
                        reminder.audioFilePath
                    )
                }
            }
        }
    }
}