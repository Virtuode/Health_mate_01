package com.corps.healthmate.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.corps.healthmate.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragmentPatient : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_patient, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.chat_recycler_view)
        setupRecyclerView()
        loadChats()
    }

    private fun setupRecyclerView() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            // TODO: Set adapter when implemented
        }
    }

    private fun loadChats() {
        // TODO: Load chat list from repository
    }
}