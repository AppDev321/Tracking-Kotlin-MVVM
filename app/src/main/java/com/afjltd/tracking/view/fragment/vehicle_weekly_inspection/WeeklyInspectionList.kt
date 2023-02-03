package com.afjltd.tracking.view.fragment.vehicle_weekly_inspection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afjltd.tracking.model.requests.WeeklyVehicleInspectionRequest
import com.afjltd.tracking.model.responses.VehicleDetail
import com.afjltd.tracking.model.responses.WeeklyInspectionData
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.AFJUtils.hideKeyboard
import com.afjltd.tracking.utils.PaginatedAdapter.OnPaginationListener
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.view.adapter.ClickListenerInterface
import com.afjltd.tracking.view.adapter.WeeklyInspectionAdapter
import com.afjltd.tracking.view.fragment.vehicle_daily_inspection.InspectionReviewFragment
import com.afjltd.tracking.view.fragment.vehicle_weekly_inspection.viewmodel.WeeklyInspectionViewModel
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.FragmentWeeklyInspectionListBinding


class WeeklyInspectionList : Fragment() {

    private var _binding: FragmentWeeklyInspectionListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mBaseActivity: NavigationDrawerActivity
    var loadMoreApi = false

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
            ViewModelProvider(this)[WeeklyInspectionViewModel::class.java]

        _binding = FragmentWeeklyInspectionListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.btnAddInspection.visibility = View.GONE

        root.hideKeyboard()
        weeklyInspectionViewModel.showDialog.observe(mBaseActivity) {
            if(!loadMoreApi)
                mBaseActivity.showProgressDialog(it)
        }

        //Save vehicle object
        val vehicleDetail = AFJUtils.getObjectPref(
            mBaseActivity,
            AFJUtils.KEY_VEHICLE_DETAIL,
            VehicleDetail::class.java
        )


        val adapter = WeeklyInspectionAdapter(mBaseActivity)
        adapter.setPageSize(10)
        adapter.setDefaultRecyclerView(mBaseActivity, binding.recWeeklyInspectionList)
        adapter.setListnerClick(object : ClickListenerInterface {
            override fun <T> handleContinueButtonClick(data: T) {
                val dataInspection = data as WeeklyInspectionData
                val bundle =   bundleOf(InspectionReviewFragment.argumentParams to dataInspection.id)
                mBaseActivity.moveFragmentToNextFragment( binding.root,   R.id.nav_weekly_inspection_form, bundle  )
            }
        })

        adapter.setOnPaginationListener(object : OnPaginationListener {
            override fun onCurrentPage(page: Int) {
            }
            override fun onNextPage(page: Int) {
                loadMoreApi = true
                adapter.addLoadingFooter(WeeklyInspectionData())
                val body = WeeklyVehicleInspectionRequest(
                    page,
                    adapter.getPageSize(),
                    vehicleDetail.id.toString(),
                    deviceDetail = AFJUtils.getDeviceDetail()
                )
                weeklyInspectionViewModel.getWeeklyVehicleInspectionCheckList(mBaseActivity, body)

            }

            override fun onFinish() {
              //      mBaseActivity.showSnackMessage("Reached to end",binding.root)
            }
        })

        val body = WeeklyVehicleInspectionRequest(
            adapter.getStartPage(),
            adapter.getPageSize(),
            vehicleDetail.id.toString(),
            deviceDetail = AFJUtils.getDeviceDetail()
        )
        weeklyInspectionViewModel.getWeeklyVehicleInspectionCheckList(mBaseActivity, body)


        weeklyInspectionViewModel.vehicleData.observe(viewLifecycleOwner) {

            if (it != null) {

                try {
                    if (it.inspections.isNotEmpty()) {

                        binding.txtNoData.visibility = View.GONE
                        binding.recWeeklyInspectionList.visibility = View.VISIBLE

                    } else {
                        if(!loadMoreApi) {
                            binding.txtNoData.visibility = View.VISIBLE
                            binding.recWeeklyInspectionList.visibility = View.GONE
                        }
                    }
                    binding.txtInspectionTitle.text =
                        "Inspection | ${it.vrn} (${it.detail!!.vehicleType})"
                    binding.btnAddInspection.visibility = View.VISIBLE


                    //Addimg loading more data in adapter
                    adapter.removeLoadingFooter()
                    adapter.submitItems(it.inspections)



                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
            }
        }

        weeklyInspectionViewModel.errorsMsg.observe(mBaseActivity)
        {
            mBaseActivity.toast(it)
            binding.txtNoData.visibility = View.VISIBLE
            binding.recWeeklyInspectionList.visibility = View.GONE

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