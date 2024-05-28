package com.cookandroid.myapplication

import android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cookandroid.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //createNotificationChannel()
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_phone, R.id.navigation_sms, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //createNotificationChannel()
        requestPermission()

        requestSMSPermission()
        // 배터리 최적화 예외를 요청합니다.
        if (!isIgnoringBatteryOptimizations(this)) {
            requestIgnoreBatteryOptimization()
        }
        //requestBatteryPermission()
        //val startIntent = Intent(this, MyForegroundService::class.java)
        //ContextCompat.startForegroundService(this, startIntent)
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    private fun requestPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {

                //권한이 허용됐을 때
                override fun onPermissionGranted() {
                    startProcess()
                }

                //권한이 거부됐을 때
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MainActivity, "알림 권한이 거부됨.", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("알림 권한을 허용해주세요.")// 권한이 없을 때 띄워주는 Dialog Message
            .setPermissions(android.Manifest.permission.POST_NOTIFICATIONS)// 얻으려는 권한(여러개 가능)
            .check()
    }

    private fun requestSMSPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {

                //권한이 허용됐을 때
                override fun onPermissionGranted() {
                    //startProcess()
                }

                //권한이 거부됐을 때
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MainActivity, "알림 권한이 거부됨.", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("SMS 권한을 허용해주세요.")// 권한이 없을 때 띄워주는 Dialog Message
            .setPermissions(android.Manifest.permission.SEND_SMS)// 얻으려는 권한(여러개 가능)
            .check()
    }

    /*private fun requestBatteryPermission() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {

                //권한이 허용됐을 때
                override fun onPermissionGranted() {
                    //startProcess()
                }

                //권한이 거부됐을 때
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MainActivity, "배터리 최적화 해제 거부됨.", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("배터리 최적화 목록 제외를 이행해주세요.")// 권한이 없을 때 띄워주는 Dialog Message
            .setPermissions(android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)// 얻으려는 권한(여러개 가능)
            .check()
    }*/

    private fun startProcess() {
        val startIntent = Intent(this, MyForegroundService::class.java)
        ContextCompat.startForegroundService(this, startIntent)
    }
    /*private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name) // 알림 채널의 이름
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(getString(R.string.notification_channel_id), name, importance)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }*/
}