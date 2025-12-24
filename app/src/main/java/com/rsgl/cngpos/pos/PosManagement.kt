package com.rsgl.cngpos.pos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import java.util.*
import androidx.navigation.fragment.findNavController
import com.rsgl.cngpos.databinding.PosFragmentBinding


class PosManagement : Fragment() {

    // Use nullable binding pattern
    private var _binding: PosFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Initialize binding properly
        _binding = PosFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // HIDE HEADING / LABEL HERE
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        binding.btnClickToEnter.setOnClickListener {
            val action = PosManagementDirections.actionPosFragmentToSalesMainFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding to avoid memory leaks
        _binding = null
    }
}