package com.corps.healthmate.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.corps.healthmate.R
import com.corps.healthmate.filesImp.DoctorDiffCallback
import com.corps.healthmate.models.DoctorSummary
import de.hdodenhof.circleimageview.CircleImageView

class DoctorAdapter(
    private val context: Context,
    private var doctorList: List<DoctorSummary>,
    private val onDoctorClickListener: OnDoctorClickListener
) : RecyclerView.Adapter<DoctorAdapter.ViewHolder>() {


    fun updateDoctorList(updatedList: List<DoctorSummary>) {
        val diffCallback = DoctorDiffCallback(doctorList, updatedList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        doctorList = updatedList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.doctor_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doctor = doctorList[position]
        holder.nameTextView.text = doctor.name
        holder.specializationTextView.text = doctor.specialization
        val experience = doctor.experience?.toIntOrNull() ?: 0  // Use 0 as default if null or invalid
        holder.experienceTextView.text = context.getString(R.string.experience_text, experience)
        holder.tvEducation.text = context.getString(R.string.qualification_text, doctor.education)

        // Load image using Glide or Picasso
        Glide.with(context)
            .load(doctor.imageUrl)
            .placeholder(R.drawable.user)
            .into(holder.imageView)

        if ("Verified".equals(doctor.verificationStatus, ignoreCase = true)) {
            holder.verificationStatusImageView.setImageResource(R.drawable.approved)
        }

        holder.itemView.setOnClickListener { onDoctorClickListener.onDoctorClick(doctor) }
    }

    override fun getItemCount(): Int {
        return doctorList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTextView: TextView = itemView.findViewById(R.id.tvDoctorName)
        var specializationTextView: TextView = itemView.findViewById(R.id.tvSpecialization)
        var experienceTextView: TextView = itemView.findViewById(R.id.tvExperience)
        var tvEducation: TextView = itemView.findViewById(R.id.tveducation)
        var verificationStatusImageView: ImageView = itemView.findViewById(R.id.imgVerified)
        var imageView: CircleImageView = itemView.findViewById(R.id.imgDoctor)
    }

    interface OnDoctorClickListener {
        fun onDoctorClick(doctor: DoctorSummary?)
    }
}
