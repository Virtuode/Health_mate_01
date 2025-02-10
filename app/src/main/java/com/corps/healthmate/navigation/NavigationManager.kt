package com.corps.healthmate.navigation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.corps.healthmate.activities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

object NavigationManager {
    private const val PREFS_NAME = "HealthmatePrefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboardingCompleted"
//    private const val KEY_SURVEY_COMPLETED = "surveyCompleted"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }



    fun handleLoginSuccess(activity: AppCompatActivity) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            // Ensure onboarding is marked as completed for logged-in users
            getPrefs(activity).edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
            checkUserStatus(activity, currentUser.uid)
        } else {
            navigateToWelcomeScreen(activity)
        }
    }

    fun handleRegistrationSuccess(activity: AppCompatActivity) {
        navigateToSurvey(activity)
    }


    fun handleSplashScreenNavigation(activity: AppCompatActivity) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null && currentUser.isEmailVerified) {
            checkUserStatus(activity, currentUser.uid)
        } else {
            navigateToWelcomeScreen(activity)
        }
    }




    private fun isSurveyDataComplete(snapshot: DataSnapshot): Boolean {
        // Check if survey data exists
        if (!snapshot.exists()) return false

        // Check basic information
        val basicInfo = snapshot.child("basicInfo")
        if (!basicInfo.exists()) return false

        val requiredBasicFields = listOf(
            "firstName", "lastName", "age", "gender",
            "contactNumber", "height", "weight"
        )

        for (field in requiredBasicFields) {
            val value = basicInfo.child(field).value
            when (field) {
                "age" -> if (((value as? Number)?.toInt() ?: 0) <= 0) return false
                "height", "weight" -> if (((value as? Number)?.toFloat() ?: 0f) <= 0f) return false
                else -> if (value.toString().isBlank()) return false
            }
        }

        // Check blood group
        val bloodGroup = snapshot.child("bloodGroup")
        if (!bloodGroup.exists()) return false
        if (bloodGroup.child("bloodGroup").value.toString().isBlank() ||
            bloodGroup.child("rhFactor").value.toString().isBlank()) return false

        // Check current health
        val currentHealth = snapshot.child("currentHealth")
        if (!currentHealth.exists()) return false
        val symptoms = currentHealth.child("symptoms").getValue(object : GenericTypeIndicator<List<String>>() {})
        if (symptoms.isNullOrEmpty()) return false

        // Check emergency contact with correct field names
        val emergencyContact = snapshot.child("emergencyContact")
        if (!emergencyContact.exists()) return false
        val requiredContactFields = listOf("name", "relation", "phone")  // Changed from phoneNumber to phone
        for (field in requiredContactFields) {
            if (emergencyContact.child(field).value.toString().isBlank()) return false
        }

        // Check medical history
        val medicalHistory = snapshot.child("medicalHistory")
        if (!medicalHistory.exists()) return false
        val chronicConditions = medicalHistory.child("chronicConditions")
            .getValue(object : GenericTypeIndicator<List<String>>() {})
        return !chronicConditions.isNullOrEmpty()
    }





    private fun checkUserStatus(activity: AppCompatActivity, userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.reference.child("patients").child(userId)
        
        // Single listener for both survey and user data
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val surveySnapshot = snapshot.child("survey")
                    val isSurveyComplete = isSurveyDataComplete(surveySnapshot)
                    
                    // Log survey data for debugging
                    Timber.d("NavigationManager", "Survey Data: ${surveySnapshot.value}")
                    Timber.d("NavigationManager", "Survey Complete: $isSurveyComplete")

                    if (!snapshot.exists()) {
                        // Create new user data if it doesn't exist
                        val user = FirebaseAuth.getInstance().currentUser
                        val userData = hashMapOf(
                            "email" to (user?.email ?: ""),
                            "createdAt" to System.currentTimeMillis(),
                            "surveyCompleted" to isSurveyComplete,
                            "emailVerified" to user?.isEmailVerified
                        )
                        userRef.setValue(userData)
                            .addOnSuccessListener {
                                navigateBasedOnSurveyStatus(activity, isSurveyComplete)
                            }
                            .addOnFailureListener { error ->
                                Timber.e("NavigationManager", "Failed to create user: ${error.message}")
                                navigateToSurvey(activity)
                            }
                    } else {
                        // Update survey completion status and navigate
                        userRef.child("surveyCompleted")
                            .setValue(isSurveyComplete)
                            .addOnSuccessListener {
                                navigateBasedOnSurveyStatus(activity, isSurveyComplete)
                            }
                            .addOnFailureListener { error ->
                                Timber.e("NavigationManager", "Failed to update survey status: ${error.message}")
                                navigateToSurvey(activity)
                            }
                    }
                } catch (e: Exception) {
                    Timber.e("NavigationManager", "Error processing user data: ${e.message}")
                    navigateToSurvey(activity)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("NavigationManager", "Database error: ${error.message}")
                navigateToSurvey(activity)
            }
        })
    }

    private fun navigateBasedOnSurveyStatus(activity: AppCompatActivity, isSurveyComplete: Boolean) {
        if (isSurveyComplete) {
            navigateToMain(activity)
        } else {
            navigateToSurvey(activity)
        }
    }

//    private fun navigateToOnboarding(activity: AppCompatActivity) {
//        val intent = Intent(activity, OnBoardActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        activity.startActivity(intent)
//        activity.finish()
//    }

    private fun navigateToWelcomeScreen(activity: AppCompatActivity) {
        val intent = Intent(activity, WelcomeScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    private fun navigateToSurvey(activity: AppCompatActivity) {
        val intent = Intent(activity, SurveyScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }

    private fun navigateToMain(activity: AppCompatActivity) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}
