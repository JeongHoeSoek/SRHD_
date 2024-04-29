package com.cookandroid.myapplication.ui.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cookandroid.myapplication.databinding.FragmentPhoneBinding
import com.cookandroid.myapplication.R

class PhoneFragment : Fragment() {

    private var _binding: FragmentPhoneBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PhoneViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        binding.plusButton.setOnClickListener { showCustomDialog() }
    }

    private fun observeViewModel() {
        viewModel.infoList.observe(viewLifecycleOwner) { infoList ->
            updateUI(infoList)
        }
    }

    private fun updateUI(infoList: List<Pair<String, String>>) {
        val layout = binding.infoLayout
        layout.removeAllViews() // 기존 뷰 제거

        infoList.forEach { info ->
            val infoView = LayoutInflater.from(context).inflate(R.layout.info_data, layout, false)
            val nameLabel = infoView.findViewById<EditText>(R.id.name_label)
            val phoneLabel = infoView.findViewById<EditText>(R.id.phone_label)
            val deleteButton = infoView.findViewById<ImageButton>(R.id.delete_button)

            nameLabel.setText(info.first)
            phoneLabel.setText(info.second)
            deleteButton.setOnClickListener {
                viewModel.deleteInfo(info.first)
            }
            layout.addView(infoView)
        }
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.name_edit_text)
        val phoneEditText = dialogView.findViewById<EditText>(R.id.phone_edit_text)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .show()

        dialogView.findViewById<ImageButton>(R.id.cancel_button).setOnClickListener {
            alertDialog.dismiss()
        }

        dialogView.findViewById<ImageButton>(R.id.save_button).setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            viewModel.addInfo(name, phone)
            alertDialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

