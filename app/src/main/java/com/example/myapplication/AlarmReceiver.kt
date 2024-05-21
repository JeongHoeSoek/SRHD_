package com.cookandroid.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import com.cookandroid.myapplication.ui.home.HomeFragment

class AlarmReceiver : BroadcastReceiver() {

    //private var ringtone: Ringtone? = null
    //private var vibrator: Vibrator? = null
    companion object {
        private var ringtone: Ringtone? = null
        private var vibrator: Vibrator? = null
        private val handler = Handler(Looper.getMainLooper())
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "STOP_ALARM") {
            Log.d("AlarmReceiver stopalarm", intent.action.toString())
            stopAlarm(context)
            return
        }
        // 알람이 울릴 때 실행할 코드
        Toast.makeText(context, "알람 울림!", Toast.LENGTH_LONG).show()
        // 추가적인 작업 수행 (예: 알림 표시)
        /*val builder = AlertDialog.Builder(context).apply {
            setTitle("제목을 정하는 건 항상 어려워")
            setMessage("내용 음슴!")
            setPositiveButton("넹", DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(this.context, "넹!", Toast.LENGTH_SHORT).show()
            })
            setNegativeButton("아니용", DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(this.context, "아니용!", Toast.LENGTH_SHORT).show()
            })
            setNeutralButton("몰?루", DialogInterface.OnClickListener { dialog, which ->
                Toast.makeText(this.context, "몰?루!", Toast.LENGTH_SHORT).show()
            })
            create()
            show()
        }*/
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isAlarmRunning", true)
            apply()
        }
        // 시간 설정 가져오기
        val selectedTime = sharedPref.getString("SelectedTime", "30초")
        Log.d("AlarmReceiver", "Selected Time: $selectedTime")

        // 세기 설정 가져오기
        val selectedLevel = sharedPref.getString("SelectedLevel", "보통")
        Log.d("AlarmReceiver", "Selected Level: $selectedLevel")

        // 모드 설정 가져오기
        val selectedMode = sharedPref.getString("SelectedMode", "소리")
        Log.d("AlarmReceiver", "Selected Mode: $selectedMode")

        // 알람 소리나 진동 설정
        when (selectedMode) {
            "소리" -> playAlarmSound(context, selectedLevel)
            "진동" -> startVibration(context, selectedLevel)
            else -> {
                // 기본 행동
                playAlarmSound(context, selectedLevel)
            }
        }
        // 일정 시간 후 알람 종료
        //val handler = Handler(Looper.getMainLooper())
        val alarmDuration = when (selectedTime) {
            "10초" -> 10000L
            "30초" -> 30000L
            "1분" -> 60000L
            "3분" -> 180000L
            else -> 30000L
        }
        handler.postDelayed({
            stopAlarm(context)
            Log.d("stop alarm", "알람 멈춤")
        }, alarmDuration)
    }

    private fun playAlarmSound(context: Context, selectedLevel: String?) {
        val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(context, alarmSound)

        if (ringtone == null) {
            Log.e("AlarmReceiver", "Failed to get ringtone")
            return
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        ringtone!!.audioAttributes = audioAttributes

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val volume = when (selectedLevel) {
            "낮음" -> maxVolume * 0.3
            "보통" -> maxVolume * 0.6
            "높음" -> maxVolume * 1.0
            else -> maxVolume * 0.6
        }
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume.toInt(), 0)

        Log.d("AlarmReceiver", "Playing alarm sound with level: $selectedLevel")
        ringtone!!.play()
    }

    private fun startVibration(context: Context, selectedLevel: String?) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val pattern = when (selectedLevel) {
            "낮음" -> longArrayOf(0, 100, 1000)
            "보통" -> longArrayOf(0, 500, 1000)
            "높음" -> longArrayOf(0, 1000, 1000)
            else -> longArrayOf(0, 500, 1000)
        }

        vibrator.vibrate(pattern, -1)
        Log.d("AlarmReceiver", "Starting vibration with level: $selectedLevel")
    }

    private fun stopAlarm(context: Context) {
        //ringtone?.stop()
        if (ringtone != null) {
            if (ringtone!!.isPlaying) {
                Log.d("AlarmReceiver", "Stopping alarm sound")
                ringtone!!.stop()
            } else {
                Log.d("AlarmReceiver", "Alarm sound is not playing")
            }
        } else {
            Log.d("AlarmReceiver", "Ringtone is null")
        }
        vibrator?.cancel()
        // 알람 상태 업데이트
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isAlarmRunning", false)
            apply()
        }
        Log.d("AlarmReceiver", "Alarm stopped")
    }
}