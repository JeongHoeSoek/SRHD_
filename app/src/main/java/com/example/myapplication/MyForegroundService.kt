package com.cookandroid.myapplication


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

class MyForegroundService : Service() {

    val CHANNEL_ID = "FGS153"
    val NOTI_ID = 153

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
            .setContentTitle("Foreground Service")
            .setContentText("Service is running in background")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .build()

        startForeground(NOTI_ID, notification)

        return START_STICKY
    }

    /*fun runBackground(){
        thread(start=true){
            for(i in 0..100){
                Thread.sleep(1000)
                Log.d("서비스", "COUNT===?$i")
            }
        }
    }*/

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // 서비스 재시작 시 구현할 공간
        super.onTaskRemoved(rootIntent)
    }
}