package com.corps.healthmate

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.corps.healthmate.repository.MedicineRepository
import com.corps.healthmate.utils.MedicineDataLoader
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HealthMateApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    @Inject
    lateinit var medicineRepository: MedicineRepository

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        
        initializeMedicineDatabase()
    }

    private fun initializeMedicineDatabase() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val isDatabaseInitialized = prefs.getBoolean("is_medicine_db_initialized", false)
                
                if (!isDatabaseInitialized) {
                    Timber.d("Starting medicine database initialization")
                    MedicineDataLoader(applicationContext).loadMedicinesIntoFirestore()
                    
                    // Verify the data was loaded
                    if (medicineRepository.verifyMedicineData()) {
                        prefs.edit().putBoolean("is_medicine_db_initialized", true).apply()
                        Timber.d("Medicine database initialized and verified successfully")
                    } else {
                        Timber.w("Medicine database initialization could not be verified")
                    }
                } else {
                    // Verify existing data
                    if (!medicineRepository.verifyMedicineData()) {
                        Timber.w("Medicine data missing despite initialization flag. Reinitializing...")
                        prefs.edit().putBoolean("is_medicine_db_initialized", false).apply()
                        initializeMedicineDatabase() // Retry initialization
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize medicine database")
            }
        }
    }
}