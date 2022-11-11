package com.example.afjtracking.view.fragment.vehicle_weekly_inspection

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentWeeklyInspectionFormBinding
import com.example.afjtracking.databinding.LayoutWeeklyInspectionCheckItemBinding
import com.example.afjtracking.model.requests.SavedInspection
import com.example.afjtracking.model.requests.SavedWeeklyInspection
import com.example.afjtracking.model.requests.SingleInspectionRequest
import com.example.afjtracking.model.responses.*
import com.example.afjtracking.utils.*
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.InspectionReviewFragment
import com.example.afjtracking.view.fragment.vehicle_weekly_inspection.viewmodel.WeeklyInspectionViewModel



class WeeklyInspectionForm : Fragment() {
    private var _binding: FragmentWeeklyInspectionFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var mBaseActivity: NavigationDrawerActivity
    var inspectionTimeSpent= ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    private var checkIndex = 0
    private var inpsecitonTitle = "Inspection Completed: "
    var listChecks: ArrayList<WeeklyInspectionCheck> = arrayListOf()
    var listRadioOption: ArrayList<RadioCheckOption> = arrayListOf()
    var totalCountChecks = 0
    var inspectionID = ""
    val satisfactoryCode = "satisfactory"

    var radioOption = ""
    lateinit var weeklyInspectionViewModel: WeeklyInspectionViewModel


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

        weeklyInspectionViewModel =
            ViewModelProvider(this)[WeeklyInspectionViewModel::class.java]

        _binding = FragmentWeeklyInspectionFormBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.timerView.setListener(object:TimerListener{
            override fun getStringTime(time: String) {
                inspectionTimeSpent = time
            }

        })
        binding.timerView.getTimerVariable().stop()
        root.hideKeyboard()

