package com.corps.healthmate.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.corps.healthmate.R
import java.util.*
import java.util.concurrent.TimeUnit

class SleepTrackingWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        analyzeSleepPatterns()
        return Result.success()
    }

    private fun analyzeSleepPatterns() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.HOUR_OF_DAY, -8)
            val startTime = calendar.timeInMillis

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (!stats.isNullOrEmpty()) {
                // Analyze phone usage patterns during bedtime
                provideSleepSuggestions()
            }
        }
    }

    private fun provideSleepSuggestions() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val suggestion = when {
            currentHour >= 22 -> "It's getting late. Consider putting your phone away and preparing for bed."
            currentHour in 6..8 -> "Good morning! Try to get some sunlight to regulate your sleep cycle."
            else -> "Maintain a consistent sleep schedule for better rest."
        }

        showSleepNotification(suggestion)
    }

    private fun showSleepNotification(message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sleep Suggestion")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "sleep_tracking_channel"
        private const val NOTIFICATION_ID = 3
    }
}
