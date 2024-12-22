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
        val values = ContentValues()
        values.put(LifeCalendarProvider.LIFESPAN_COLUMN_TIME, timeString)
        val uri = LifeCalendarProvider.CONTENT_URI
        val newUri = contentResolver.insert(uri, values)
        if (newUri!= null) {
            Log.d("BirthdayService", "Successfully inserted data with Uri: $newUri")
        } else {
            Log.e("BirthdayService", "Failed to insert data.")
        }
    }

    companion object {
        private const val TAG = "BirthdayService"
    }
}