        val inspectionId = arguments?.getInt(InspectionReviewFragment.argumentParams)!!
        val body = SingleInspectionRequest("$inspectionId")

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
                    weeklyInspectionViewModel.getWeeklyInspectionCheckRequest(mBaseActivity, body)
                } else {
                    binding.mainLayout.removeAllViews()
                    binding.mainLayout.addView(authView)
                }
            }

            override fun onAuthForceClose(boolean: Boolean) {
                mBaseActivity.onBackPressed()
            }
        })


        weeklyInspectionViewModel.showDialog.observe(viewLifecycleOwner) {
            mBaseActivity.showProgressDialog(it)
        }
        weeklyInspectionViewModel.apiHasData.observe(viewLifecycleOwner) {
            if (!it) {
                mBaseActivity.onBackPressed()
            }
        }

        weeklyInspectionViewModel.errorsMsg.observe(viewLifecycleOwner) {
            mBaseActivity.toast(it)
        }

        weeklyInspectionViewModel.weeklyInspectionCheck.observe(viewLifecycleOwner)
        {
            if (it != null) {
                binding.inspectionModel = weeklyInspectionViewModel
                try {
                    binding.timerView.getTimerVariable().start()
                    createViews(it)
                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }

                // weeklyInspectionViewModel._weeklyInspectionCheck.value = null
            }

        }

        weeklyInspectionViewModel.apiCompleted.observe(viewLifecycleOwner)
        {
            if (it) {
                //  mBaseActivity.toast("Inspection Successfully Completed")
                //  mBaseActivity.onBackPressed()
                CustomDialog().showTaskCompleteDialog(
                    mBaseActivity,
                    isShowTitle = true,
                    isShowMessage = true,
                    titleText = getString(R.string.request_submited),
                    msgText = getString(R.string.request_msg, "inspection form"),
                    lottieFile = R.raw.inspection_complete,
                    showOKButton = true,
                    okButttonText = "Close",
                    listner = object : DialogCustomInterface {
                        override fun onClick(var1: LottieDialog) {
                            super.onClick(var1)
                            var1.dismiss()
                            mBaseActivity.onBackPressed()
                        }
                    }
                )
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        mSensorManager!!.unregisterListener(inspectionSensor)
    }


    private fun createViews(data: WeeklyInspectionCheckData) {

        binding.progressHorizontal.max = data.totalCount!!
        totalCountChecks = data.totalCount!!
        listChecks = data.checks
        listRadioOption = data.options
        inspectionID = data.inspection!!.id.toString()


        try {
            createInspectionChecksView(listChecks[checkIndex], data)
        } catch (e: Exception) {
            mBaseActivity.writeExceptionLogs(e.toString())
        }
    }


    private fun createInspectionChecksView(
        check: WeeklyInspectionCheck,
        data: WeeklyInspectionCheckData,
    ) {

        binding.txtInpsecitonCount.text = "$inpsecitonTitle$checkIndex/${totalCountChecks}"
        binding.txtInspectionType.text = check.type
        binding.progressHorizontal.progress = checkIndex
        binding.txtInpsecitonCount.hideKeyboard()

        val containerChecks = binding.layoutContainerCheck
        containerChecks.removeAllViews()

        val view: LayoutWeeklyInspectionCheckItemBinding =
            DataBindingUtil.inflate(
                layoutInflater, R.layout.layout_weekly_inspection_check_item,
                containerChecks, false
            ) as LayoutWeeklyInspectionCheckItemBinding

        view.checkList = check


        if (checkIndex == 0) {
            view.btnPreviousCehck.visibility = View.INVISIBLE
        } else {
            view.btnPreviousCehck.visibility = View.VISIBLE
        }


        view.btnNextCheck.setOnClickListener {

            saveInspectionToList(data, check, view.edComment, false)

        }


        view.btnPreviousCehck.setOnClickListener {

            saveInspectionToList(data, check, view.edComment, true)

        }

        view.commentContainer.visibility = View.GONE
        addRadioButtons(listRadioOption, view.radioCheckGroup, check.savedInspections)


        view.radioCheckGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById(checkedId) as RadioButton
            radioOption = rb.tag.toString()

            if (radioOption != satisfactoryCode) {
                view.commentContainer.visibility = View.VISIBLE
            } else {
                view.commentContainer.visibility = View.GONE
            }


        }

        if (check.savedInspections.size > 0) {
            if (check.savedInspections[0].code != satisfactoryCode) {
                view.commentContainer.visibility = View.VISIBLE
                radioOption = check.savedInspections[0].code.toString()
                if (check.savedInspections[0].comment != null) {
                    view.edComment.setText(check.savedInspections[0].comment)
                }
            } else {
                view.commentContainer.visibility = View.GONE
                radioOption = satisfactoryCode
            }
        } else {
            radioOption = satisfactoryCode
        }


        containerChecks.addView(view.root)
    }


    private fun saveInspectionToList(
        data: WeeklyInspectionCheckData,
        check: WeeklyInspectionCheck,
        edComent: EditText, isPrevious: Boolean
    ) {


        try {
            val savedInspection = savedInspectionData(data, check, edComent)
            listChecks[checkIndex].savedInspections = arrayListOf(savedInspection)
            if (savedInspection.code != satisfactoryCode) {
                if (savedInspection.comment!!.isEmpty()) {
                    mBaseActivity.toast("Please enter comment")
                    return
                }
            }
            if (isPrevious) {
                if (checkIndex > 0) {
                    checkIndex--


                    createInspectionChecksView(listChecks[checkIndex], data)
                    AFJUtils.startOutAnimation(mBaseActivity, binding.layoutContainerCheck)
                }
            } else {
                if (checkIndex < listChecks.size - 1) {
                    checkIndex++


                    createInspectionChecksView(listChecks[checkIndex], data)
                    AFJUtils.startInAnimation(mBaseActivity, binding.layoutContainerCheck)


                } else {
                    binding.timerView.getTimerVariable().stop()
                    val body = SavedWeeklyInspection(
                        inspectionID,
                        listChecks,
                        SensorData(mAccelerometerData, mGyroSensorData, mLinearSensorData),
                        inspectionTimeSpent
                    )
                    weeklyInspectionViewModel.saveWeeklyInspectionChecks(mBaseActivity, body, false)
                }
            }
        } catch (e: Exception) {
            mBaseActivity.writeExceptionLogs(e.toString())
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun addRadioButtons(
        radioButtonTexts: ArrayList<RadioCheckOption>,
        rg: RadioGroup,
        savedInspection: ArrayList<SavedInspection>
    ) {
        for (text in radioButtonTexts) {
            val newRadioButton = RadioButton(context)


            newRadioButton.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            newRadioButton.text = text.value
            newRadioButton.tag = text.id


            if (Build.VERSION.SDK_INT < 21) {
                CompoundButtonCompat.setButtonTintList(
                    newRadioButton,
                    ColorStateList.valueOf(R.color.colorPrimary)
                )
            } else {
                newRadioButton.buttonTintList =
                    ColorStateList.valueOf(R.color.colorPrimary)
            }

            rg.addView(newRadioButton)

            if (savedInspection.size > 0) {
                if (text.id == savedInspection[0].code) {
                    rg.check(newRadioButton.id)
                }
            } else {
                if (text.id == satisfactoryCode) {
                    rg.check(newRadioButton.id)
                }
            }


        }
    }


    private fun savedInspectionData(
        data: WeeklyInspectionCheckData,
        check: WeeklyInspectionCheck,
        edComent: EditText
    ): SavedInspection {
        return SavedInspection(
            vehicleInspectionId = data.inspection!!.id.toString(),
            checkNo = check.checkNo.toString(),
            name = check.name,
            type = check.type,
            code = radioOption,
            comment = edComent.text.toString()
        )
    }


    override fun onDestroy() {
        super.onDestroy()

        val body = SavedWeeklyInspection(
            inspectionID,
            listChecks,
            SensorData(mAccelerometerData, mGyroSensorData, mLinearSensorData),
            inspectionTimeSpent
        )
        weeklyInspectionViewModel.saveWeeklyInspectionChecks(mBaseActivity, body, true)
    }


}