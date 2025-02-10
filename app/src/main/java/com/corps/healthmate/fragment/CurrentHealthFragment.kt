package com.corps.healthmate.fragment

import com.corps.healthmate.interfaces.SurveyDataProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.corps.healthmate.databinding.FragmentCurrentHealthBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrentHealthFragment : Fragment(), SurveyDataProvider {
    private var _binding: FragmentCurrentHealthBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null. Is the view visible?")

    // Cache for selected values
    private var cachedSymptoms: List<String>? = null
    private var cachedLifestyleHabits: List<String>? = null
    private var cachedMedication: Map<String, String>? = null
    private var cachedSleepDuration: Float? = null
    private var cachedExercise: String? = null
    private var cachedStressLevel: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChipGroups()
        setupMedicationInputs()
        setupExerciseRadioGroup()
        setupSleepDurationSlider()
        setupStressLevelRadioGroup()
        
        // Apply cached data if it exists
        applyCachedData()
    }

    private fun applyCachedData() {
        if (_binding == null) return

        cachedSymptoms?.forEach { symptom ->
            binding.symptomsChipGroup.children.forEach { chip ->
                if (chip is Chip && chip.text.toString() == symptom) {
                    chip.isChecked = true
                }
            }
        }

        cachedLifestyleHabits?.forEach { habit ->
            binding.lifestyleChipGroup.children.forEach { chip ->
                if (chip is Chip && chip.text.toString() == habit) {
                    chip.isChecked = true
                }
            }
        }

        cachedMedication?.let { med ->
            binding.medicationNameInput.setText(med["name"])
            binding.dosageInput.setText(med["dosage"])
            binding.frequencyInput.setText(med["frequency"])
        }

        cachedSleepDuration?.let {
            binding.sleepDurationSlider.value = it
        }

        cachedExercise?.let { exercise ->
            when (exercise) {
                "Daily" -> binding.radioDaily.isChecked = true
                "3-4 times a week" -> binding.radio3Times.isChecked = true
                "Once a week" -> binding.radioOnce.isChecked = true
                "Rarely" -> binding.radioRarely.isChecked = true
            }
        }

        cachedStressLevel?.let { stress ->
            when (stress) {
                "Low" -> binding.lowStress.isChecked = true
                "Medium" -> binding.mediumStress.isChecked = true
                "High" -> binding.highStress.isChecked = true
            }
        }

        // Clear cached data after applying
        clearCachedData()
    }

    private fun clearCachedData() {
        cachedSymptoms = null
        cachedLifestyleHabits = null
        cachedMedication = null
        cachedSleepDuration = null
        cachedExercise = null
        cachedStressLevel = null
    }

    private fun setupChipGroups() {
        // Make sure all chips are checkable
        binding.symptomsChipGroup.children.forEach { chip ->
            if (chip is Chip) {
                chip.isCheckable = true
            }
        }
        
        binding.lifestyleChipGroup.children.forEach { chip ->
            if (chip is Chip) {
                chip.isCheckable = true
            }
        }

        binding.symptomsChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            updateCurrentHealthData()
        }
        
        binding.lifestyleChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            updateCurrentHealthData()
        }
    }

    private fun setupMedicationInputs() {
        binding.medicationNameInput.addTextChangedListener {
            updateCurrentHealthData()
        }

        binding.dosageInput.addTextChangedListener {
            updateCurrentHealthData()
        }

        val frequencies = listOf("Once a day", "Twice a day", "Every 8 hours", "As needed")
        val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_dropdown_item_1line, frequencies)
        binding.frequencyInput.setAdapter(adapter)

        binding.frequencyInput.setOnItemClickListener { _, _, _, _ ->
            updateCurrentHealthData()
        }
    }


    private fun setupExerciseRadioGroup() {
        binding.exerciseRadioGroup.setOnCheckedChangeListener { _, _ -> updateCurrentHealthData() }
    }


    private fun setupSleepDurationSlider() {
        // Set a default value if no value is set
        if (binding.sleepDurationSlider.value == 0f) {
            binding.sleepDurationSlider.value = 6f  // Default to 6 hours
        }

        binding.sleepDurationSlider.addOnChangeListener { _, value, _ ->
            updateCurrentHealthData()
        }

        // Trigger initial update
        updateCurrentHealthData()
    }

    private fun setupStressLevelRadioGroup() {
        binding.stressLevelGroup.setOnCheckedChangeListener { _, _ ->
            updateCurrentHealthData()
        }
    }

    private fun loadExistingData() {
        // Implement loading logic for existing data
    }

    private fun updateCurrentHealthData() {
        // Implement update logic for current health data
    }

    // Public methods for validation in SurveyScreen
    fun areSymptomsSelected(): Boolean =
        binding.symptomsChipGroup.checkedChipIds.isNotEmpty()

    fun isStressLevelSelected(): Boolean =
        binding.stressLevelGroup.checkedRadioButtonId != -1

    fun getSelectedStressLevel(): String =
        when {
            binding.lowStress.isChecked -> "Low"
            binding.mediumStress.isChecked -> "Medium"
            binding.highStress.isChecked -> "High"
            else -> ""
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getSurveyData(): Map<String, Any?> {
        val selectedSymptoms = binding.symptomsChipGroup.checkedChipIds
            .mapNotNull { id -> 
                (binding.symptomsChipGroup.findViewById<Chip>(id)).text.toString() 
            }

        val medications = if (binding.medicationNameInput.text.toString().isNotEmpty()) {
            listOf(
                mapOf(
                    "name" to binding.medicationNameInput.text.toString(),
                    "dosage" to binding.dosageInput.text.toString(),
                    "frequency" to binding.frequencyInput.text.toString()
                )
            )
        } else emptyList()

        return mapOf(
            "currentHealth" to mapOf(
                "symptoms" to selectedSymptoms,
                "medications" to medications,
                "lifestyleHabits" to getLifestyleHabits(),
                "sleepDuration" to binding.sleepDurationSlider.value,
                "exercise" to getExerciseFrequency(),
                "stressLevel" to getSelectedStressLevel()
            )
        )
    }

    override fun isDataValid(): Boolean {
        return areSymptomsSelected() && 
               isStressLevelSelected() &&
               isMedicationValid() &&
               isExerciseSelected()
    }

    override fun loadExistingData(data: Map<String, Any?>) {
        val currentHealth = data["currentHealth"] as? Map<*, *> ?: return

        if (_binding == null) {
            // Cache the data for later
            cachedSymptoms = (currentHealth["symptoms"] as? List<*>)?.mapNotNull { it?.toString() }
            cachedLifestyleHabits = (currentHealth["lifestyleHabits"] as? List<*>)?.mapNotNull { it?.toString() }
            
            (currentHealth["medications"] as? List<*>)?.firstOrNull()?.let { med ->
                if (med is Map<*, *>) {
                    cachedMedication = mapOf(
                        "name" to (med["name"]?.toString() ?: ""),
                        "dosage" to (med["dosage"]?.toString() ?: ""),
                        "frequency" to (med["frequency"]?.toString() ?: "")
                    )
                }
            }
            
            cachedSleepDuration = (currentHealth["sleepDuration"] as? Number)?.toFloat()
            cachedExercise = currentHealth["exercise"] as? String
            cachedStressLevel = currentHealth["stressLevel"] as? String
            return
        }

        // If binding is available, apply the data directly
        (currentHealth["symptoms"] as? List<*>)?.forEach { symptom ->
            val symptomText = symptom.toString()
            binding.symptomsChipGroup.children.forEach { chip ->
                if (chip is Chip && chip.text.toString() == symptomText) {
                    chip.isChecked = true
                }
            }
        }

        // Apply other data...
        // ... rest of the existing loadExistingData implementation ...
    }

    private fun getLifestyleHabits(): List<String> {
        return binding.lifestyleChipGroup.checkedChipIds.mapNotNull { id ->
            (binding.lifestyleChipGroup.findViewById<Chip>(id)).text.toString()
        }
    }

    private fun getExerciseFrequency(): String {
        return when {
            binding.radioDaily.isChecked -> "Daily"
            binding.radio3Times.isChecked -> "3-4 times a week"
            binding.radioOnce.isChecked -> "Once a week"
            binding.radioRarely.isChecked -> "Rarely"
            else -> "Not Selected"
        }
    }

    private fun isMedicationValid(): Boolean {
        // If medication name is filled, check other fields
        return if (binding.medicationNameInput.text.toString().isNotEmpty()) {
            binding.dosageInput.text.toString().isNotEmpty() &&
            binding.frequencyInput.text.toString().isNotEmpty()
        } else true // No medication is valid
    }

    private fun isExerciseSelected(): Boolean {
        return binding.exerciseRadioGroup.checkedRadioButtonId != -1
    }

    companion object {
        fun newInstance() = CurrentHealthFragment()
    }
}