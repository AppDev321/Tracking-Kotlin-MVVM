package com.example.afjtracking.view.fragment.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentVehicleDetailBinding
import com.example.afjtracking.databinding.LayoutVehicleInfoTextBinding
import com.example.afjtracking.model.responses.VehicleDetail
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.activity.NavigationDrawerActivity

class VehicleDetailFragment : Fragment() {

    private var _binding: FragmentVehicleDetailBinding? = null
    private val binding get() = _binding!!


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


        _binding = FragmentVehicleDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root





        try {
            //Save vehicle object
            val vehicleDetail = AFJUtils.getObjectPref(
                mBaseActivity,
                AFJUtils.KEY_VEHICLE_DETAIL,
                VehicleDetail::class.java
            )
            createVehicleView("VRN", vehicleDetail?.vrn ?: Constants.NULL_DEFAULT_VALUE)
            createVehicleView("Type", vehicleDetail?.type ?: Constants.NULL_DEFAULT_VALUE)
            createVehicleView("Model", vehicleDetail?.model ?: Constants.NULL_DEFAULT_VALUE)
            createVehicleView("Make", vehicleDetail?.make ?: Constants.NULL_DEFAULT_VALUE)
            createVehicleView(
                "Odometer Reading",
                vehicleDetail?.odometerReading ?: Constants.NULL_DEFAULT_VALUE
            )
            createVehicleView(
                "Vehicle Type",
                vehicleDetail?.detail?.vehicleType ?: Constants.NULL_DEFAULT_VALUE
            )
        } catch (e: Exception) {
            mBaseActivity.writeExceptionLogs(e.toString())
        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun createVehicleView(vehicleInfo: String, vehicleDetail: String) {
        val containerVehicleDetail = binding.containerVehicleObject
        //containerVehicleDetail.removeAllViews()
        val view: LayoutVehicleInfoTextBinding =
            DataBindingUtil.inflate(
                layoutInflater, R.layout.layout_vehicle_info_text,
                containerVehicleDetail, false
            ) as LayoutVehicleInfoTextBinding

        view.txtVehicleDetail.text = vehicleDetail
        view.txtVehicleInfo.text = vehicleInfo

        containerVehicleDetail.addView(view.root)
    }


}