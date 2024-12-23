package com.example.lifecalendar.ui.home

import ButtonAdapter
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.lifecalendar.LifeCalendarProvider
import com.example.lifecalendar.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: ButtonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val weeks = getStoredWeeks()

        // val textView: TextView = binding.textHome
        // homeViewModel.text.observe(viewLifecycleOwner) {
        //     textView.text = it
        // }

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 6) // 6 列

         val buttonTexts = MutableList(getStoredWeeks()) { "${it}" }
        val initialWeeksDiff = calculateWeeksDifference() ?: 0
        
        // Get the saved preference
        val showNumbers = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getBoolean("show_numbers", true) ?: true
        
        recyclerViewAdapter = ButtonAdapter(buttonTexts, initialWeeksDiff, showNumbers)
        recyclerView.adapter = recyclerViewAdapter
        return root
    }

    private fun getStoredWeeks(): Int {
        val uri: Uri = LifeCalendarProvider.CONTENT_URI
        val projection = arrayOf(LifeCalendarProvider.LIFESPAN_COLUMN_WEEKS)
        val cursor: Cursor? = context?.contentResolver?.query(uri, projection, null, null, null)
         return if (cursor != null && cursor.moveToFirst()) {
            val weeksIndex = cursor.getColumnIndex(LifeCalendarProvider.LIFESPAN_COLUMN_WEEKS)
            cursor.getInt(weeksIndex).also {
                cursor.close() // 记得关闭游标
            }
        } else {
            0 // 如果没有数据，返回默认值
        }
    }

    fun refreshRecyclerView() {
        val newWeeks = getStoredWeeks()
        val weeksDiff = calculateWeeksDifference() ?: 0
        val newButtonTexts = MutableList(newWeeks) { "${it}" }
        val showNumbers = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getBoolean("show_numbers", true) ?: true
        recyclerViewAdapter.updateItems(newButtonTexts, weeksDiff, showNumbers)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun calculateWeeksDifference(): Int? {
        val timeCheckUri = LifeCalendarProvider.TIME_CHECK_URI
        val birthdayString = context?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("birthday", null)

        if (birthdayString == null) {
            Log.d("HomeFragment", "Birthday not set")
            return null
        }

        val cursor = context?.contentResolver?.query(
            timeCheckUri,
            arrayOf(LifeCalendarProvider.TIME_CHECK_COLUMN_FORMATTED_TIME),
            null,
            null,
            null
        )

        return cursor?.use { 
            if (it.moveToFirst()) {
                val currentTimeString = it.getString(
                    it.getColumnIndexOrThrow(LifeCalendarProvider.TIME_CHECK_COLUMN_FORMATTED_TIME)
                )

                try {
                    // 使用正确的格式解析生日
                    val birthdayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val birthday = birthdayFormatter.parse(birthdayString)

                    // 使用服务中使用的格式解析当前时间
                    val currentTimeFormatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
                    val currentDate = currentTimeFormatter.parse(currentTimeString)

                    if (birthday != null && currentDate != null) {
                        val diffInMillis = currentDate.time - birthday.time
                        (diffInMillis / (7 * 24 * 60 * 60 * 1000)).toInt()
                    } else null
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error parsing dates", e)
                    null
                }
            } else null
        } ?: null
    }

    fun scrollToCurrentWeek() {
        val weeksDiff = calculateWeeksDifference()
        if (weeksDiff != null) {
            smoothScrollToPositionWithCenter(weeksDiff)
            Log.d("HomeFragment", "Scrolling to week: $weeksDiff")
        }
    }

    private fun smoothScrollToPositionWithCenter(position: Int) {
        binding.recyclerView.post {
            val layoutManager = binding.recyclerView.layoutManager as GridLayoutManager
            
            // 创建平滑滚动器
            val smoothScroller = object : LinearSmoothScroller(context) {
                override fun getVerticalSnapPreference(): Int {
                    return SNAP_TO_START
                }

                override fun calculateDtToFit(
                    viewStart: Int,
                    viewEnd: Int,
                    boxStart: Int,
                    boxEnd: Int,
                    snapPreference: Int
                ): Int {
                    // 计算居中位置
                    return boxStart + (boxEnd - boxStart) / 2 - (viewStart + (viewEnd - viewStart) / 2)
                }

                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    // 计算当前位置到目标位置的距离
                    val currentPosition = layoutManager.findFirstVisibleItemPosition()
                    val distance = Math.abs(position - currentPosition)
                    
                    // 根据距离动态调整速度，确保动画在1秒内完成
                    val totalPixels = distance * (binding.recyclerView.width / layoutManager.spanCount)
                    return (totalPixels / 500f) / displayMetrics.densityDpi
                }

                override fun calculateTimeForScrolling(dx: Int): Int {
                    // 强制限制最大滚动时间为1000ms
                    return minOf(500, super.calculateTimeForScrolling(dx))
                }
            }

            // 设置目标位置并开始滚动
            smoothScroller.targetPosition = position
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }

}