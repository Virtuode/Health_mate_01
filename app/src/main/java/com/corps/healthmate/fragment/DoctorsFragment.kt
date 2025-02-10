package com.corps.healthmate.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.corps.healthmate.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DoctorsFragment : Fragment() {
    private lateinit var searchBar: EditText
    private lateinit var filterButton: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doctor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        searchBar = view.findViewById(R.id.search_bar)
        filterButton = view.findViewById(R.id.filter_button)
        chipGroup = view.findViewById(R.id.chip_group_specialization)
        recyclerView = view.findViewById(R.id.doctors_recycler_view)

        setupRecyclerView()
        setupSearchBar()
        setupChipGroup()
        setupFilterButton()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        // TODO: Set adapter when implemented
    }

    private fun setupSearchBar() {
        searchBar.setOnEditorActionListener { _, _, _ ->
            performSearch(searchBar.text.toString())
            true
        }
    }

    private fun setupChipGroup() {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedSpecialties = checkedIds.map { id ->
                (group.findViewById<Chip>(id)).text.toString()
            }
            filterBySpecialties(selectedSpecialties)
        }
    }

    private fun setupFilterButton() {
        filterButton.setOnClickListener {
            // TODO: Show filter dialog or expand filter options
        }
    }

    private fun performSearch(query: String) {
        // TODO: Implement search functionality
    }

    private fun filterBySpecialties(specialties: List<String>) {
        // TODO: Implement specialty filtering
    }
}