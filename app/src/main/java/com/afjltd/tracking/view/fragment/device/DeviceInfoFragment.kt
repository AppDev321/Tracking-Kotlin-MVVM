package com.afjltd.tracking.view.fragment.device


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.databinding.FragmentDeviceFormBinding


class DeviceInfoFragment : Fragment() {

    private var _binding: FragmentDeviceFormBinding? = null
    private val binding get() = _binding!!


    private val TAG = DeviceInfoFragment::class.java.simpleName




    private lateinit var mBaseActivity: NavigationDrawerActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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