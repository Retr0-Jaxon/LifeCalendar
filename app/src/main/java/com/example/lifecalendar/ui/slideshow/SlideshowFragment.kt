package com.example.lifecalendar.ui.slideshow

import android.content.Context
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

class SlideshowFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var currentStepCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 获取 TextView 和按钮
        val textView: TextView = binding.textSlideshow
        val stepsTextView: TextView = binding.textSteps
        val refreshButton: Button = binding.buttonRefreshStep

        // 观察步数变化并更新 UI
        slideshowViewModel.steps.observe(viewLifecycleOwner) {
            stepsTextView.text = " $it 步"
        }

        // 初始化传感器
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // 刷新按钮点击事件
        refreshButton.setOnClickListener {
            updateStepCount(currentStepCount) // 手动刷新步数
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        // 注册传感器事件监听器
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // 取消传感器监听
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            // 获取设备启动后的总步数
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

    private fun updateStepCount(newStepCount: Int) {
        val slideshowViewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)
        slideshowViewModel.updateStepCount(newStepCount) // 更新步数
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

