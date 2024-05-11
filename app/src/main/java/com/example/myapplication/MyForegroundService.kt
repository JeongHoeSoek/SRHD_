package com.cookandroid.myapplication


import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

class MyForegroundService : Service() {

    val CHANNEL_ID = "FGS153"
    val NOTI_ID = 153
    private var isRunning = true
    private lateinit var backgroundThread: Thread

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = getString(R.string.channel_name)
            val serviceChannel = NotificationChannel(CHANNEL_ID, "SRHD", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Foreground Service", "Service started")
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SRHD")
            .setContentText("SRHD 애플리케이션 실행 중...")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .build()

        startForeground(NOTI_ID, notification)

        // 스레드가 이미 실행중인지 확인하고 중지
        /*if (::backgroundThread.isInitialized && backgroundThread.isAlive) {
            isRunning = false
            backgroundThread.interrupt()
            backgroundThread.join()
        }*/
        runBackground()
        return START_STICKY
    }

    private fun runBackground(){
        backgroundThread = Thread {
            try {
                var i = 0
                while (!Thread.currentThread().isInterrupted) {
                    Thread.sleep(1000)
                    Log.d("Service", "Running in background $i")
                    i++
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt() // 스레드의 인터럽트 상태를 복구합니다.
                Log.d("Service", "Thread was interrupted, shutting down.")
            }
        }
        backgroundThread.start()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // 서비스 재시작 시 구현할 공간
        // 스레드 안전 종료
        isRunning = false

        backgroundThread.interrupt()
        backgroundThread.join()

        // 서비스 재시작 인텐트 생성
        val restartServiceIntent = Intent(applicationContext, this::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)

        // 알람 서비스를 사용하여 서비스를 재시작
        val alarmService: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent)
        super.onTaskRemoved(rootIntent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDestroy() {
        super.onDestroy()
        /*if (::backgroundThread.isInitialized && backgroundThread.isAlive) {
            backgroundThread.interrupt()
            backgroundThread.join()
        }*/
        stopForeground(STOP_FOREGROUND_DETACH)
        Log.d("Service", "Service destroyed.")
    }
}