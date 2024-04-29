package com.cookandroid.myapplication.ui.dashboard

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        companion object {

            const val KEY_MESSAGE = "message"
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "MessageDB"
            const val TABLE_MESSAGES = "messages"
            const val COLUMN_NAME_ID = BaseColumns._ID
            const val COLUMN_NAME_MESSAGE = "message"

            private const val SQL_CREATE_ENTRIES = "CREATE TABLE $TABLE_MESSAGES (" +
                    "$COLUMN_NAME_ID INTEGER PRIMARY KEY," +
                    "$COLUMN_NAME_MESSAGE TEXT)"

            private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_MESSAGES"
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }
    }
    private val dbHelper: DBHelper = DBHelper(application)

    init {
        _message.value = getMessageFromDatabase()
    }

    fun saveMessage(newMessage: String) {
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM ${DBHelper.TABLE_MESSAGES}") // 기존 메시지 삭제
        val values = ContentValues().apply {
            put(DBHelper.KEY_MESSAGE, newMessage)
        }
        db.insert(DBHelper.TABLE_MESSAGES, null, values)
        _message.value = newMessage // LiveData 업데이트
    }

    private fun getMessageFromDatabase(): String? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DBHelper.TABLE_MESSAGES, arrayOf(DBHelper.KEY_MESSAGE), null, null, null, null, null)
        var message: String? = null
        if (cursor.moveToFirst()) {
            message = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_MESSAGE))
        }
        cursor.close()
        return message
    }

    override fun onCleared() {
        dbHelper.close()
        super.onCleared()
    }
}
