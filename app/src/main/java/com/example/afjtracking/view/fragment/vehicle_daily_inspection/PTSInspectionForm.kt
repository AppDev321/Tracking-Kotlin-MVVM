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
import com.example.afjtracking.model.responses.InspectionCheckData
import com.example.afjtracking.model.responses.PTSCheck
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.utils.InputFilterMinMax
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel.DailyInspectionViewModel


class PTSInspectionForm : Fragment() {

    private var _binding: FragmentDailyInpsectionFormBinding? = null
    private val binding get() = _binding!!

    private var mInspectionChecks: ArrayList<Checks> = arrayListOf()
    private var mInspectionTypeList: ArrayList<PTSCheck> = arrayListOf()
    private lateinit var mBaseActivity: NavigationDrawerActivity
    var checkIndex: Int = 0
    var inpsectionTypeIndex = 0
    var previousCounter = 0
    var isPreviousClicked= false

    private lateinit var apiRequestParams: InspectionCheckData
    companion object
    {
        val argumentParams ="form_data"
    }

    var totalChecksCount = 0
    var inpsecitonTitle = "Inspection Completed: "

    lateinit var inspectionViewModel : DailyInspectionViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        inspectionViewModel=  ViewModelProvider(this).get(DailyInspectionViewModel::class.java)

        _binding = FragmentDailyInpsectionFormBinding.inflate(inflater, container, false)


        val root: View = binding.root
        root.hideKeyboard()
        try {

            val inpsecitonData: InspectionCheckData = arguments?.getParcelable(argumentParams)!!


            binding.containerInspection.visibility = View.VISIBLE
            binding.containerCheck.visibility = View.VISIBLE
            inspectionViewModel._inspectionData.value = inpsecitonData
            inspectionViewModel._vehicleInfo.value = inpsecitonData.vehicle
            inspectionViewModel._vehicleClassChecks.value = inpsecitonData.ptsChecks
            binding.inspectionModel = inspectionViewModel
            apiRequestParams = inpsecitonData



            inspectionViewModel.inspectionChecksData.observe(viewLifecycleOwner) {

                if (it?.isCompleted == true) {
                    mBaseActivity.closeFragment(this)

                }
            }



            inspectionViewModel.errorsMsg.observe(viewLifecycleOwner) {
                if (it != null) {
                    mBaseActivity.showProgressDialog(false)
                    mBaseActivity.toast(it)
                }
            }

            inspectionViewModel.getInspcetionClassList.observe(viewLifecycleOwner) {
                if (it != null) {
                    mInspectionTypeList = it

                    binding.txtInspectionType.text = mInspectionTypeList[inpsectionTypeIndex].title
                    mInspectionChecks = mInspectionTypeList[inpsectionTypeIndex].checks
                    createPSTInspectionView(mInspectionChecks[checkIndex])


                    //Save count check
                    for (i in 0 until mInspectionTypeList.size) {
                        for (z in 0 until mInspectionTypeList[i].checks.size) {
                            totalChecksCount++
                        }
                    }

                    binding.progressHorizontal.max = totalChecksCount
                    setTitleValue()
                }
            }

            inspectionViewModel.apiUploadStatus.observe(viewLifecycleOwner)
            {
                mBaseActivity.showProgressDialog(false)
                if (it) {
                 //   mBaseActivity.closeFragment(this)
                    mBaseActivity.toast("Inspection Completed")
                    mBaseActivity.onBackPressed()
                    inspectionViewModel._dataUploaded.value = false
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
            view.edQuantityVehicle.filters = arrayOf(InputFilterMinMax("0", check.message!!))
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
                    solvedInspection.quantityRequired = check.message
                }


                check.savedInspection = solvedInspection
                mInspectionChecks[checkIndex] = check
                mInspectionTypeList[inpsectionTypeIndex].checks = mInspectionChecks
                //********************************


                if (checkIndex < mInspectionChecks.size - 1) {
                    checkIndex++
                    previousCounter++

                    setTitleValue()
                    createPSTInspectionView(mInspectionChecks[checkIndex])
                    AFJUtils.startInAnimation(mBaseActivity, binding.layoutContainerCheck)


                } else {
                    if (inpsectionTypeIndex < mInspectionTypeList.size - 1) {
                        inpsectionTypeIndex++
                        previousCounter++
                        setTitleValue()
                        binding.txtInspectionType.text =
                            mInspectionTypeList[inpsectionTypeIndex].title
                        mInspectionChecks = mInspectionTypeList[inpsectionTypeIndex].checks
                        checkIndex = 0
                        createPSTInspectionView(mInspectionChecks[checkIndex])

                        AFJUtils.startInAnimation(mBaseActivity, binding.layoutContainerCheck)


                    } else {
                        mBaseActivity.showProgressDialog(true)
                        apiRequestParams.ptsChecks = mInspectionTypeList

                        inspectionViewModel.postInspectionVDI(mBaseActivity, apiRequestParams)

                    }
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

            } else {
                if (inpsectionTypeIndex > 0) {
                    inpsectionTypeIndex--
                    binding.txtInspectionType.text = mInspectionTypeList[inpsectionTypeIndex].title
                    mInspectionChecks = mInspectionTypeList[inpsectionTypeIndex].checks
                    checkIndex = mInspectionChecks.size - 1
                    createPSTInspectionView(mInspectionChecks[checkIndex])
                    AFJUtils.startOutAnimation(mBaseActivity,binding.layoutContainerCheck)
                }
            }
        }
        containerChecks.addView(view.root)
    }


}


