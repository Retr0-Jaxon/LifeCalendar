package com.example.lifecalendar.ui.birthdayDialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.lifecalendar.R

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date

class BirthdayDialogFragment : DialogFragment() {

    interface OnBirthdaySetListener {
        fun onBirthdaySet(date: Date)
    }

    private var listener: OnBirthdaySetListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBirthdaySetListener) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnBirthdaySetListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)


        return DatePickerDialog(requireContext(), { _: DatePicker, year: Int, month: Int, day: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, day)
            listener?.onBirthdaySet(selectedCalendar.time)
            dismiss()
        }, year, month, day)
    }
}