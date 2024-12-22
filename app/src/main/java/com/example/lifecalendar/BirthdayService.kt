package com.example.lifecalendar

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.Calendar
import java.util.Date

import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import java.util.Locale

class BirthdayService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 检测是否过了24小时
        checkIf24HoursPassed()
        // 设置定时器，每隔一分钟检查一次
        val thread = Thread {
            while (true) {
                try {
                    Thread.sleep((60 * 1000).toLong()) // 休眠一分钟，单位是毫秒
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
        // 从SharedPreferences获取上次记录的日期（这里假设键为"last_record_date"，需要根据实际情况修改）
        val lastRecordDate = getLastRecordDateFromPrefs()
        val currentDate = Date()

        if (lastRecordDate!= null) {
            // 计算时间差
            val diffInMillis = currentDate.time - lastRecordDate.time
            val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
            if (diffInDays >= 1) {
                // 如果过了24小时，更新记录日期，并返回true
                saveLastRecordDate(currentDate)
                return true
            }
        } else {
            // 如果没有上次记录的日期，保存当前日期作为第一次记录
            saveLastRecordDate(currentDate)
        }
        return false
    }

    private fun getLastRecordDateFromPrefs(): Date? {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val dateString = sharedPreferences.getString("last_record_date", null)
        return if (dateString!= null) {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.parse(dateString)
        } else {
            null
        }
    }

    private fun saveLastRecordDate(date: Date) {
        val editor = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        editor.putString("last_record_date", formatter.format(date))
        editor.apply()
    }

    private fun getDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }

    companion object {
        private const val TAG = "BirthdayService"
    }
}