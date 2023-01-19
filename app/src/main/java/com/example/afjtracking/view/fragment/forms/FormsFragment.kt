package com.example.afjtracking.view.fragment.forms

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentFormsBinding

import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.Form
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.model.responses.VehicleMenu
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.CustomWidget
import com.example.afjtracking.utils.StoreCustomFormData

import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.FileFormAdapter
import com.example.afjtracking.view.adapter.ImageFormAdapter
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.forms.viewmodel.FormsViewModel

import com.google.android.material.snackbar.Snackbar
import java.io.File


class FormsFragment : Fragment() {

    private var _binding: FragmentFormsBinding? = null
    private val binding get() = _binding!!

    var storedData: ArrayList<StoreCustomFormData> = arrayListOf()


    lateinit var formsViewModel: FormsViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity
   // val uniqueUploadId = Constants.FILE_UPLOAD_UNIQUE_ID
   val uniqueUploadId =  "" + System.currentTimeMillis()
    var requestType = ""

    var formAttachments: ArrayList<Form> = arrayListOf()
    var lastOdoReading = 0
    var odoReadingError = ""
    lateinit var txtErrorMsg: TextView

    var vehicle: Vehicle = Vehicle()
var formName = ""

    var fileForm: ArrayList<Form> = arrayListOf()

