package com.cookandroid.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cookandroid.myapplication.AlarmReceiver
import com.cookandroid.myapplication.R
import com.cookandroid.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private fun setAlarm() {
        // 권한 상태를 확인하고 필요한 경우 권한을 요청하는 로직
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmPermissionIntent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val hasPermission = alarmManager.canScheduleExactAlarms()
            if (!hasPermission) {
                startActivity(alarmPermissionIntent)
            } else {
                // 알람 시간 설정
                val alarmTimeAtUTC = System.currentTimeMillis() + 30 * 1000 // 30초 후
                // 알람 설정
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent)
            }
        } else {
            // Android 11 이하 버전에서는 이전 로직 그대로 실행
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmTimeAtUTC = System.currentTimeMillis() + 30 * 1000 // 30초 후
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.isImageOn.observe(viewLifecycleOwner) { isOn ->
            updateButtonState(isOn)
        }

        binding.buttonToggle.setOnClickListener {
            homeViewModel.toggleImageState()
        }
    }

    private fun updateButtonState(isImageOn: Boolean) {
        if (isImageOn) {
            // 앱 기능 ON 시 코드
            binding.imageView.setImageResource(R.drawable.main_icon_on)
            binding.buttonToggle.setImageResource(R.drawable.main_on_button)
            setAlarm() // 알람 설정
        } else {
            // 앱 기능 OFF 시 코드
            binding.imageView.setImageResource(R.drawable.main_icon_off)
            binding.buttonToggle.setImageResource(R.drawable.main_off_button)
            cancelAlarm() // 알람 취소
        }
    }

    private fun cancelAlarm() {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        // FLAG_NO_CREATE와 FLAG_IMMUTABLE 추가
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
