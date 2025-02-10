package com.corps.healthmate.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.corps.healthmate.database.AppDatabase
import com.corps.healthmate.database.Reminder
import com.corps.healthmate.receivers.AlarmReceiver
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ReminderManager private constructor(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val database = AppDatabase.getDatabase(context)

    companion object {
        @Volatile
        private var instance: ReminderManager? = null
        private const val TAG = "ReminderManager"

        fun getInstance(context: Context): ReminderManager {
            return instance ?: synchronized(this) {
                instance ?: ReminderManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun scheduleReminder(reminder: Reminder) {
        try {
            val calendar = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = timeFormat.parse(reminder.time) ?: return

            calendar.apply {
                set(Calendar.HOUR_OF_DAY, time.hours)
                set(Calendar.MINUTE, time.minutes)
                set(Calendar.SECOND, 0)
            }

            // If time has passed for today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("reminder_id", reminder.id)
                putExtra("title", "com.corps.healthmate.models.Medicine Reminder")
                putStringArrayListExtra("pills", ArrayList(reminder.pillNames))
                putExtra("dosage", reminder.dosage)
                putExtra("ringtone", reminder.ringtoneUri)
                putExtra("isHindi", reminder.isHindi)
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id,
                intent,
                flags
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            reminder.nextTriggerTime = calendar.timeInMillis
            updateReminderInDatabase(reminder)

            Timber.d("Scheduled reminder ${reminder.id} for ${Date(calendar.timeInMillis)}")
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling reminder ${reminder.id}")
        }
    }

    fun cancelReminder(reminder: Reminder) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("reminder_id", reminder.id)
                putExtra("title", "com.corps.healthmate.models.Medicine Reminder")
                putStringArrayListExtra("pills", ArrayList(reminder.pillNames))
                putExtra("dosage", reminder.dosage)
                putExtra("ringtone", reminder.ringtoneUri)
                putExtra("isHindi", reminder.isHindi)
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_NO_CREATE
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id,
                intent,
                flags
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }

            reminder.isActive = false
            updateReminderInDatabase(reminder)

            Timber.d("Cancelled reminder ${reminder.id}")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling reminder ${reminder.id}")
        }
    }

    private fun updateReminderInDatabase(reminder: Reminder) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                database!!.reminderDao()!!.update(reminder)
            } catch (e: Exception) {
                Timber.e(e, "Error updating reminder in database")
            }
        }
    }
}
