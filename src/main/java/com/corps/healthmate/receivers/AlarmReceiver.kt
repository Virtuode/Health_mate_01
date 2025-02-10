class AlarmReceiver : BroadcastReceiver() {
    // ...existing code...

    override fun onReceive(context: Context, intent: Intent) {
        // ...existing code...

        if ("snooze" == intent.action) {
            val reminderId = intent.getIntExtra("reminder_id", 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val snoozeIntent = PendingIntent.getBroadcast(context, reminderId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val triggerTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, snoozeIntent)
            return
        }

        // ...existing code...
    }

    // ...existing code...
}