    var identifierForm = "no_argument"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    companion object {
        const val FORM_IDENTIFIER_ARGUMENT = "form_identifier"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        formsViewModel = ViewModelProvider(this)[FormsViewModel::class.java]
        _binding = FragmentFormsBinding.inflate(inflater, container, false)


        val menuObject = requireArguments().getSerializable(FORM_IDENTIFIER_ARGUMENT) as VehicleMenu
        identifierForm = menuObject.identifier.toString()

        val root: View = binding.root
        txtErrorMsg = binding.txtErrorMsg


        formsViewModel.showDialog.observe(mBaseActivity) {
            mBaseActivity.showProgressDialog(it)
        }

        binding.baseLayout.visibility = View.GONE


        if(menuObject.qrStatus == true) {
            val authView = CustomAuthenticationView(requireContext())
            binding.root.addView(authView)
            authView.addAuthListener(object : CustomAuthenticationView.AuthListeners {
                override fun onAuthCompletionListener(boolean: Boolean) {
                    if (_binding == null)
                        return
                    if (boolean) {
                        binding.root.removeAllViews()
                        binding.root.addView(binding.baseLayout)
                        formsViewModel.getReportFormRequest(mBaseActivity, identifierForm)
                    } else {
                        binding.root.removeAllViews()
                        binding.root.addView(authView)
                    }
                }

                override fun onAuthForceClose(boolean: Boolean) {
                    mBaseActivity.onBackPressed()
                }
            })

        }
        else
        {
            binding.root.removeAllViews()
            binding.root.addView(binding.baseLayout)
            formsViewModel.getReportFormRequest(mBaseActivity, identifierForm)
        }


        binding.root.removeAllViews()
        binding.root.addView(binding.baseLayout)
   //     formsViewModel.getReportFormRequest(mBaseActivity, identifierForm)


        formsViewModel.getFormData.observe(viewLifecycleOwner)
        {
            requestType = it.requestName!!
            //  binding.txtInspectionTitle.text = it.formName
            mBaseActivity.supportActionBar?.title = it.formName
            formName= it.formName!!
        }


        formsViewModel.getReportForm.observe(viewLifecycleOwner) {
            if (it != null) {
                try {
                    showReportFormField(it)
                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
                formsViewModel._reportForm.value = null
            }
        }

        formsViewModel.getVehicle.observe(viewLifecycleOwner) {
            if (it != null) {
                vehicle = it
                formsViewModel._vehicle.value = null
            }
        }

        // Add observer for score
        formsViewModel.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                mBaseActivity.toast(it, true)
                mBaseActivity.showProgressDialog(false)
                // binding.txtErrorMsg.visibility = View.VISIBLE
                binding.txtErrorMsg.text = it.toString()
                // binding.layoutCreateInspection.visibility = View.GONE
                formsViewModel.errorsMsg.value = null
            }
        })

        formsViewModel.apiUploadStatus.observe(viewLifecycleOwner) {
            if (it) {
                mBaseActivity.onBackPressed()
                mBaseActivity.showSnackMessage(
                    "Request saved", requireView()
                )
            }
        }




        return root
    }


    private fun showReportFormField(formList: List<Form>) {

        binding.baseLayout.visibility = View.VISIBLE
        binding.txtErrorMsg.visibility = View.GONE


        if (vehicle.odometerReading != null) {
            val odoReading = vehicle.odometerReading
            lastOdoReading = if (odoReading!!.isEmpty()) 0 else odoReading.toInt()
            odoReadingError = getString(R.string.odo_meter_error, lastOdoReading)
        }
        for (i in formList.indices) {

            val formData = formList[i]
            try {
                //
                if (formData.type!!.uppercase().contains("IMAGE") || formData.type!!.uppercase()
                        .contains("FILE")
                ) {
                    createViewChecks(formData.type!!.uppercase(), formData, i)
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
            } catch (e: Exception) {
                mBaseActivity.writeExceptionLogs(e.toString())
            }
        }


        binding.btnPreviousCehck.setOnClickListener {
            mBaseActivity.onBackPressed()
        }
        binding.btnSubmit.setOnClickListener {
            var isAllImageUploaded = true
            if (formAttachments.size > 0) {
                for (image in formAttachments) {
                    if (image.required == true) {
                        if (image.value!!.isEmpty()) {
                            Snackbar.make(
                                binding.root,
                                "Please enter ${image.title}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            isAllImageUploaded = false
                            break
                        }
                    }
                }
            }
            if (isAllImageUploaded) {
                var isAllRequired = true
                for (i in storedData.indices) {
                    formList[i].value = storedData[i].formData!!.value
                    if (storedData[i].formData?.required == true) {
                        if (formList[i].value!!.isEmpty()) {
                            mBaseActivity.showSnackMessage(
                                "Please enter ${storedData[i].formData!!.title}",
                                binding.root
                            )
                            isAllRequired = false
                            break
                        }
                    }
                }
                if (isAllRequired) {
                    val request = SaveFormRequest()
                    request.uploadID = uniqueUploadId
                    request.requestName = identifierForm
                    request.identifier = identifierForm
                    request.formData = formList
                    request.deviceDetail = AFJUtils.getDeviceDetail()
                    formsViewModel.saveReportForm(request, mBaseActivity)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }


    @SuppressLint("MissingInflatedId")
    fun createViewChecks(uiType: String, formData: Form, position: Int) {
        val containerChecks = binding.layoutVdiForm
        when (uiType) {
            "IMAGE" -> {

                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                val titleLabel = view.findViewById<TextView>(R.id.text_label)
                titleLabel.text = formData.title + "${if (formData.required!!) "*" else ""}"
                titleLabel.setTextColor(Color.BLACK)
                containerChecks.addView(view)
                formData.attachmentList?.add(
                    Form(
                        fieldName = "${formData.fieldName}#${formData.attachmentList?.size}"
                    )
                )

                view = layoutInflater.inflate(R.layout.layout_recycler_veiw, null)
                val layoutManager = GridLayoutManager(mBaseActivity, 3)
                val recImageContainer = view.findViewById<RecyclerView>(R.id.rec_image_container)
                  recImageContainer.layoutManager = layoutManager
                val imageFormAdapter =
                    ImageFormAdapter(
                        requestType,
                        uniqueUploadId,
                        mBaseActivity,
                        formData.attachmentList!!,
                        true
                    )
                imageFormAdapter.setImageFormListner(object : ImageFormAdapter.ImageFormListner {
                    override fun onPreviewGenerated(uploadForm: Form, positon: Int,filePath:String) {
                        formData.attachmentList?.set(positon, uploadForm)
                        formData.value= uploadForm.value

                        if(formName.equals("Fuel Form")) {
                            if (filePath.isNotEmpty() && formData.fieldName.toString().lowercase()
                                    .contains("receipt")
                            ) {
                                //****************Than make data auto filled
                                checkIfItsFuelFormReciept(filePath)
                                //********************************************
                            }
                        }
                    }
                })

                recImageContainer.adapter = imageFormAdapter
                val textTitleLable = view.findViewById<Button>(R.id.button)
                textTitleLable.text = "Add Photo"

                val uploadLimit = formData.uploadLimit as Int
                if (uploadLimit > 1) {

                    textTitleLable.visibility = View.VISIBLE
                    textTitleLable.setOnClickListener {
                        val listSize = formData.attachmentList?.size as Int
                        if (listSize < uploadLimit) {
                            formData.attachmentList?.add(
                                Form(
                                    fieldName = "${formData.fieldName}#${formData.attachmentList?.size}"
                                )
                            )
                            imageFormAdapter.notifyItemInserted(formAttachments.size)
                            recImageContainer.smoothScrollToPosition(formAttachments.size)
                        } else {
                            textTitleLable.visibility = View.GONE
                        }

                    }
                } else {
                    textTitleLable.visibility = View.GONE
                }

                containerChecks.addView(view)
                containerChecks.addView(addSpaceView())

            }
            "FILE" -> {
                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                val titleLabel = view.findViewById<TextView>(R.id.text_label)
                titleLabel.text = formData.title + "${if (formData.required!!) "*" else ""}"
                titleLabel.setTextColor(Color.BLACK)
                containerChecks.addView(view)

                formData.attachmentList?.add(
                    Form(
                        fieldName = "${formData.fieldName}#${formData.attachmentList?.size}",
                        title = "Filename"
                    )
                )


                view = layoutInflater.inflate(R.layout.layout_recycler_veiw, null)
                val layoutManager = LinearLayoutManager(mBaseActivity)
                val recImageContainer = view.findViewById<RecyclerView>(R.id.rec_image_container)
                recImageContainer.layoutManager = layoutManager
                val fileFormAdapter =
                    FileFormAdapter(
                        requestType,
                        uniqueUploadId,
                        mBaseActivity,
                        formData.attachmentList!!,
                        true
                    )
                fileFormAdapter.setImageFormListner(object : FileFormAdapter.ImageFormListner {
                    override fun onPreviewGenerated(uploadForm: Form, positon: Int) {
                        formData.attachmentList?.set(positon, uploadForm)
                        formData.value= uploadForm.value
                    }
                })

                recImageContainer.adapter = fileFormAdapter

                val textTitleLable = view.findViewById<Button>(R.id.button)
                textTitleLable.text = "Add More Files"
                textTitleLable.setTextColor(Color.WHITE)
                val uploadLimit = formData.uploadLimit as Int
                if (uploadLimit > 1) {
                    textTitleLable.visibility = View.VISIBLE
                } else {
                    textTitleLable.visibility = View.GONE
                }
                textTitleLable.setOnClickListener {
                    val listSize = formData.attachmentList?.size as Int
                    if (listSize < uploadLimit) {
                        formData.attachmentList?.add(
                            Form(
                                fieldName = "${formData.fieldName}#${formData.attachmentList?.size}",
                                title = "Filename"
                            )
                        )

                        fileFormAdapter.notifyItemInserted(fileForm.size)
                        recImageContainer.smoothScrollToPosition(fileForm.size)
                    } else {
                        textTitleLable.visibility = View.GONE
                    }

                }
                containerChecks.addView(view)
                containerChecks.addView(addSpaceView())

            }

            else -> {
                AFJUtils.writeLogs("not thing to create view")
            }
        }


    }

    private fun checkIfItsFuelFormReciept(filePath: String) {
        formsViewModel.getTextFromFuelSlip(File(filePath),mBaseActivity, callback = {
            if(it is String)
            {
              mBaseActivity.showSnackMessage(it, binding.root)
            }
            else {

                val dataMap = it as Map<String, String>
                AFJUtils.writeLogs(dataMap.toString())

                val liter = dataMap["per_liter"] ?:""
                val qty = dataMap["total_liter"] ?:""
                val totalPrice =dataMap["total_price"] ?:""
                val cardNumber = dataMap["card_number"] ?:""

                if (liter.isNotEmpty() && qty.isNotEmpty() && totalPrice.isNotEmpty()) {
                    for (data in storedData) {
                        val fieldName = data.formData!!.title!!.lowercase()
                        if (fieldName.contains("price per litre")) {
                            data.formData!!.value = liter
                            data.editText?.setText(data.formData!!.value.toString())
                        } else if (fieldName.contains("fuel quan")) {
                            data.formData!!.value = qty
                            data.editText?.setText(data.formData!!.value.toString())
                        } else if (fieldName.contains("total fuel")) {
                            data.formData!!.value = totalPrice
                            data.editText?.setText(data.formData!!.value.toString())
                        }
                        else if (fieldName.contains("card")) {
                            data.formData!!.value = cardNumber
                            data.editText?.setText(data.formData!!.value.toString())
                        }
                        else{}
                    }
                }
            }
        })
    }


    private fun addSpaceView(): View {
        // Create Space programmatically.
        val tv = Space(mBaseActivity)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            30
        )
        tv.layoutParams = layoutParams

        return tv

    }


}

