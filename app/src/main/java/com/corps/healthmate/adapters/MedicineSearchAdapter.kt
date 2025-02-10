package com.corps.healthmate.adapters

import com.corps.healthmate.models.Medicine
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.corps.healthmate.databinding.ItemMedicineSearchBinding


class MedicineSearchAdapter(
    private val onMedicineClick: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicineSearchAdapter.MedicineViewHolder>(MedicineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MedicineViewHolder(
        private val binding: ItemMedicineSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMedicineClick(getItem(position))
                }
            }
        }

        fun bind(medicine: Medicine) {
            binding.medicineName.text = medicine.name
            binding.medicineDescription.text = medicine.description
        }
    }

    private class MedicineDiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem == newItem
    }
} 