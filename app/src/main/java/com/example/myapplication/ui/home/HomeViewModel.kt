package com.cookandroid.myapplication.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _isImageOn = MutableLiveData<Boolean>().apply { value = false }
    val isImageOn: LiveData<Boolean> = _isImageOn

    fun toggleImageState() {
        _isImageOn.value = _isImageOn.value != true
    }
}
