package com.example.lifecalendar.ui.slideshow

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.lifecalendar.databinding.FragmentSlideshowBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SlideshowFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var currentStepCount = 0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化 SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)

        // 初始化传感器
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // 检查并初始化当天步数
        checkAndInitializeSteps()

        // 获取布局元素
        val textView: TextView = binding.textSlideshow
        val stepsTextView: TextView = binding.textSteps
        val refreshButton: Button = binding.buttonRefreshStep

        // 刷新按钮点击事件
        refreshButton.setOnClickListener {
            updateStepCount(currentStepCount)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        // 注册传感器监听
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // 注销传感器监听
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            // 记录总步数并更新 UI
            val totalSteps = event.values[0].toInt()
            if (totalSteps >= currentStepCount) {
                currentStepCount = totalSteps
                updateStepCount(currentStepCount)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 可选：处理传感器精度变化
    }

    private fun checkAndInitializeSteps() {
        // 获取当前日期
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 从 SharedPreferences 获取上次保存的日期
        val lastDate = sharedPreferences.getString("lastDate", "")
        if (lastDate != currentDate) {
            // 如果是新的一天，重置步数
            currentStepCount = 0
            saveStepData(currentDate, currentStepCount)
        } else {
            // 如果是同一天，加载之前保存的步数
            currentStepCount = sharedPreferences.getInt("currentStepCount", 0)
        }
        updateStepCount(currentStepCount)
    }

    private fun saveStepData(date: String, steps: Int) {
        // 保存当前日期和步数到 SharedPreferences
        with(sharedPreferences.edit()) {
            putString("lastDate", date)
            putInt("currentStepCount", steps)
            apply()
        }
    }

    private fun updateStepCount(newStepCount: Int) {
        // 更新当前步数并保存
        currentStepCount = newStepCount
        binding.textSteps.text = "步数: $currentStepCount"
        saveStepData(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            currentStepCount
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


