package com.corps.healthmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corps.healthmate.models.Medicine
import com.corps.healthmate.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class MedicineSearchViewModel @Inject constructor(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _medicineCount = MutableStateFlow(0)
    val medicineCount: StateFlow<Int> = _medicineCount

    fun loadInitialMedicines() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getInitialMedicines().collect {
                    _medicines.value = it
                }
            } catch (e: Exception) {
                _error.value = "Failed to load medicines"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMedicines(query: String) {
        if (query.length < 2) {
            loadInitialMedicines()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.searchMedicines(query.lowercase()).collect {
                    _medicines.value = it
                    Timber.d("Search results for '$query': ${it.size} medicines found")
                }
            } catch (e: Exception) {
                _error.value = "Search failed"
                Timber.e(e, "Search failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyMedicineCount() {
        viewModelScope.launch {
            try {
                repository.getInitialMedicines(Int.MAX_VALUE).collect { medicines ->
                    _medicineCount.value = medicines.size
                    Timber.d("Total medicines in Firestore: ${medicines.size}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error verifying medicine count")
            }
        }
    }
} 