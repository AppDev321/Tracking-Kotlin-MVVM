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
import com.example.afjtracking.model.requests.DailyInspectionListRequest
import com.example.afjtracking.model.requests.WeeklyVehicleInspectionRequest
import com.example.afjtracking.model.responses.InspectionCheckData
import com.example.afjtracking.model.responses.Inspections
import com.example.afjtracking.model.responses.WeeklyInspectionData
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.utils.PaginatedAdapter
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.DailyInspectionAdapter
import com.example.afjtracking.view.adapter.WeeklyInspectionAdapter
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel.DailyInspectionViewModel

class DailyInspectionList : Fragment() {

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

        val dailyInspectionViewModel =
            ViewModelProvider(this).get(DailyInspectionViewModel::class.java)

        _binding = FragmentWeeklyInspectionListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.btnAddInspection.visibility = View.GONE
        binding.baseLayout.visibility = View.VISIBLE
        root.hideKeyboard()





        dailyInspectionViewModel.showDialog.observe(viewLifecycleOwner) {
            if(!loadMoreApi)
            mBaseActivity.showProgressDialog(it)
        }

        val adapter = DailyInspectionAdapter(mBaseActivity)
        adapter.setPageSize(10)
        adapter.setDefaultRecyclerView(mBaseActivity, binding.recWeeklyInspectionList)
        adapter.setListenerClick(object : WeeklyInspectionAdapter.ClickWeeklyInspectionListener{


            override fun <T> handleContinueButtonClick(data: T) {
                val data = data as Inspections
                val bundle =
                    bundleOf(InspectionReviewFragment.argumentParams to data.id)

                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_daily_inspection_review, bundle
                )
            }
        })

        adapter.setOnPaginationListener(object : PaginatedAdapter.OnPaginationListener {
            override fun onCurrentPage(page: Int) {

            }

            override fun onNextPage(page: Int) {
                loadMoreApi = true
                adapter.addLoadingFooter(Inspections())
                val body = DailyInspectionListRequest(
                    page,
                    adapter.getPageSize(),
                   AFJUtils.getDeviceDetail()
                )
                dailyInspectionViewModel.getDailyInspectionList(mBaseActivity,body)

            }

            override fun onFinish() {
              //  mBaseActivity.showSnackMessage("Reached to end",binding.root)
            }
        })

        val body = DailyInspectionListRequest(
            adapter.getStartPage(),
            adapter.getPageSize(),
            AFJUtils.getDeviceDetail()
        )
        dailyInspectionViewModel.getDailyInspectionList(mBaseActivity,body)

        dailyInspectionViewModel.getInspectionList.observe(viewLifecycleOwner) {
            if (it != null) {
                try {
                    if (it.isNotEmpty()) {

                        binding.txtNoData.visibility = View.GONE
                        binding.recWeeklyInspectionList.visibility = View.VISIBLE

                    } else {
                        if(!loadMoreApi) {
                            binding.txtNoData.visibility = View.VISIBLE
                            binding.recWeeklyInspectionList.visibility = View.GONE
                        }
                    }

                    binding.btnAddInspection.visibility = View.VISIBLE

                    //Adding loading more data in adapter
                    adapter.removeLoadingFooter()
                    adapter.submitItems(it)


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
            if (it != null) {
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