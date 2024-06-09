package com.cookandroid.myapplication.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.widget.Toast
import com.cookandroid.myapplication.AlarmReceiver
import com.cookandroid.myapplication.R
import com.cookandroid.myapplication.databinding.FragmentHomeBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.cookandroid.myapplication.SSHManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var previousIsImageOn: Boolean = false

    private val homeViewModel: HomeViewModel by viewModels()
    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm() {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        //val alarmTimeAtUTC = System.currentTimeMillis()
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        context?.sendBroadcast(intent)
        Log.d("HomeFragment", "setAlarm ready")
    }

    private val updateStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getBooleanExtra("state", false)?.let { state ->
                Log.d("HomeFragment", "Broadcast received with state: $state")
                homeViewModel.setImageOnState(state)
            }
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
        Log.d("버튼 업데이트", isImageOn.toString())
        val sharedPref = context?.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        if (isImageOn) {
            // 앱 기능 ON 시 코드
            binding.imageView.setImageResource(R.drawable.main_icon_on)
            binding.buttonToggle.setImageResource(R.drawable.main_on_button)

            val isAlarmRunning = sharedPref?.getBoolean("isAlarmRunning", false)
            if (!isAlarmRunning!!) {
                setAlarm() // 알람 설정
            } else {
                Log.d("HomeFragment", "Alarm is already running")
            }

        } else {
            // 앱 기능 OFF 시 코드
            binding.imageView.setImageResource(R.drawable.main_icon_off)
            binding.buttonToggle.setImageResource(R.drawable.main_off_button)
            if (previousIsImageOn) {
                cancelAlarm() // 알람 취소
            }
        }
        previousIsImageOn = isImageOn
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
        CoroutineScope(Dispatchers.IO).launch {
            SSHManager.writeToServer("0")
        }
        stopAlarm(context!!)
    }

    private fun stopAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "STOP_ALARM"
        context.sendBroadcast(intent)
        Log.d("HomeFragment StopAlarm", intent.action.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        context?.registerReceiver(updateStateReceiver, IntentFilter("com.cookandroid.myapplication.ACTION_UPDATE_STATE"), RECEIVER_EXPORTED)
        // 상태를 다시 로드하여 UI를 업데이트
        homeViewModel.setImageOnState(homeViewModel.loadImageOnState())
        //Log.d("백그라운드에서 복귀", homeViewModel.loadImageOnState().toString())
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(updateStateReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
