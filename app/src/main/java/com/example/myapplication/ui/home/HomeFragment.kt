package com.cookandroid.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cookandroid.myapplication.R
import com.cookandroid.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.isImageOn.observe(viewLifecycleOwner) { isOn ->
            updateButtonState(isOn)
        }

        binding.buttonToggle.setOnClickListener {
            homeViewModel.toggleImageState()
        }
    }

    private fun updateButtonState(isImageOn: Boolean) {
        if (isImageOn) {
            // 앱 기능 ON 시 코드
            binding.imageView.setImageResource(R.drawable.main_icon_on)
            binding.buttonToggle.setImageResource(R.drawable.main_on_button)
        } else {
            // 앱 기능 OFF 시 코드
            binding.imageView.setImageResource(R.drawable.main_icon_off)
            binding.buttonToggle.setImageResource(R.drawable.main_off_button)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
