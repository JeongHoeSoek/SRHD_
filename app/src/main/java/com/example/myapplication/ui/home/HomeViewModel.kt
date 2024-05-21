package com.cookandroid.myapplication.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _isImageOn = MutableLiveData<Boolean>()
    val isImageOn: LiveData<Boolean> = _isImageOn

    init {
        _isImageOn.value = loadImageOnState()
    }

    fun toggleImageState() {
        val newState = !_isImageOn.value!!
        _isImageOn.value = newState
        saveImageOnState(newState)
    }

    fun setImageOnState(isOn: Boolean) {
        _isImageOn.postValue(isOn)
        saveImageOnState(isOn)
    }

    private fun saveImageOnState(isOn: Boolean) {
        val sharedPref = getApplication<Application>().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("IS_IMAGE_ON", isOn)
            apply()
        }
    }

    fun loadImageOnState(): Boolean {
        val sharedPref = getApplication<Application>().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("IS_IMAGE_ON", false)
    }
}

