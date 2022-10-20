package com.example.afjtracking.view.fragment.vehicle_daily_inspection


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentDailyInpsectionFormBinding
import com.example.afjtracking.databinding.ItemDailyPsvInspectionCheckBinding
import com.example.afjtracking.model.responses.InspectionCheckData
import com.example.afjtracking.model.responses.PSVCheck
import com.example.afjtracking.model.responses.SensorData
import com.example.afjtracking.utils.*
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel.DailyInspectionViewModel
import kotlinx.android.synthetic.main.fragment_daily_inpsection_form.view.*


class PSVInspectionForm : Fragment() {

    private var _binding: FragmentDailyInpsectionFormBinding? = null
    private val binding get() = _binding!!




    private lateinit var mBaseActivity: NavigationDrawerActivity
    var checkIndex: Int = 0


    private lateinit var apiRequestParams: InspectionCheckData

    companion object {
        val argumentParams = "form_data"
    }

    var totalChecksCount = 0
    var inpsecitonTitle = "Inspection Completed: "

    lateinit var inspectionViewModel: DailyInspectionViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }




    private var mSensorManager: SensorManager? = null
    private var mAccelerometerData: MutableList<FloatArray> = arrayListOf()
    private var mGyroSensorData: MutableList<FloatArray> = arrayListOf()
    private var mLinearSensorData: MutableList<FloatArray> = arrayListOf()
    private var mInspectionTypeList: ArrayList<PSVCheck> = arrayListOf()

    private val inspectionSensor = object : InspectionSensor() {

        override fun sendSensorValue(data: ArrayList<FloatArray>) {
            mAccelerometerData.add(data[0])
            mGyroSensorData.add(data[1])
            mLinearSensorData.add(data[2])
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSensorManager = mBaseActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            mSensorManager!!.registerListener(
                inspectionSensor,
                accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        mSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also { gyroscope ->
            mSensorManager!!.registerListener(
                inspectionSensor,
                gyroscope,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also { linear ->
            mSensorManager!!.registerListener(
                inspectionSensor,
                linear,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        inspectionViewModel = ViewModelProvider(this)[DailyInspectionViewModel::class.java]
        _binding = FragmentDailyInpsectionFormBinding.inflate(inflater, container, false)
        val root: View = binding.root
        root.hideKeyboard()
        root.container_check.visibility = View.GONE

        try {


            val inspectionData: InspectionCheckData =
                arguments?.getParcelable(PTSInspectionForm.argumentParams)!!

            apiRequestParams = inspectionData
            mInspectionTypeList = inspectionData.psvChecks

            inspectionViewModel._inspectionData.value = inspectionData
            inspectionViewModel._vehicleInfo.value = inspectionData.vehicle

            binding.inspectionModel = inspectionViewModel


            binding.timerView.setListener(object:TimerListener{

                override fun getStringTime(time: String) {
                    apiRequestParams.inspectionTimeSpent = time
                }

            })


            //Save count check
            for (i in 0 until mInspectionTypeList.size) {

                totalChecksCount++

            }


            createPSVInspectionView(mInspectionTypeList[checkIndex])

            binding.progressHorizontal.max = totalChecksCount
            setTitleValue()
        } catch (e: Exception) {
            mBaseActivity.writeExceptionLogs(e.toString())
        }


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


        inspectionViewModel.apiUploadStatus.observe(viewLifecycleOwner)
        {
            mBaseActivity.showProgressDialog(false)
            if (it) {
                if(isAdded) {
                  //  mBaseActivity.closeFragment(this)

                   // mBaseActivity.toast("Inspection Completed")
                  //  mBaseActivity.onBackPressed()
                    CustomDialog().showTaskCompleteDialog(
                        mBaseActivity,
                        isShowTitle =  true,
                        isShowMessage = true,
                        titleText=getString(R.string.request_inspection_completed),
                        msgText =getString(R.string.request_msg,"inspection form"),
                        lottieFile = R.raw.inspection_complete,
                        showOKButton = true,
                        okButttonText = "Close",
                        listner =object: DialogCustomInterface {
                            override fun onClick(var1: LottieDialog) {
                                super.onClick(var1)
                                var1.dismiss()
                                mBaseActivity.onBackPressed()
                            }
                        }
                    )
                }
                inspectionViewModel._dataUploaded.value =false

            }
        }



        return root
    }

    fun setTitleValue() {
        binding.txtInpsecitonCount.text = "$inpsecitonTitle$checkIndex/$totalChecksCount"
        binding.progressHorizontal.progress = checkIndex
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mSensorManager!!.unregisterListener(inspectionSensor)
    }


    fun createPSVInspectionView(check: PSVCheck) {
        val containerChecks = binding.layoutContainerCheck
        containerChecks.hideKeyboard()
        containerChecks.removeAllViews()
        val view: ItemDailyPsvInspectionCheckBinding =
            DataBindingUtil.inflate(
                layoutInflater, R.layout.item_daily_psv_inspection_check,
                containerChecks, false
            ) as ItemDailyPsvInspectionCheckBinding



        view.checkList = check
        if (checkIndex == 0) {
            view.btnPreviousCehck.visibility = View.INVISIBLE
        } else {
            view.btnPreviousCehck.visibility = View.VISIBLE
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
            //***** Saved Inspection Data*******
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
                check.savedInspection = solvedInspection
                mInspectionTypeList[checkIndex] = check
                //********************************
                if (checkIndex < mInspectionTypeList.size - 1) {
                    checkIndex++
                    setTitleValue()
                    createPSVInspectionView(mInspectionTypeList[checkIndex])
                    AFJUtils.startInAnimation(mBaseActivity, binding.layoutContainerCheck)

                } else {
                    mBaseActivity.showProgressDialog(true)
                    apiRequestParams.psvChecks = mInspectionTypeList
                    apiRequestParams.sensorData = SensorData(mAccelerometerData,mGyroSensorData,mLinearSensorData)

                    inspectionViewModel.postInspectionVDI(mBaseActivity, apiRequestParams)
                }
            }
        }

        view.btnPreviousCehck.setOnClickListener {

            if (checkIndex > 0) {
                checkIndex--
                createPSVInspectionView(mInspectionTypeList[checkIndex])
                setTitleValue()

                AFJUtils.startOutAnimation(mBaseActivity,binding.layoutContainerCheck)

            }
        }
        containerChecks.addView(view.root)
    }


}


