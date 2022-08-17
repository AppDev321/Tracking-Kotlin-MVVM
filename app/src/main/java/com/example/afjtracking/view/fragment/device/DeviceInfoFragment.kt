package com.example.afjtracking.view.fragment.device

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.databinding.FragmentDeviceFormBinding
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.fuel.viewmodel.FuelViewModel


class DeviceInfoFragment : Fragment() {

    private var _binding: FragmentDeviceFormBinding? = null
    private val binding get() = _binding!!


    private val TAG = DeviceInfoFragment::class.java.simpleName


    private var _fuelViewModel: FuelViewModel? = null
    private val fuelViewModel get() = _fuelViewModel!!

    private lateinit var mBaseActivity: NavigationDrawerActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fuelViewModel = ViewModelProvider(this).get(FuelViewModel::class.java)

        _binding = FragmentDeviceFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.deviceInfo = AFJUtils.getDeviceDetail()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}