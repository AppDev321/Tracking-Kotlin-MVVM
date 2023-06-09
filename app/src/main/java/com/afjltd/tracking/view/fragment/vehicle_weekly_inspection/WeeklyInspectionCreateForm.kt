package com.afjltd.tracking.view.fragment.vehicle_weekly_inspection

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.afjltd.tracking.model.requests.InspectionCreateRequest
import com.afjltd.tracking.model.responses.VehicleDetail
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.AFJUtils.hideKeyboard
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.view.fragment.auth.CustomAuthenticationView
import com.afjltd.tracking.view.fragment.vehicle_weekly_inspection.viewmodel.WeeklyInspectionViewModel
import com.afjltd.tracking.databinding.FragmentWeeklyCreateInspectionBinding
import java.util.*


class WeeklyInspectionCreateForm : Fragment() {
    private var _binding: FragmentWeeklyCreateInspectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var mBaseActivity: NavigationDrawerActivity
    var year = 0
    var month = 0
    var day = 0

    lateinit var calendar: Calendar
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

        _binding = FragmentWeeklyCreateInspectionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        root.hideKeyboard()

        binding.baseLayout.visibility = View.GONE
        val authView = CustomAuthenticationView(requireContext())
        binding.mainLayout.addView(authView)
        authView.addAuthListener(object : CustomAuthenticationView.AuthListeners {
            override fun onAuthCompletionListener(boolean: Boolean) {
                if (_binding == null)
                    return
                if (boolean) {
                    binding.mainLayout.removeAllViews()
                    binding.mainLayout.addView(binding.baseLayout)
                    binding.baseLayout.visibility = View.VISIBLE
                } else {
                    binding.mainLayout.removeAllViews()
                    binding.mainLayout.addView(authView)
                }
            }

            override fun onAuthForceClose(boolean: Boolean) {
                mBaseActivity.pressBackButton()

            }
        })



        binding.btnCancel.setOnClickListener {
            mBaseActivity.pressBackButton()

        }

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)

        month = calendar.get(Calendar.MONTH)
        day = calendar.get(Calendar.DAY_OF_MONTH)
        showDate(year, month + 1, day)

        binding.btnDatePicker.setOnClickListener {

            DatePickerDialog(
                mBaseActivity,
                myDateListener, year, month, day
            ).show()

        }

        weeklyInspectionViewModel.showDialog.observe(mBaseActivity) {
            mBaseActivity.showProgressDialog(it)
        }
        weeklyInspectionViewModel.apiCompleted.observe(mBaseActivity) {
            if (it) {
                mBaseActivity.toast("Inspection Created Successfully")
                mBaseActivity.pressBackButton()

            }
        }

        weeklyInspectionViewModel.errorsMsg.observe(mBaseActivity) {
            mBaseActivity.toast(it)
        }


        binding.btnSave.setOnClickListener {

            val vehicleDetail =
                AFJUtils.getObjectPref(
                    mBaseActivity,
                    AFJUtils.KEY_VEHICLE_DETAIL,
                    VehicleDetail::class.java )

            val body = InspectionCreateRequest(
                vehicleId = vehicleDetail.id.toString(),
                type = binding.spInspectionType.selectedItem.toString(),
                date = binding.txtDate.text.toString(),
                odoMeterReading = binding.edOdoReading.text.toString()
            )

            weeklyInspectionViewModel.createWeeklyInspectionRequest(mBaseActivity, body)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val myDateListener =
        OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            showDate(arg1, arg2 + 1, arg3)
        }

    private fun showDate(year: Int, month: Int, day: Int) {
        binding.txtDate.text = StringBuilder().append(year).append("-")
            .append(month).append("-").append(day)
    }
}