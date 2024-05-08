package com.cookandroid.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 알람이 울릴 때 실행할 코드
        Toast.makeText(context, "알람 울림!", Toast.LENGTH_SHORT).show()
        // 추가적인 작업 수행 (예: 알림 표시)
    }
}
