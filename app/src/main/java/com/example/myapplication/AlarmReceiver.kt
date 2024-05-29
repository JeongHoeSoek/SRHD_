package com.cookandroid.myapplication

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import com.cookandroid.myapplication.ui.dashboard.DashboardViewModel.DBHelper as MessageDBHelper
import com.cookandroid.myapplication.ui.phone.PhoneViewModel
import com.cookandroid.myapplication.ui.dashboard.DashboardViewModel

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private var ringtone: Ringtone? = null
        private var vibrator: Vibrator? = null
        private val handler = Handler(Looper.getMainLooper())
        private var isManualStop = false
        private var alarmRunnable: Runnable? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "STOP_ALARM") {
            Log.d("AlarmReceiver", "Alarm manually stopped")
            isManualStop = true
            stopAlarm(context)
            return
        }

        Toast.makeText(context, "알람 울림!", Toast.LENGTH_LONG).show()
        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isAlarmRunning", true)
            apply()
        }

        val selectedTime = sharedPref.getString("SelectedTime", "30초")
        val selectedLevel = sharedPref.getString("SelectedLevel", "보통")
        val selectedMode = sharedPref.getString("SelectedMode", "소리")

        when (selectedMode) {
            "소리" -> playAlarmSound(context, selectedLevel)
            "진동" -> startVibration(context, selectedLevel)
            else -> playAlarmSound(context, selectedLevel)
        }

        val alarmDuration = when (selectedTime) {
            "10초" -> 10000L
            "30초" -> 30000L
            "1분" -> 60000L
            "3분" -> 180000L
            else -> 30000L
        }

        alarmRunnable = Runnable {
            if (!isManualStop) {
                stopAlarm(context)
                Log.d("AlarmReceiver", "Alarm stopped automatically")
            }
        }
        handler.postDelayed(alarmRunnable!!, alarmDuration)
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
        if (ringtone != null && ringtone!!.isPlaying) {
            ringtone!!.stop()
        }
        vibrator?.cancel()
        alarmRunnable?.let { handler.removeCallbacks(it) }

        val sharedPref = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isAlarmRunning", false)
            apply()
        }

        if (!isManualStop) {
            sendSMS(context)
        } else {
            isManualStop = false
        }

        Log.d("AlarmReceiver", "Alarm stopped")
    }

    private fun sendSMS(context: Context) {
        val smsManager = SmsManager.getDefault()
        val message = getMessageFromDatabase(context) ?: return
        val phoneNumbers = getPhoneNumbersFromDatabase(context) ?: return

        Log.d("AlarmReceiver", "sendSMS")
        phoneNumbers.forEach { phoneNumber ->
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("AlarmReceiver", "SMS sent to $phoneNumber: $message")
        }
    }

    private fun getMessageFromDatabase(context: Context): String? {
        val dbHelper = MessageDBHelper(context)
        val db = dbHelper.readableDatabase
        val cursor = db.query(MessageDBHelper.TABLE_MESSAGES, arrayOf(MessageDBHelper.KEY_MESSAGE), null, null, null, null, null)
        var message: String? = null
        if (cursor.moveToFirst()) {
            message = cursor.getString(cursor.getColumnIndexOrThrow(MessageDBHelper.KEY_MESSAGE))
        }
        cursor.close()
        dbHelper.close()
        return message
    }

    private fun getPhoneNumbersFromDatabase(context: Context): List<String>? {
        val dbHelper = object : SQLiteOpenHelper(context, PhoneViewModel.DATABASE_NAME, null, PhoneViewModel.DATABASE_VERSION) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(PhoneViewModel.SQL_CREATE_ENTRIES)
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                db.execSQL(PhoneViewModel.SQL_DELETE_ENTRIES)
                onCreate(db)
            }
        }

        val db = dbHelper.readableDatabase
        val cursor = db.query(PhoneViewModel.TABLE_NAME, arrayOf(PhoneViewModel.COLUMN_NAME_PHONE), null, null, null, null, null)
        val phoneNumbers = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {
                val phone = getString(getColumnIndexOrThrow(PhoneViewModel.COLUMN_NAME_PHONE))
                phoneNumbers.add(phone)
            }
        }
        cursor.close()
        dbHelper.close()
        return phoneNumbers
    }
}
