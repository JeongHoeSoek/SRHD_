package com.cookandroid.myapplication

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.Toast
import com.cookandroid.myapplication.ui.home.HomeFragment

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 알람이 울릴 때 실행할 코드
        Toast.makeText(context, "알람 울림!", Toast.LENGTH_LONG).show()
        // 추가적인 작업 수행 (예: 알림 표시)
        val builder = AlertDialog.Builder(context).apply {
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
        }
    }
}
