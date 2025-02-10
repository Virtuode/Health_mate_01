package com.corps.healthmate.receivers

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.corps.healthmate.database.AppDatabase
import com.corps.healthmate.database.AppDatabase.Companion.getDatabase
import com.corps.healthmate.notification.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        when (intent.action) {
            "taken" -> {
                // Cancel the notification
                val notificationId = intent.getIntExtra("notification_id", 0)
                notificationManager.cancel(notificationId)
                
                val reminderId = intent.getIntExtra("reminder_id", -1)
                if (reminderId != -1) {
                    handleMedicineTaken(context, reminderId)
                }
            }
            "snooze" -> {
                // Snooze for 10 minutes
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val newIntent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("title", intent.getStringExtra("title"))
                    putExtra("ringtone", intent.getStringExtra("ringtone"))
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    System.currentTimeMillis().toInt(),
                    newIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
                
                // Cancel current notification
                val notificationId = intent.getIntExtra("notification_id", 0)
                notificationManager.cancel(notificationId)
                
                Toast.makeText(context, "Reminder snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleMedicineTaken(context: Context, reminderId: Int) {
        AppDatabase.databaseWriteExecutor.execute {
            val db = getDatabase(context)
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val reminder = db!!.reminderDao()!!.getReminderById(reminderId)
                if (reminder != null) {
                    db.reminderDao()!!.update(reminder)

                    // Show confirmation on main thread
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "Medicine taken!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}