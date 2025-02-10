package com.corps.healthmate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedProfileViewModel : ViewModel() {
    private val _profileImageUrl = MutableLiveData<String>()
    val profileImageUrl: LiveData<String> = _profileImageUrl

    fun updateProfileImage(url: String) {
        _profileImageUrl.value = url
    }
} 