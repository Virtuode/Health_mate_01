package com.corps.healthmate.adapters

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.corps.healthmate.R
import com.corps.healthmate.database.Reminder
import com.corps.healthmate.filesImp.ReminderDiffCallback
import com.corps.healthmate.interfaces.TimeDifferenceCallback
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import timber.log.Timber

class ReminderAdapter(
    private val listener: OnReminderClickListener,  // Use the interface here
    private val callback: TimeDifferenceCallback  // Callback for calculating time remaining
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private var reminders: List<Reminder> = emptyList()

    fun updateReminders(newReminders: List<Reminder>) {
        val diffCallback = ReminderDiffCallback(reminders, newReminders)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        this.reminders = newReminders
        diffResult.dispatchUpdatesTo(this)
    }

    // Interface for handling clicks on delete and deactivate buttons
    interface OnReminderClickListener {
        fun onDeleteClick(reminder: Reminder?)  // Method to handle delete button clicks
        fun onDeactivateClick(reminder: Reminder?)  // Method to handle deactivate button clicks
    }

    // Create a new view for each reminder item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    // Bind data to the view holder for each reminder item
    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        try {
            holder.bind(reminders[position])
        } catch (e: Exception) {
            Timber.e(e, "Error binding reminder at position $position")
        }
    }

    // Return the total number of items in the list
    override fun getItemCount(): Int = reminders.size

    // ViewHolder for each reminder item in the RecyclerView
    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reminderTime: TextView = itemView.findViewById(R.id.reminderTime)
        private val reminderDay: TextView = itemView.findViewById(R.id.reminderDay)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        private val pillNamesChipGroup: ChipGroup = itemView.findViewById(R.id.pillNamesChipGroup)
        private val timeRemainingText: TextView = itemView.findViewById(R.id.timeRemainingText)

        fun bind(reminder: Reminder) {
            try {
                reminderTime.text = reminder.time
                reminderDay.text = reminder.day

                pillNamesChipGroup.removeAllViews()
                
                reminder.pillNames.forEach { pillName ->
                    addPillChip(pillName, reminder.dosage)
                }

                callback.onCalculateTimeDifference(timeRemainingText, reminder.time)
                
                setupButtons(reminder)
                
            } catch (e: Exception) {
                Timber.e(e, "Error binding reminder view holder")
            }
        }

        private fun addPillChip(pillName: String, dosage: String?) {
            val chip = Chip(itemView.context).apply {
                text = buildString {
                    append(pillName)
                    if (!dosage.isNullOrBlank()) {
                        append(" (")
                        append(dosage)
                        append(")")
                    }
                }
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.default_chip_background)
                setTextColor(ContextCompat.getColor(context, R.color.white))
                chipStrokeWidth = 0f
                chipCornerRadius = resources.getDimension(R.dimen.chip_corner_radius)
            }
            pillNamesChipGroup.addView(chip)
        }

        private fun setupButtons(reminder: Reminder) {


            deleteButton.setOnClickListener {
                animateButton(deleteButton) {
                    listener.onDeleteClick(reminder)
                }
            }


        }

        private fun animateButton(button: View, onEnd: () -> Unit) {
            ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.8f, 1f).apply {
                duration = 200
                doOnEnd { onEnd() }
                start()
            }
        }
    }

    companion object {
        private const val TAG = "ReminderAdapter"
    }
}