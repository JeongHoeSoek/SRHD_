package com.cookandroid.myapplication.ui.notifications

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cookandroid.myapplication.R
import com.cookandroid.myapplication.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToggleButtons()
        observeViewModel()
        loadSettings()
    }

    private fun setupToggleButtons() {
        binding.apply {
            listOf(toggleButton10Sec, toggleButton30Sec, toggleButton1Min, toggleButton3Min).forEach { button ->
                button.setOnClickListener {
                    val time = button.textOn.toString()
                    viewModel.selectTime(if (button.isChecked) time else null)
                    saveSettings()
                }
            }

            listOf(toggleButtonLow, toggleButtonMedium, toggleButtonHigh).forEach { button ->
                button.setOnClickListener {
                    val level = button.textOn.toString()
                    viewModel.selectLevel(if (button.isChecked) level else null)
                    saveSettings()
                }
            }

            listOf(toggleButtonVibration, toggleButtonSound).forEach { button ->
                button.setOnClickListener {
                    val mode = button.textOn.toString()
                    viewModel.selectMode(if (button.isChecked) mode else null)
                    saveSettings()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.selectedTime.observe(viewLifecycleOwner) { updateToggleButtonsUI() }
        viewModel.selectedLevel.observe(viewLifecycleOwner) { updateToggleButtonsUI() }
        viewModel.selectedMode.observe(viewLifecycleOwner) { updateToggleButtonsUI() }
    }

    private fun updateToggleButtonsUI() {
        // 시간 설정 토글 버튼 UI 업데이트
        binding.apply {
            listOf(toggleButton10Sec, toggleButton30Sec, toggleButton1Min, toggleButton3Min).forEach { button ->
                val isSelected = viewModel.selectedTime.value == button.textOn.toString()
                button.isChecked = isSelected
                if (isSelected) {
                    button.setTextColor(Color.WHITE)
                    button.setBackgroundResource(R.drawable.alarm_chip2)
                } else {
                    button.setTextColor(Color.BLACK)
                    button.setBackgroundResource(R.drawable.alarm_chip)
                }
            }

            // 세기 설정 토글 버튼 UI 업데이트
            listOf(toggleButtonLow, toggleButtonMedium, toggleButtonHigh).forEach { button ->
                val isSelected = viewModel.selectedLevel.value == button.textOn.toString()
                button.isChecked = isSelected
                if (isSelected) {
                    button.setTextColor(Color.WHITE)
                    button.setBackgroundResource(R.drawable.alarm_chip2)
                } else {
                    button.setTextColor(Color.BLACK)
                    button.setBackgroundResource(R.drawable.alarm_chip)
                }
            }

            // 모드 설정 토글 버튼 UI 업데이트
            listOf(toggleButtonVibration, toggleButtonSound).forEach { button ->
                val isSelected = viewModel.selectedMode.value == button.textOn.toString()
                button.isChecked = isSelected
                if (isSelected) {
                    button.setTextColor(Color.WHITE)
                    button.setBackgroundResource(R.drawable.alarm_long_chip2)
                } else {
                    button.setTextColor(Color.BLACK)
                    button.setBackgroundResource(R.drawable.alarm_long_chip)
                }
            }
        }
    }

    private fun saveSettings() {
        val sharedPref = activity?.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("SelectedTime", viewModel.selectedTime.value)
            putString("SelectedLevel", viewModel.selectedLevel.value)
            putString("SelectedMode", viewModel.selectedMode.value)
            apply()
        }
    }

    private fun loadSettings() {
        val sharedPref = activity?.getSharedPreferences("AppSettings", Context.MODE_PRIVATE) ?: return
        viewModel.selectTime(sharedPref.getString("SelectedTime", null))
        viewModel.selectLevel(sharedPref.getString("SelectedLevel", null))
        viewModel.selectMode(sharedPref.getString("SelectedMode", null))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


