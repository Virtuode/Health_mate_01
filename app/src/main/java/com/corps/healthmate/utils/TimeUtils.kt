package com.corps.healthmate.utils

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.corps.healthmate.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object TimeUtils {
    private const val DUE_NOW_THRESHOLD_MINUTES = 15 // Show "Due Now" if within 15 minutes
    private const val URGENT_THRESHOLD_MINUTES = 60 // Show in red if within 60 minutes

    fun calculateTimeDifference(context: Context, textView: TextView, reminderTime: String?) {
        try {
            if (reminderTime.isNullOrEmpty()) {
                setTextViewState(textView, context.getString(R.string.time_error), Color.GRAY)
                return
            }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Using 24-hour format
            val reminderDate = timeFormat.parse(reminderTime) ?: return
            val currentDate = Date()

            val reminderCalendar = Calendar.getInstance()
            val currentCalendar = Calendar.getInstance()

            reminderCalendar.time = reminderDate
            currentCalendar.time = currentDate

            // Set reminder time to today
            reminderCalendar[Calendar.YEAR] = currentCalendar[Calendar.YEAR]
            reminderCalendar[Calendar.MONTH] = currentCalendar[Calendar.MONTH]
            reminderCalendar[Calendar.DAY_OF_MONTH] = currentCalendar[Calendar.DAY_OF_MONTH]

            var diffMillis = reminderCalendar.timeInMillis - currentCalendar.timeInMillis
            val diffMinutes = abs(diffMillis) / (60 * 1000)

            // If time has passed for today, check if it's within the last DUE_NOW_THRESHOLD_MINUTES
            if (diffMillis < 0) {
                if (diffMinutes <= DUE_NOW_THRESHOLD_MINUTES) {
                    // Still show as due now if within threshold
                    setTextViewState(
                        textView,
                        context.getString(R.string.time_to_take_medicine),
                        ContextCompat.getColor(context, R.color.light_red_color)
                    )
                    return
                } else {
                    // Move to next day
                    reminderCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    diffMillis = reminderCalendar.timeInMillis - currentCalendar.timeInMillis
                }
            }

            // Within "Due Now" threshold
            if (diffMinutes <= DUE_NOW_THRESHOLD_MINUTES) {
                setTextViewState(
                    textView,
                    context.getString(R.string.time_to_take_medicine),
                    ContextCompat.getColor(context, R.color.light_red_color)
                )
            } else {
                updateTimeDisplay(context, textView, diffMillis)
            }

        } catch (e: ParseException) {
            setTextViewState(textView, context.getString(R.string.time_error), Color.GRAY)
        }
    }

    private fun updateTimeDisplay(context: Context, textView: TextView, diffMillis: Long) {
        val diffMinutes = diffMillis / (60 * 1000) // Don't use abs here as we want to show "overdue"
        val hours = abs(diffMinutes) / 60
        val minutes = abs(diffMinutes) % 60

        val timeRemaining = when {
            diffMinutes < 0 -> {
                val overdue = context.getString(R.string.overdue_by)
                when {
                    hours > 0 -> {
                        if (minutes > 0) {
                            "$overdue $hours h $minutes min"
                        } else {
                            "$overdue $hours h"
                        }
                    }
                    else -> "$overdue $minutes min"
                }
            }
            hours > 0 -> {
                if (minutes > 0) {
                    context.getString(R.string.in_x_hours_y_minutes, hours, minutes)
                } else {
                    context.getString(R.string.in_x_hours, hours, if (hours == 1L) "" else "s")
                }
            }
            minutes > 1 -> context.getString(R.string.in_x_minutes, minutes)
            minutes == 1L -> context.getString(R.string.in_one_minute)
            else -> context.getString(R.string.time_to_take_medicine)
        }

        val textColor = when {
            diffMinutes < 0 -> ContextCompat.getColor(context, R.color.overdue_color) // New color for overdue
            diffMinutes <= URGENT_THRESHOLD_MINUTES -> ContextCompat.getColor(context, R.color.urgent_color)
            else -> ContextCompat.getColor(context, R.color.normal_time_color)
        }

        setTextViewState(textView, timeRemaining, textColor)
    }

    private fun setTextViewState(textView: TextView, text: String, color: Int) {
        textView.text = text
        textView.setTextColor(color)
    }
}