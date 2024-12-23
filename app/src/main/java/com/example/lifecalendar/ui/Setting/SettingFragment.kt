package com.example.lifecalendar.ui.Setting

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.lifecalendar.BirthdayService
import com.example.lifecalendar.databinding.FragmentSettingBinding
import com.example.lifecalendar.ui.birthdayDialog.BirthdayDialogFragment
import com.example.lifecalendar.ui.lifespanDialog.lifeSpanFragment

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = SettingFragment()
    }

    private val viewModel: SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set up the button click listener for birthday dialog
        binding.buttonShowBirthdayDialog.setOnClickListener {
            showBirthdayDialog()
        }

        // Set up the button click listener for lifespan dialog
        binding.buttonShowLifespanDialog.setOnClickListener {
            showLifespanDialog()
        }

        return root
    }

    private fun showBirthdayDialog() {
        val dialog = BirthdayDialogFragment()
        dialog.show(requireActivity().supportFragmentManager, "birthday_dialog")
    }

    private fun showLifespanDialog() {
        val lifeSpanDialog = lifeSpanFragment()
        lifeSpanDialog.show(requireActivity().supportFragmentManager, "life_span_dialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}