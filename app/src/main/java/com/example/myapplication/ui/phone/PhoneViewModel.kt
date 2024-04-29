package com.cookandroid.myapplication.ui.phone

import android.app.Application
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.provider.BaseColumns

class PhoneViewModel(application: Application) : AndroidViewModel(application) {
    private val _infoList = MutableLiveData<List<Pair<String, String>>>()
    val infoList: LiveData<List<Pair<String, String>>> = _infoList

    private val dbHelper = object : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }
    }

    init {
        loadInfoFromDatabase()
    }

    private fun loadInfoFromDatabase() {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(COLUMN_NAME_NAME, COLUMN_NAME_PHONE)
        val cursor = db.query(TABLE_NAME, projection, null, null, null, null, null)

        val items = mutableListOf<Pair<String, String>>()
        with(cursor) {
            while (moveToNext()) {
                val name = getString(getColumnIndexOrThrow(COLUMN_NAME_NAME))
                val phone = getString(getColumnIndexOrThrow(COLUMN_NAME_PHONE))
                items.add(Pair(name, phone))
            }
        }
        cursor.close()
        _infoList.postValue(items)
    }

    fun addInfo(name: String, phone: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME_NAME, name)
            put(COLUMN_NAME_PHONE, phone)
        }
        db.insert(TABLE_NAME, null, values)
        loadInfoFromDatabase() // Refresh list
    }

    fun deleteInfo(name: String) {
        val db = dbHelper.writableDatabase
        val selection = "$COLUMN_NAME_NAME = ?"
        val selectionArgs = arrayOf(name)
        db.delete(TABLE_NAME, selection, selectionArgs)
        loadInfoFromDatabase() // Refresh list
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Info.db"
        const val TABLE_NAME = "info"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_PHONE = "phone"
        const val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "$COLUMN_NAME_NAME TEXT," +
                    "$COLUMN_NAME_PHONE TEXT)"
        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}

