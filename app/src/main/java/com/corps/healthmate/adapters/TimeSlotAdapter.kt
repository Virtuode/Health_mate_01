package com.corps.healthmate.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.corps.healthmate.R
import com.corps.healthmate.adapters.TimeSlotAdapter.TimeSlotViewHolder
import com.corps.healthmate.models.TimeSlot
import com.google.android.material.card.MaterialCardView

class TimeSlotAdapter(
    private val timeSlots: List<TimeSlot>,
    private val context: Context,
    private val listener: TimeSlotClickListener
) : RecyclerView.Adapter<TimeSlotViewHolder>() {
    private var selectedPosition = -1

    interface TimeSlotClickListener {
        fun onTimeSlotClick(timeSlot: TimeSlot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.timeSlotText.text = timeSlot.displayTime
        holder.slotsAvailable.text = context.getString(R.string.slots_left, timeSlot.availableSlots)

        // Update card appearance based on selection and availability
        holder.itemView.isSelected = position == selectedPosition
        holder.itemView.isEnabled = timeSlot.isAvailable

        updateCardAppearance(holder, timeSlot, position == selectedPosition)
    }

    private fun updateCardAppearance(
        holder: TimeSlotViewHolder,
        timeSlot: TimeSlot,
        isSelected: Boolean
    ) {
        val cardView = holder.itemView as MaterialCardView
        val context = holder.itemView.context

        if (!timeSlot.isAvailable) {
            // Slot is full
            cardView.setCardBackgroundColor(context.getColor(R.color.grey_200))
            holder.timeSlotText.setTextColor(context.getColor(R.color.grey_501))
            holder.slotsAvailable.setTextColor(context.getColor(R.color.grey_501))
        } else if (isSelected) {
            // Slot is selected
            cardView.setCardBackgroundColor(context.getColor(R.color.colorPrimaryss))
            holder.timeSlotText.setTextColor(context.getColor(R.color.white))
            holder.slotsAvailable.setTextColor(context.getColor(R.color.white))
        } else {
            // Available slot
            cardView.setCardBackgroundColor(context.getColor(R.color.white))
            holder.timeSlotText.setTextColor(context.getColor(R.color.colorPrimaryss))
            holder.slotsAvailable.setTextColor(context.getColor(R.color.colorPrimaryss))
        }
    }

    override fun getItemCount(): Int {
        return timeSlots.size
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeSlotText: TextView = itemView.findViewById(R.id.time_slot_text)
        var slotsAvailable: TextView = itemView.findViewById(R.id.slots_available)

        init {
            itemView.setOnClickListener {
                // Use bindingAdapterPosition instead of adapterPosition
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val timeSlot = timeSlots[bindingAdapterPosition]
                    if (timeSlot.isAvailable) {
                        val previousSelected = selectedPosition
                        selectedPosition = bindingAdapterPosition
                        notifyItemChanged(previousSelected)
                        notifyItemChanged(selectedPosition)
                        listener.onTimeSlotClick(timeSlot)
                    }
                }
            }
        }
    }
}