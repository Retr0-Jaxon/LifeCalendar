package com.example.lifecalendar.ui.home

import ButtonAdapter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.lifecalendar.LifeCalendarProvider
import com.example.lifecalendar.databinding.FragmentHomeBinding

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

         val buttonTexts = MutableList(getStoredWeeks()) { "${it}" } // 8 行 6 列，共 48 个按钮
//        val buttonTexts = List(96) { " " }
        recyclerViewAdapter = ButtonAdapter(buttonTexts)
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
        val newButtonTexts = MutableList(newWeeks) { "${it}" }
        recyclerViewAdapter.updateItems(newButtonTexts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}