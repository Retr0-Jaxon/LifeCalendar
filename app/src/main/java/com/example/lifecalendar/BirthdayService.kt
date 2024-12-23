package com.example.lifecalendar

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.Context
import android.os.IBinder
import android.util.Log
import java.util.Calendar
import java.util.Date
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class BirthdayService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        checkIf24HoursPassed()
        // 设置定时器，每隔20s检查一次
        val thread = Thread {
            while (true) {
                try {
                    Thread.sleep((20 * 1000).toLong()) // 休眠20s，单位是毫秒
                    checkIf24HoursPassed()
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread hibernation is interrupted.", e)
                }
            }
        }
        thread.start()

        return START_STICKY
    }

    private fun checkIf24HoursPassed(): Boolean {
        val zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
        val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
        val formattedDateTime = zonedDateTime.format(formatter)
        Log.e("time", "Time=" + formattedDateTime)

        // 将formattedDateTime存储到LifeCalendarProvider中
        storeTimeInProvider(formattedDateTime)

        return true
    }


    private fun storeTimeInProvider(timeString: String) {
        val uri = LifeCalendarProvider.TIME_CHECK_URI
        
        // 查询是否已有记录
        val cursor = contentResolver.query(
            uri,
            arrayOf(LifeCalendarProvider.TIME_CHECK_COLUMN_ID),
            null,
            null,
            null
        )

        val values = ContentValues().apply {
            put(LifeCalendarProvider.TIME_CHECK_COLUMN_TIMESTAMP, System.currentTimeMillis())
            put(LifeCalendarProvider.TIME_CHECK_COLUMN_FORMATTED_TIME, timeString)
        }

        cursor?.use {
            if (it.moveToFirst()) {
                // 已有记录，更新它
                val id = it.getInt(it.getColumnIndexOrThrow(LifeCalendarProvider.TIME_CHECK_COLUMN_ID))
                contentResolver.update(
                    uri,
                    values,
                    "${LifeCalendarProvider.TIME_CHECK_COLUMN_ID} = ?",
                    arrayOf(id.toString())
                )
                Log.d("BirthdayService", "Updated time check data")
            } else {
                // 没有记录，插入新记录
                val newUri = contentResolver.insert(uri, values)
                if (newUri != null) {
                    Log.d("BirthdayService", "Successfully inserted time check data")
                } else {
                    Log.e("BirthdayService", "Failed to insert time check data")
                }
            }
        }

        // 查询并展示最新存储的时间数据
        val checkCursor = contentResolver.query(
            uri,
            arrayOf(
                LifeCalendarProvider.TIME_CHECK_COLUMN_FORMATTED_TIME,
                LifeCalendarProvider.TIME_CHECK_COLUMN_TIMESTAMP
            ),
            null,
            null,
            null
        )

        checkCursor?.use {
            if (it.moveToFirst()) {
                val formattedTime = it.getString(
                    it.getColumnIndexOrThrow(LifeCalendarProvider.TIME_CHECK_COLUMN_FORMATTED_TIME)
                )
                val timestamp = it.getLong(
                    it.getColumnIndexOrThrow(LifeCalendarProvider.TIME_CHECK_COLUMN_TIMESTAMP)
                )
                Log.d("BirthdayService", "Stored time: $formattedTime, Timestamp: $timestamp")
            }
        }
    }

    companion object {
        private const val TAG = "BirthdayService"
    }
}