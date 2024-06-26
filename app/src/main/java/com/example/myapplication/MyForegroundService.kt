package com.cookandroid.myapplication


import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class MyForegroundService : Service() {

    private val CHANNEL_ID = "FGS153"
    private val NOTI_ID = 153
    private var serviceScope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()
        //serviceScope = CoroutineScope(Dispatchers.Default)
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val serviceChannel = NotificationChannel(CHANNEL_ID, "SRHD", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Foreground Service", "Service started")
        serviceScope?.cancel()  // Cancel existing scope if exists
        serviceScope = CoroutineScope(Dispatchers.Default)  // Create new scope
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SRHD")
            .setContentText("SRHD 애플리케이션 실행 중...")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .build()

        startForeground(NOTI_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)

        runBackground()

        return START_STICKY
    }

    private suspend fun initializeSSHConnection() {
        val data = withContext(Dispatchers.IO) {
            SSHManager.loadFromServer() // SSH로 서버에서 데이터 로드
        }
        data?.let {
            Log.d("Service", "Received new data from server: $it")
            if (it.trim() == "1") {
                // 브로드캐스트를 통해 HomeViewModel의 상태를 변경합니다.
                //val intent = Intent("com.cookandroid.myapplication.ACTION_UPDATE_STATE")

                //intent.putExtra("state", true)
                //Log.d("값 변경", intent.getBooleanExtra("state", false).toString())
                //sendBroadcast(intent)
                updateState(true)
                // 알람을 설정합니다.
                //setAlarm()
            }
        }
    }
    private fun updateState(state: Boolean) {
        // 상태를 SharedPreferences에 저장합니다.
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("IS_IMAGE_ON", state)
            apply()
        }

        val intent = Intent("com.cookandroid.myapplication.ACTION_UPDATE_STATE")
        intent.putExtra("state", state)
        sendBroadcast(intent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        //val alarmTimeAtUTC = System.currentTimeMillis()
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        //sendBroadcast(intent)
    }

    private fun runBackground() {
        Log.d("runBackground", "백그라운드 메서드 실행")
        serviceScope?.launch {
            while (isActive) {
                Log.d("isActive", "반복문 부분")
                try {
                    initializeSSHConnection()
                    delay(1000)  // 1초 대기
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    Log.d("Service", "Thread was interrupted, shutting down.")
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        //SSHManager.closeSession() // SSH 세션을 명시적으로 닫습니다.
        serviceScope?.cancel("Service is being destroyed")
        Log.d("Service", "Service destroyed.")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        serviceScope?.cancel()

        val restartServiceIntent = Intent(applicationContext, this::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        val alarmService: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent)
        super.onTaskRemoved(rootIntent)
    }
}
