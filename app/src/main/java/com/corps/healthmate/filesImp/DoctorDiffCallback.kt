package com.corps.healthmate.filesImp

import androidx.recyclerview.widget.DiffUtil
import com.corps.healthmate.models.DoctorSummary

class DoctorDiffCallback(
    private val oldList: List<DoctorSummary>,
    private val newList: List<DoctorSummary>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id // Assuming 'id' uniquely identifies each doctor
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
