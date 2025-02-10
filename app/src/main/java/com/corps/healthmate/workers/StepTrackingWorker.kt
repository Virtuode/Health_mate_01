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
import java.util.concurrent.TimeUnit

class StepTrackingWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        checkPhoneUsageDuringWalk()
        return Result.success()
    }

    private fun checkPhoneUsageDuringWalk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val currentTime = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - TimeUnit.MINUTES.toMillis(15),
                currentTime
            )

            if (!stats.isNullOrEmpty()) {
                // If there's significant phone usage during walking, show notification
                showPhoneUsageWarning()
            }
        }
    }

    private fun showPhoneUsageWarning() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Walking Safety")
            .setContentText("Please avoid using your phone while walking for your safety.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "step_tracking_channel"
        private const val NOTIFICATION_ID = 2
    }
}
