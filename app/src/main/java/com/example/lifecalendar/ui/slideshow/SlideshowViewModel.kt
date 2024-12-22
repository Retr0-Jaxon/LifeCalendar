package com.example.lifecalendar.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SlideshowViewModel : ViewModel() {

    // 用于存储步数的 LiveData
    private val _steps = MutableLiveData<Int>().apply {
        value = 0  // 初始步数为0
    }
    val steps: LiveData<Int> = _steps

    // 更新步数的方法
    fun updateStepCount(newStepCount: Int) {
        _steps.value = newStepCount
    }
}
