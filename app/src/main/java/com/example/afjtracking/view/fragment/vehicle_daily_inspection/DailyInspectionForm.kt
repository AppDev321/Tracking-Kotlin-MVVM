package com.example.afjtracking.view.fragment.vehicle_daily_inspection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentVdiCreateInspectionBinding
import com.example.afjtracking.model.responses.InspectionCheckData
import com.example.afjtracking.model.responses.Form
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.CustomWidget
import com.example.afjtracking.utils.StoreCustomFormData
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.ImageFormAdapter
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel.DailyInspectionViewModel
import com.google.android.material.snackbar.Snackbar


class DailyInspectionForm : Fragment() {

    private var _binding: FragmentVdiCreateInspectionBinding? = null
    private val binding get() = _binding!!

    var storedData: ArrayList<StoreCustomFormData> = arrayListOf()

    lateinit var inspectionViewModel: DailyInspectionViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity
    private val uniqueUploadId = Constants.FILE_UPLOAD_UNIQUE_ID
    var requestType = ""

    var imageForm: ArrayList<Form> = arrayListOf()
    var formIndex: ArrayList<Int> = arrayListOf()
    var lastOdoReading = 0
    var odoReadingError = ""

    lateinit var txtErrorMsg: TextView

    var isOdoMeterErrorFound = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    private fun moveToChecksScreen(it: InspectionCheckData) {
        if (it.ptsChecks.size > 0) {

            //Create PSV  form
            val bundle = bundleOf(PTSInspectionForm.argumentParams to it)
            //mBaseActivity.closeFragment(this)
            mBaseActivity.moveFragmentCloseCurrent(
                binding.root,
                R.id.nav_pts_inspection_form,
                R.id.nav_vdi_inspection_create,
                bundle
            )
        } else {
            //Create PSV  form
            val bundle = bundleOf(PSVInspectionForm.argumentParams to it)
            //  mBaseActivity.closeFragment(this)
            mBaseActivity.moveFragmentCloseCurrent(
                binding.root,
                R.id.nav_psv_inspection_form,
                R.id.nav_vdi_inspection_create,
                bundle
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // AFJUtils.setPeriodicWorkRequest(mBaseActivity)
        inspectionViewModel = ViewModelProvider(this).get(DailyInspectionViewModel::class.java)
        _binding = FragmentVdiCreateInspectionBinding.inflate(inflater, container, false)

        val root: View = binding.root
        txtErrorMsg = binding.txtErrorMsg
        binding.baseLayout.visibility = View.GONE

        val authView = CustomAuthenticationView(requireContext())
        binding.root.addView(authView)
        authView.addAuthListner(object : CustomAuthenticationView.AuthListeners {
            override fun onAuthCompletionListener(boolean: Boolean) {
                if (_binding == null)
                    return
                if (boolean) {
                    binding.root.removeAllViews()
                    binding.root.addView(binding.baseLayout)
                    binding.root.addView(binding.txtErrorMsg)
                    inspectionViewModel.getDailyVehicleInspectionCheckList(mBaseActivity)
                } else {
                    binding.root.removeAllViews()
                    binding.root.addView(authView)
                }
            }

            override fun onAuthForceClose(boolean: Boolean) {
                mBaseActivity.onBackPressed()
            }
        })

        inspectionViewModel.showDialog.observe(mBaseActivity) {
            mBaseActivity.showProgressDialog(it)
        }

        inspectionViewModel.inspectionChecksData.observe(viewLifecycleOwner) {
            if (it != null) {
                requestType = it.requestName!!
                try {
                    if (it.isCompleted == false)
                        showInspectionCreationForm(it)
                    else {
                        mBaseActivity.onBackPressed()
                        mBaseActivity.showSnackMessage(
                            "Inspection already completed",
                            requireView()
                        )
                    }
                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
                inspectionViewModel._inspectionData.value = null
            }
        }

        // Add observer for score
        inspectionViewModel.errorsMsg.observe(viewLifecycleOwner) {
            if (it != null) {
                mBaseActivity.toast(it, false)
                mBaseActivity.showProgressDialog(false)
                binding.txtErrorMsg.visibility = View.VISIBLE
                binding.txtErrorMsg.text = it.toString()
                binding.baseLayout.visibility = View.GONE
                inspectionViewModel.errorsMsg.value = null
            }
        }


        return root
    }


    private fun showInspectionCreationForm(it: InspectionCheckData) {

        val inpsecitonData: InspectionCheckData = it

        val data = if (inpsecitonData.vehicleType!!.lowercase()
                .contains("psv")
        ) inpsecitonData.psvForm else inpsecitonData.ptsForm


        binding.baseLayout.visibility = View.VISIBLE
        binding.txtErrorMsg.visibility = View.GONE

        binding.txtInspectionTitle.text =
            "Inspection | ${it.vehicle!!.vrn} ( ${it.vehicle!!.detail!!.vehicleType})"

        val odoReading = inpsecitonData.vehicle!!.odometerReading
        lastOdoReading = if (odoReading!!.isEmpty()) 0 else odoReading.toInt()
        odoReadingError = "Cannot less than previous reading $lastOdoReading"

        for (i in data.indices) {
            val formData = data[i]
            try {
                if (formData.type!!.uppercase().contains("IMAGE") /*|| formData.type!!.uppercase()
                        .contains("FILE")*/
                ) {
                    storedData.add(StoreCustomFormData(formData = formData))
                    imageForm.add(formData)
                    formIndex.add(i)
                } else {
                    val customFormData = CustomWidget().createDynamicFormViews(
                        mBaseActivity,
                        formData,
                        i,
                        lastOdoReading,
                        uniqueUploadId,
                        requestType,
                        binding.layoutVdiForm

                    )
                    if (customFormData != null) {
                        storedData.add(customFormData)

                    }
                }
                //    createViewChecks(formData.type!!.uppercase(), formData, i)
            } catch (e: Exception) {
                mBaseActivity.writeExceptionLogs(e.toString())
            }
        }

        //Create Form data
        if (imageForm.size > 0) {
            val layoutManager = GridLayoutManager(mBaseActivity, 3)
            binding.recImageContainer.layoutManager = layoutManager
            val imageFormAdapter =
                ImageFormAdapter(requestType, uniqueUploadId, mBaseActivity, imageForm)
            imageFormAdapter.setImageFormListner(object : ImageFormAdapter.ImageFormListner {
                override fun onPreviewGenerated(uploadForm: Form, positon: Int) {

                    val index = formIndex[positon]
                    imageForm[positon] = uploadForm
                    storedData[index].formData = uploadForm
                }
            })
            binding.recImageContainer.adapter = imageFormAdapter
        }


        binding.btnSubmit.setOnClickListener {
            proceedVerification(data, inpsecitonData)
        }

    }

    private fun proceedVerification(
        data: ArrayList<Form>,
        inspectionData: InspectionCheckData
    ) {
        isOdoMeterErrorFound = false
        for (i in storedData.indices) {
            if (storedData[i].isOdoMeterErrorFound == true) {
                isOdoMeterErrorFound = true
            }
        }

        if (isOdoMeterErrorFound) {
            mBaseActivity.showSnackMessage(odoReadingError, binding.root)
            return
        }

        var isImageErrorFound = false
        if (imageForm.size > 0) {
            for (i in imageForm.indices) {
                if (imageForm[i].required == true) {
                    if (imageForm[i].value!!.isEmpty()) {
                        Snackbar.make(
                            binding.root,
                            "Please enter ${imageForm[i].title}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        isImageErrorFound = true
                        break

                    }
                }

            }
        }
        if (isImageErrorFound) {
            return
        }


        var errorFoundInEmptyFields = false

        for (i in storedData.indices) {
            data[i].value = storedData[i].formData!!.value

            if (storedData[i].formData?.required == true) {
                if (data[i].value!!.isEmpty()) {

                    mBaseActivity.showSnackMessage(
                        "Please enter ${storedData[i].formData!!.title}",
                        binding.root
                    )
                    errorFoundInEmptyFields = true
                    break

                }
            }

        }

        if(errorFoundInEmptyFields)
        {
            return
        }

        if (inspectionData.vehicleType!!.lowercase().contains("psv")) {
            inspectionData.psvForm = data
        } else {
            inspectionData.ptsForm = data
        }
        inspectionData.uploadID = uniqueUploadId
        inspectionData.vehicle!!.odometerReading = "$lastOdoReading"
        moveToChecksScreen(inspectionData)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }


}






