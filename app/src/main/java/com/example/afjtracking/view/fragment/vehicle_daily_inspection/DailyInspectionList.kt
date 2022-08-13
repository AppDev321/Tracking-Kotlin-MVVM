package com.example.afjtracking.view.fragment.vehicle_daily_inspection

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
import com.example.afjtracking.model.responses.Inspections
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.DailyInspectionAdapter
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel.DailyInspectionViewModel

class DailyInspectionList : Fragment() {

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

        val dailyInspectionViewModel = ViewModelProvider(this).get(DailyInspectionViewModel::class.java)

        _binding = FragmentWeeklyInspectionListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.btnAddInspection.visibility = View.GONE

        root.hideKeyboard()

        dailyInspectionViewModel.showDialog.observe(viewLifecycleOwner) {
            mBaseActivity.showProgressDialog(it)
        }

        dailyInspectionViewModel.getDailyInspectionList(mBaseActivity)

        dailyInspectionViewModel.getInspectionList.observe(viewLifecycleOwner) {
            if (it != null) {
                try {
                    if (it.size > 0) {
                        val adapter = DailyInspectionAdapter(mBaseActivity, it)
                        adapter.setListnerClick(object :
                            DailyInspectionAdapter.ClickWeeklyInspectionListner {
                            override fun handleContinueButtonClick(data: Inspections) {
                                val bundle = bundleOf(InspectionReviewFragment.argumentParams to data.id)

                                mBaseActivity.moveFragmentToNextFragment(
                                    binding.root,
                                    R.id.nav_daily_inspection_review, bundle
                                )
                            }
                        })

                        val layoutManager = LinearLayoutManager(mBaseActivity)
                        binding.recWeeklyInspectionList.layoutManager = layoutManager
                        binding.recWeeklyInspectionList.adapter = adapter

                        binding.txtNoData.visibility = View.GONE
                        binding.recWeeklyInspectionList.visibility = View.VISIBLE

                    }
                    else
                    {
                        binding.txtNoData.visibility = View.VISIBLE
                        binding.recWeeklyInspectionList.visibility = View.GONE
                    }

                    binding.btnAddInspection.visibility = View.VISIBLE

                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
            }
        }

        dailyInspectionViewModel.getVehicleInfo.observe(viewLifecycleOwner)
        {
            try {
                if (it != null) {
                    binding.txtInspectionTitle.text =
                        "Daily Inspection | ${it.vrn} (${it.detail!!.vehicleType})"
                }
            } catch (e: Exception) {
                mBaseActivity.writeExceptionLogs(e.toString())
            }
        }

        dailyInspectionViewModel.errorsMsg.observe(mBaseActivity) {
            if(it != null) {
                mBaseActivity.toast(it)
                dailyInspectionViewModel.errorsMsg.value = null
            }

        }

        binding.btnAddInspection.setOnClickListener {
            mBaseActivity.moveFragmentToNextFragment(
                binding.root,
                R.id.nav_vdi_inspection_create
            )
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}