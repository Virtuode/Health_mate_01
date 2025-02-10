package com.corps.healthmate.repository

import com.corps.healthmate.models.Medicine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val medicineCollection = firestore.collection("medicines")

    suspend fun searchMedicines(query: String): Flow<List<Medicine>> = flow {
        try {
            val snapshot = medicineCollection
                .orderBy("name")
                .whereGreaterThanOrEqualTo("name", query.lowercase())
                .whereLessThanOrEqualTo("name", query.lowercase() + '\uf8ff')
                .limit(20)
                .get()
                .await()
            
            val medicines = snapshot.toObjects(Medicine::class.java)
            Timber.d("Found ${medicines.size} medicines matching '$query'")
            emit(medicines)
        } catch (e: Exception) {
            Timber.e(e, "Error searching medicines: ${e.message}")
            emit(emptyList())
        }
    }

    suspend fun getInitialMedicines(limit: Int = 20): Flow<List<Medicine>> = flow {
        try {
            val snapshot = medicineCollection
                .limit(limit.toLong())
                .get()
                .await()
            
            emit(snapshot.toObjects(Medicine::class.java))
        } catch (e: Exception) {
            Timber.e(e, "Error getting initial medicines")
            emit(emptyList())
        }
    }

    suspend fun getMedicineById(id: String): Medicine? {
        return try {
            medicineCollection.document(id).get().await().toObject(Medicine::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error getting medicine by id")
            null
        }
    }

    suspend fun verifyMedicineData(): Boolean {
        return try {
            val count = medicineCollection
                .get()
                .await()
                .size()
            
            Timber.d("Found $count medicines in Firestore")
            count > 0
        } catch (e: Exception) {
            Timber.e(e, "Error verifying medicine data")
            false
        }
    }
} 