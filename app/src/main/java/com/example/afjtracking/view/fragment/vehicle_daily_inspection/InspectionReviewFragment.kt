package com.example.afjtracking.view.fragment.vehicle_daily_inspection


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentDailyInpsectionFormBinding
import com.example.afjtracking.databinding.ItemDailyPtsInspectionCheckBinding
import com.example.afjtracking.model.responses.Checks
import com.example.afjtracking.model.responses.InspectionReviewData
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.utils.InputFilterMinMax
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView

import com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel.DailyInspectionViewModel


class InspectionReviewFragment : Fragment() {

    private var _binding: FragmentDailyInpsectionFormBinding? = null
    private val binding get() = _binding!!

    private var mInspectionChecks: ArrayList<Checks> = arrayListOf()

    private lateinit var mBaseActivity: NavigationDrawerActivity
    var checkIndex: Int = 0
    var inpsectionTypeIndex = 0
    var previousCounter = 0
    var isPreviousClicked= false

    private lateinit var apiRequestParams: InspectionReviewData
    companion object
    {
        val argumentParams ="inspection_id"
    }

    var totalChecksCount = 0
    var inpsecitonTitle = "Inspection Completed: "

    lateinit var dailyInspectionViewModel : DailyInspectionViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        dailyInspectionViewModel= ViewModelProvider(this)[DailyInspectionViewModel::class.java]
        _binding = FragmentDailyInpsectionFormBinding.inflate(inflater, container, false)
        val root: View = binding.root
        root.hideKeyboard()
        try {
            val inspectionID= arguments?.getInt(argumentParams)!!

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
                        dailyInspectionViewModel.getDailyInspectionReview(mBaseActivity,inspectionID)
                    } else {
                        binding.mainLayout.removeAllViews()
                        binding.mainLayout.addView(authView)
                    }
                }
                override fun onAuthForceClose(boolean: Boolean) {
                    mBaseActivity.onBackPressed()
                }
            })



            binding.containerInspection.visibility = View.VISIBLE
            binding.containerCheck.visibility = View.GONE




            dailyInspectionViewModel.showDialog.observe(mBaseActivity) {
                mBaseActivity.showProgressDialog(it)
            }



            dailyInspectionViewModel.errorsMsg.observe(viewLifecycleOwner) {
                if (it != null) {
                    mBaseActivity.showProgressDialog(false)
                    mBaseActivity.toast(it)
                }
                dailyInspectionViewModel.errorsMsg.value = null
            }

            dailyInspectionViewModel.getInspectionReviewData.observe(viewLifecycleOwner) {
                if (it != null) {


                    dailyInspectionViewModel._vehicleInfo.value = it.vehicle
                    binding.inspectionModel = dailyInspectionViewModel

                    mInspectionChecks = it.checks
                    apiRequestParams = it

                    createPSTInspectionView(mInspectionChecks[checkIndex])

                    //Save count check
                    for (i in 0 until mInspectionChecks.size) {
                            totalChecksCount++
                    }

                    binding.progressHorizontal.max = totalChecksCount
                    setTitleValue()
                    dailyInspectionViewModel._inspectionReviewData.value = null
                }


            }

            dailyInspectionViewModel.apiUploadStatus.observe(viewLifecycleOwner)
            {
              mBaseActivity.showProgressDialog(false)
                if (it) {
                 //   mBaseActivity.closeFragment(this)
                    mBaseActivity.toast("Inspection Completed")
                    mBaseActivity.onBackPressed()
                    dailyInspectionViewModel._dataUploaded.value =false
                }

            }

        }
        catch (e: Exception)
        {
            mBaseActivity.writeExceptionLogs(e.toString())
        }

        return root
    }

    fun setTitleValue() {
        binding.txtInpsecitonCount.text = "$inpsecitonTitle$previousCounter/$totalChecksCount"
        binding.progressHorizontal.progress = previousCounter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun createPSTInspectionView(check: Checks) {
        val containerChecks = binding.layoutContainerCheck
        containerChecks.hideKeyboard()
        containerChecks.removeAllViews()
        val view: ItemDailyPtsInspectionCheckBinding =
            DataBindingUtil.inflate(
                layoutInflater, R.layout.item_daily_pts_inspection_check,
                containerChecks, false
            ) as ItemDailyPtsInspectionCheckBinding


        view.checkList = check

        if (previousCounter == 0) {
            view.btnPreviousCehck.visibility = View.INVISIBLE
        } else {
            view.btnPreviousCehck.visibility = View.VISIBLE
        }

        if(check.type!! == "quantity")
        {
            view.edQuantityVehicle.filters = arrayOf(InputFilterMinMax("0", check.data!!))
        }

        view.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {
                    view.containerIssueFound.visibility = View.VISIBLE
                    view.containerWornRefit.visibility = View.GONE
                }
                else{
                    view.containerIssueFound.visibility = View.GONE
                    view.containerWornRefit.visibility = View.GONE
                }
        }

        view.issueCheck.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {

                view.containerWornRefit.visibility = View.VISIBLE
            }
            else{
                view.containerWornRefit.visibility = View.GONE
                view.edWorn.text.clear()
               // solvedInspection!!.wornRefit = view.edWorn.text.toString()
            }
        }



        view.btnNextCheck.setOnClickListener {

            val solvedInspection = view.checkList!!.savedInspection

            solvedInspection!!.checked  =  view.checkbox.isChecked
            solvedInspection.issueCheck = view.issueCheck.isChecked

            //***** Saved Inspection Data*******
            solvedInspection.wornRefit = view.edWorn.text.toString()

            if(solvedInspection.issueCheck == true && solvedInspection.wornRefit!!.isEmpty())
            {
                 mBaseActivity.showSnackMessage("Please enter details of issue",binding.root)
            }
            else {

                solvedInspection.fleetNo = view.edFleetId.text.toString()
                solvedInspection.quantity = view.edQuantity.text.toString()
                solvedInspection.quantityOnVehicle = view.edQuantityVehicle.text.toString()
                if (check.type!!.contains("quantity")) {
                    solvedInspection.quantityRequired = check.data
                }

                check.savedInspection = solvedInspection
                mInspectionChecks[checkIndex] = check

                //********************************

                if (checkIndex < mInspectionChecks.size - 1) {
                    checkIndex++
                    previousCounter++
                    setTitleValue()
                    createPSTInspectionView(mInspectionChecks[checkIndex])
                    AFJUtils.startInAnimation(mBaseActivity, binding.layoutContainerCheck)
                } else {


                        apiRequestParams.checks = mInspectionChecks
                        dailyInspectionViewModel.postChecksInspection(mBaseActivity, apiRequestParams)


                }
            }


        }



        view.btnPreviousCehck.setOnClickListener {

            if (previousCounter > 0) {
                previousCounter--
                setTitleValue()
            }

            if (checkIndex > 0) {
                checkIndex--
                createPSTInspectionView(mInspectionChecks[checkIndex])
                AFJUtils.startOutAnimation(mBaseActivity,binding.layoutContainerCheck)

            }
        }
        containerChecks.addView(view.root)
    }


}


