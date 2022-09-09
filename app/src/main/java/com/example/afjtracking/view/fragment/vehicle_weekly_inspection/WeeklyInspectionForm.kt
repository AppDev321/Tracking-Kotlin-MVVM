package com.example.afjtracking.view.fragment.vehicle_weekly_inspection

import android.content.Context
import android.content.res.ColorStateList
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
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentWeeklyInspectionFormBinding
import com.example.afjtracking.databinding.LayoutWeeklyInspectionCheckItemBinding
import com.example.afjtracking.model.requests.SavedInspection
import com.example.afjtracking.model.requests.SavedWeeklyInspection
import com.example.afjtracking.model.requests.SingleInspectionRequest
import com.example.afjtracking.model.responses.RadioCheckOption
import com.example.afjtracking.model.responses.WeeklyInspectionCheck
import com.example.afjtracking.model.responses.WeeklyInspectionCheckData
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.AFJUtils.hideKeyboard
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.PTSInspectionForm
import com.example.afjtracking.view.fragment.vehicle_weekly_inspection.viewmodel.WeeklyInspectionViewModel


class WeeklyInspectionForm : Fragment() {
    private var _binding: FragmentWeeklyInspectionFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var mBaseActivity: NavigationDrawerActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    private var checkIndex = 0
    var inpsecitonTitle = "Inspection Completed: "
    var listChecks: ArrayList<WeeklyInspectionCheck> = arrayListOf()
    var listRadioOption: ArrayList<RadioCheckOption> = arrayListOf()
    var totalCountChecks = 0
    var inspectionID = ""
    val satisfactoryCode = "satisfactory"

    var radioOption = ""
    lateinit var weeklyInspectionViewModel: WeeklyInspectionViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        weeklyInspectionViewModel =  ViewModelProvider(this).get(WeeklyInspectionViewModel::class.java)

        _binding = FragmentWeeklyInspectionFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        root.hideKeyboard()
        val inspectionId =
            arguments?.getInt(PTSInspectionForm.argumentParams)!!

        val body = SingleInspectionRequest("$inspectionId")

        binding.baseLayout.visibility = View.GONE


        val authView = CustomAuthenticationView(requireContext())
        binding.mainLayout.addView(authView)
        authView.addAuthListner(object : CustomAuthenticationView.AuthListeners {
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
                if(it != null) {
                    binding.inspectionModel = weeklyInspectionViewModel

                    try {
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
                mBaseActivity.toast("Inspection Successfully Completed")
                mBaseActivity.onBackPressed()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun createViews(data: WeeklyInspectionCheckData) {

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


    fun createInspectionChecksView(
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


        view.radioCheckGroup.setOnCheckedChangeListener({ group, checkedId ->
            val rb = group.findViewById(checkedId) as RadioButton
            radioOption = rb.tag.toString()

            if (radioOption != satisfactoryCode) {
                view.commentContainer.visibility = View.VISIBLE
            } else {
                view.commentContainer.visibility = View.GONE
            }


        })

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



    fun saveInspectionToList(
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
                    AFJUtils.startOutAnimation(mBaseActivity,binding.layoutContainerCheck)
                }
            } else {
                if (checkIndex < listChecks.size - 1) {
                    checkIndex++


                     createInspectionChecksView(listChecks[checkIndex], data)
                    AFJUtils.startInAnimation(mBaseActivity,binding.layoutContainerCheck)



                } else {
                    val body = SavedWeeklyInspection(inspectionID, listChecks)
                    weeklyInspectionViewModel.saveWeeklyInspectionChecks(mBaseActivity, body,false)
                }
            }
        } catch (e: Exception) {
            mBaseActivity.writeExceptionLogs(e.toString())
        }
    }

    fun addRadioButtons(
        radioButtonTexts: ArrayList<RadioCheckOption>,
        rg: RadioGroup,
        savedInspection: ArrayList<SavedInspection>
    ) {
        for (text in radioButtonTexts) {
            var newRadioButton = RadioButton(context)


            newRadioButton.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            newRadioButton.text = text.value
            newRadioButton.tag = text.id


            if (Build.VERSION.SDK_INT < 21) {
                CompoundButtonCompat.setButtonTintList(newRadioButton, ColorStateList.valueOf(R.color.colorPrimary))//Use android.support.v4.widget.CompoundButtonCompat when necessary else
            } else {
                newRadioButton.buttonTintList = ColorStateList.valueOf(R.color.colorPrimary)//setButtonTintList is accessible directly on API>19
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


    fun savedInspectionData(
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

        val body = SavedWeeklyInspection(inspectionID, listChecks)
        weeklyInspectionViewModel.saveWeeklyInspectionChecks(mBaseActivity, body,true)
    }


}