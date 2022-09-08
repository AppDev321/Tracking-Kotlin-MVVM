package com.example.afjtracking.view.fragment.vehicle_weekly_inspection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentWeeklyInspectionListBinding
import com.example.afjtracking.model.requests.WeeklyVehicleInspectionRequest
import com.example.afjtracking.model.responses.VehicleDetail
import com.example.afjtracking.model.responses.WeeklyInspectionData
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.WeeklyInspectionAdapter
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.PTSInspectionForm
import com.example.afjtracking.view.fragment.vehicle_weekly_inspection.viewmodel.WeeklyInspectionViewModel

class WeeklyInspectionList : Fragment() {

    private var _binding: FragmentWeeklyInspectionListBinding? = null
    private val binding get() = _binding!!
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

        val weeklyInspectionViewModel =
            ViewModelProvider(this).get(WeeklyInspectionViewModel::class.java)

        _binding = FragmentWeeklyInspectionListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.btnAddInspection.visibility = View.GONE

        root.hideKeyboard()
        weeklyInspectionViewModel.showDialog.observe(mBaseActivity) {
            mBaseActivity.showProgressDialog(it)
        }

        //Save vehicle object
        val vehicleDetail = AFJUtils.getObjectPref(
            mBaseActivity,
            AFJUtils.KEY_VEHICLE_DETAIL,
            VehicleDetail::class.java
        )
        val body = WeeklyVehicleInspectionRequest(vehicleDetail.id.toString(), deviceDetail = AFJUtils.getDeviceDetail())
        weeklyInspectionViewModel.getWeeklyVehicleInspectionCheckList(mBaseActivity, body)


        weeklyInspectionViewModel.vehicleData.observe(viewLifecycleOwner) {

            if (it != null) {
                try {
                    if (it.inspections.size > 0) {

                        val adapter = WeeklyInspectionAdapter(mBaseActivity, it.inspections)
                        adapter.setListnerClick(object :
                            WeeklyInspectionAdapter.ClickWeeklyInspectionListner {
                            override fun handleContinueButtonClick(data: WeeklyInspectionData) {


                                val bundle = bundleOf(PTSInspectionForm.argumentParams to data.id)
                                mBaseActivity.moveFragmentToNextFragment(
                                    binding.root,
                                    R.id.nav_weekly_inspection_form, bundle
                                )
                            }

                        })
                        val layoutManager = LinearLayoutManager(mBaseActivity)
                        binding.recWeeklyInspectionList.layoutManager = layoutManager
                        binding.recWeeklyInspectionList.adapter = adapter

                        binding.txtNoData.visibility = View.GONE
                        binding.recWeeklyInspectionList.visibility = View.VISIBLE
                    } else {
                        binding.txtNoData.visibility = View.VISIBLE
                        binding.recWeeklyInspectionList.visibility = View.GONE
                    }
                    binding.txtInspectionTitle.text =
                        "Inspection | ${it.vrn} (${it.detail!!.vehicleType})"
                    binding.btnAddInspection.visibility = View.VISIBLE

                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
            }
        }

        weeklyInspectionViewModel.errorsMsg.observe(mBaseActivity)
        {
            mBaseActivity.toast(it)
        }
        binding.btnAddInspection.setOnClickListener {
            mBaseActivity.moveFragmentToNextFragment(
                binding.root,
                R.id.nav_create_weekly_inspection
            )
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}