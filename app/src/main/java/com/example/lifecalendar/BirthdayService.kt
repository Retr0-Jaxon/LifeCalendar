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
        // 检测天数变化
        checkDaysToBirthday()
        // 设置定时器，每隔一天检查一次
        val thread = Thread {
            while (true) {
                try {
                    Thread.sleep((24 * 60 * 60 * 1000).toLong()) // 休眠一天，单位是毫秒
                    checkDaysToBirthday()
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread hibernation is interrupted.", e)
                }
            }
        }
        thread.start()

        return START_STICKY
    }

    private fun checkDaysToBirthday() {
        val birthday = getBirthdayFromPrefs()
        if (birthday!= null) {
            val currentCal = Calendar.getInstance()
            val birthCal = Calendar.getInstance()
            birthCal.time = birthday

            // 设置生日的年份为当前年份，以便比较今年的生日日期
            birthCal[Calendar.YEAR] = currentCal[Calendar.YEAR]

            var diffInMillis = birthCal.timeInMillis - currentCal.timeInMillis
            if (diffInMillis < 0) {
                // 如果生日已经过了，计算明年生日的时间差
                birthCal[Calendar.YEAR] = currentCal[Calendar.YEAR] + 1
                diffInMillis = birthCal.timeInMillis - currentCal.timeInMillis
            }

            val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
            Log.d(TAG, "You have $diffInDays day until your next birthday")

        }
    }

    private fun getBirthdayFromPrefs(): Date? {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val birthdayString = sharedPreferences.getString("birthday", null)
        return if (birthdayString!= null) {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.parse(birthdayString)
        } else {
            null
        }
    }

    companion object {
        private const val TAG = "BirthdayService"
    }
}