package com.corps.healthmate.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.corps.healthmate.R
import com.corps.healthmate.data.ProfileData
import com.corps.healthmate.databinding.ActivityProfileEditBinding
import com.corps.healthmate.utils.CloudinaryHelper
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    private var currentProfileData: ProfileData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupGenderDropdown()
        setupSaveButton()
        loadExistingData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, genders)
        binding.genderDropdown.setAdapter(adapter)
    }

    private fun loadExistingData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().reference
            .child("patients")
            .child(userId)
            .child("survey")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        currentProfileData = snapshot.getValue(ProfileData::class.java)
                        currentProfileData?.let { populateFields(it) }
                    } catch (e: Exception) {
                        showErrorMessage("Failed to load profile data")
                    } finally {
                        binding.progressBar.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    showErrorMessage("Failed to load profile data")
                }
            })
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                updateProfile()
            }
        }
    }

    private fun updateProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.saveButton.isEnabled = false

        val updatedData = currentProfileData?.copy(
            basicInfo = currentProfileData?.basicInfo?.copy(
                firstName = binding.firstNameInput.text?.toString() ?: "",
                lastName = binding.lastNameInput.text?.toString() ?: "",
                age = binding.ageInput.text?.toString()?.toIntOrNull(),
                gender = binding.genderDropdown.text?.toString() ?: "",
                contactNumber = binding.phoneInput.text?.toString() ?: "",
                height = binding.heightSlider.value,
                weight = binding.weightSlider.value
            ),
            bloodGroup = currentProfileData?.bloodGroup?.copy(
                bloodGroup = getSelectedBloodGroup(),
                rhFactor = getRhFactor()
            ),
            medicalHistory = currentProfileData?.medicalHistory?.copy(
                chronicConditions = getSelectedMedicalConditions()
            ),
            emergencyContact = currentProfileData?.emergencyContact?.copy(
                name = binding.emergencyNameInput.text?.toString() ?: "",
                relation = binding.emergencyRelationInput.text?.toString() ?: "",
                phone = binding.emergencyPhoneInput.text?.toString() ?: ""
            )
        ) ?: ProfileData()

        FirebaseDatabase.getInstance().reference
            .child("patients")
            .child(userId)
            .child("survey")
            .setValue(updatedData)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.saveButton.isEnabled = true
                showSuccessMessage()
                finish()
            }
            .addOnFailureListener { error ->
                binding.progressBar.visibility = View.GONE
                binding.saveButton.isEnabled = true
                showErrorMessage(error.message ?: "Failed to update profile")
            }
    }

    private fun populateFields(profileData: ProfileData) {
        // Basic Info
        profileData.basicInfo?.let { info ->
            binding.apply {
                firstNameInput.setText(info.firstName)
                lastNameInput.setText(info.lastName)
                ageInput.setText(info.age?.toString())
                genderDropdown.setText(info.gender)
                phoneInput.setText(info.contactNumber)
                heightSlider.value = info.height ?: 170f
                weightSlider.value = info.weight ?: 70f
            }
        }

        // Blood Group
        profileData.bloodGroup?.let { blood ->
            val chipId = when (blood.bloodGroup) {
                "A+" -> R.id.chipAPositive
                "B+" -> R.id.chipBPositive
                "O+" -> R.id.chipOPositive
                "AB+" -> R.id.chipABPositive
                "A-" -> R.id.chipANegative
                "B-" -> R.id.chipBNegative
                "O-" -> R.id.chipONegative
                "AB-" -> R.id.chipABNegative
                else -> null
            }
            chipId?.let { binding.bloodGroupChipGroup.check(it) }
        }

        // Medical History
        profileData.medicalHistory?.let { history ->
            history.chronicConditions.forEach { condition ->
                when (condition) {
                    "Diabetes" -> binding.chipDiabetes.isChecked = true
                    "Hypertension" -> binding.chipHypertension.isChecked = true
                }
            }
        }

        // Emergency Contact
        profileData.emergencyContact?.let { contact ->
            binding.apply {
                emergencyNameInput.setText(contact.name)
                emergencyRelationInput.setText(contact.relation)
                emergencyPhoneInput.setText(contact.phone)
            }
        }
    }

    private fun getSelectedBloodGroup(): String {
        return when (binding.bloodGroupChipGroup.checkedChipId) {
            R.id.chipAPositive -> "A+"
            R.id.chipBPositive -> "B+"
            R.id.chipOPositive -> "O+"
            R.id.chipABPositive -> "AB+"
            R.id.chipANegative -> "A-"
            R.id.chipBNegative -> "B-"
            R.id.chipONegative -> "O-"
            R.id.chipABNegative -> "AB-"
            else -> ""
        }
    }

    private fun getRhFactor(): String {
        return if (getSelectedBloodGroup().endsWith("+")) "Positive" else "Negative"
    }

    private fun getSelectedMedicalConditions(): List<String> {
        return mutableListOf<String>().apply {
            if (binding.chipDiabetes.isChecked) add("Diabetes")
            if (binding.chipHypertension.isChecked) add("Hypertension")
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.firstNameInput.text.isNullOrBlank()) {
            binding.firstNameLayout.error = "First name is required"
            isValid = false
        }

        if (binding.emergencyNameInput.text.isNullOrBlank()) {
            binding.emergencyNameLayout.error = "Emergency contact name is required"
            isValid = false
        }

        if (binding.emergencyPhoneInput.text.isNullOrBlank()) {
            binding.emergencyPhoneLayout.error = "Emergency contact phone is required"
            isValid = false
        }

        return isValid
    }

    private fun showSuccessMessage() {
        Snackbar.make(binding.root, "Profile updated successfully", Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(error: String) {
        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
            .setAction("Retry") { binding.saveButton.performClick() }
            .show()
    }
}



