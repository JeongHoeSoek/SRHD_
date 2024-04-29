package com.cookandroid.myapplication.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {
    // 시간, 세기, 모드의 선택 상태를 저장하는 LiveData
    private val _selectedTime = MutableLiveData<String?>()
    val selectedTime: LiveData<String?> = _selectedTime

    private val _selectedLevel = MutableLiveData<String?>()
    val selectedLevel: LiveData<String?> = _selectedLevel

    private val _selectedMode = MutableLiveData<String?>()
    val selectedMode: LiveData<String?> = _selectedMode

    // 사용자 선택을 업데이트하는 함수
    fun selectTime(time: String?) {
        _selectedTime.value = time
    }

    fun selectLevel(level: String?) {
        _selectedLevel.value = level
    }

    fun selectMode(mode: String?) {
        _selectedMode.value = mode
    }
}
