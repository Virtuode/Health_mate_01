package com.corps.healthmate.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.corps.healthmate.R

import com.corps.healthmate.models.Chat

class ChatAdapterPatient(private val context: Context, private val chatList: List<Chat>) :
    RecyclerView.Adapter<ChatAdapterPatient.ChatViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat_patient, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.doctorNameTextView.text = chat.doctorName
        holder.lastMessageTextView.text = chat.lastMessage
        // TODO: Load doctor's profile image using Glide or Picasso if available
        // Example using Glide:
        // Glide.with(context)
        //      .load(chat.getDoctorProfileImageUrl())
        //      .placeholder(R.drawable.ic_doctor_placeholder)
        //      .into(holder.doctorProfileImageView);
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var doctorNameTextView: TextView = itemView.findViewById(R.id.doctor_name)
        var lastMessageTextView: TextView = itemView.findViewById(R.id.last_message)
        var doctorProfileImageView: ImageView = itemView.findViewById(R.id.doctor_profile_image)
    }
}