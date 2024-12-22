package com.example.lifecalendar.ui.gallery

import MemoAdapter
import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifecalendar.LifeCalendarProvider
import com.example.lifecalendar.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var memoAdapter: MemoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupFab()
        loadMemos()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        memoAdapter = MemoAdapter(mutableListOf()) { memoId ->
            deleteMemo(memoId)
        }
        binding.memoRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = memoAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddMemo.setOnClickListener {
            showAddMemoDialog()
        }
    }

    private fun showAddMemoDialog() {
        val editText = EditText(context).apply {
            hint = "输入备忘录内容"
            setPadding(64, 32, 100, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("添加内容...")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val content = editText.text.toString()
                if (content.isNotEmpty()) {
                    saveMemo(content)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveMemo(content: String) {
        val values = ContentValues().apply {
            put(LifeCalendarProvider.MEMO_COLUMN_CONTENT, content)
            put(LifeCalendarProvider.MEMO_COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        context?.contentResolver?.insert(LifeCalendarProvider.MEMO_URI, values)
        loadMemos()
    }

    private fun loadMemos() {
        val cursor = context?.contentResolver?.query(
            LifeCalendarProvider.MEMO_URI,
            null,
            null,
            null,
            "${LifeCalendarProvider.MEMO_COLUMN_TIMESTAMP} DESC"
        )

        val memos = mutableListOf<MemoAdapter.Memo>()
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(LifeCalendarProvider.MEMO_COLUMN_ID))
                val content = it.getString(it.getColumnIndexOrThrow(LifeCalendarProvider.MEMO_COLUMN_CONTENT))
                val timestamp = it.getLong(it.getColumnIndexOrThrow(LifeCalendarProvider.MEMO_COLUMN_TIMESTAMP))
                memos.add(MemoAdapter.Memo(id, content, timestamp))
            }
        }
        memoAdapter.updateMemos(memos)
    }

    private fun deleteMemo(memoId: Int) {
        context?.contentResolver?.delete(
            LifeCalendarProvider.MEMO_URI,
            "${LifeCalendarProvider.MEMO_COLUMN_ID} = ?",
            arrayOf(memoId.toString())
        )
        loadMemos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}