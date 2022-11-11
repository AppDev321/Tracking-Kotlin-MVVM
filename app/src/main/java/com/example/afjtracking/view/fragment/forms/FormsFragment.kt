package com.example.afjtracking.view.fragment.forms

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
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.CustomWidget
import com.example.afjtracking.utils.StoreCustomFormData
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.FileFormAdapter
import com.example.afjtracking.view.adapter.ImageFormAdapter
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.forms.viewmodel.FormsViewModel

import com.google.android.material.snackbar.Snackbar


class FormsFragment : Fragment() {

    private var _binding: FragmentFormsBinding? = null
    private val binding get() = _binding!!

    var storedData: ArrayList<StoreCustomFormData> = arrayListOf()



    lateinit var formsViewModel: FormsViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity
    val uniqueUploadId = Constants.FILE_UPLOAD_UNIQUE_ID
    var requestType = ""

    var imageForm: ArrayList<Form> = arrayListOf()
    var lastOdoReading = 0
    var odoReadingError = ""
    lateinit var txtErrorMsg: TextView

    var vehicle: Vehicle = Vehicle()
    var uploadPhotoCount = 1
    var uploadFileCount = 1


    var imageUploadLimit = 0
    var uploadFileLimit = 1


    var fileForm: ArrayList<Form> = arrayListOf()

    var identifierForm = "no_argument"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }
    companion object
    {
        const val FORM_IDENTIFIER_ARGUMENT ="form_identifier"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        formsViewModel = ViewModelProvider(this)[FormsViewModel::class.java]
        _binding = FragmentFormsBinding.inflate(inflater, container, false)


     val menuObject=  requireArguments().getSerializable(FORM_IDENTIFIER_ARGUMENT) as VehicleMenu
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





        formsViewModel.getFormData.observe(viewLifecycleOwner)
        {

            uploadFileLimit = it.fileUploadLimit!!
            requestType = it.requestName!!
            imageUploadLimit = it.imageUploadLimit!!
          //  binding.txtInspectionTitle.text = it.formName
            mBaseActivity.supportActionBar?.title = it.formName
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



        val odoReading = vehicle.odometerReading
        lastOdoReading = if (odoReading!!.isEmpty()) 0 else odoReading.toInt()
        odoReadingError = getString(R.string.odo_meter_error, lastOdoReading)

        for (i in formList.indices) {
            val formData = formList[i]
            try {
                //
                if (formData.type!!.uppercase().contains("IMAGE") || formData.type!!.uppercase()
                        .contains("FILE")
                ) {
                    createViewChecks(formData.type!!.uppercase(), formData, i)
                }
                else {
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

            if (imageForm.size > 0) {
                for (i in imageForm.indices) {
                    if (imageForm[i].required == true) {
                        if (imageForm[i].value!!.isEmpty()) {
                            Snackbar.make(
                                binding.root,
                                "Please enter ${imageForm[i].title}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            isAllImageUploaded = false
                            break

                        }
                    }

                }
            }


            if (isAllImageUploaded ) {
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


    fun createViewChecks(uiType: String, formData: Form, position: Int) {
        val containerChecks = binding.layoutVdiForm
        when (uiType) {
            "IMAGE" -> {

                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                val titleLabel = view.findViewById<TextView>(R.id.text_label)
                titleLabel.text = formData.title + "${if (formData.required!!) "*" else ""}"
                titleLabel.setTextColor(Color.BLACK)
                containerChecks.addView(view)


                imageForm.add(Form(fieldName = "${formData.fieldName}#$uploadPhotoCount"))
                // createMultipleImageView()
                view = layoutInflater.inflate(R.layout.layout_recycler_veiw, null)
                val layoutManager = GridLayoutManager(mBaseActivity, 3)
                val recImageContainer = view.findViewById<RecyclerView>(R.id.rec_image_container)
                recImageContainer.layoutManager = layoutManager
                val imageFormAdapter =
                    ImageFormAdapter(requestType, uniqueUploadId, mBaseActivity, imageForm, true)
                imageFormAdapter.setImageFormListner(object : ImageFormAdapter.ImageFormListner {
                    override fun onPreviewGenerated(uploadForm: Form, positon: Int) {
                        imageForm[positon] = uploadForm

                    }
                })

                recImageContainer.adapter = imageFormAdapter


                val textTitleLable = view.findViewById<Button>(R.id.button)
                textTitleLable.text = "Add Photo"
                textTitleLable.setTextColor(Color.BLACK)
                textTitleLable.visibility = View.VISIBLE
                textTitleLable.setOnClickListener {
                    if (uploadPhotoCount < imageUploadLimit) {
                        imageForm.add(Form(fieldName = "image"))
                        imageFormAdapter.notifyItemInserted(imageForm.size)
                        recImageContainer.smoothScrollToPosition(imageForm.size)
                        uploadPhotoCount++
                    } else {
                        textTitleLable.visibility = View.GONE
                    }

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
                fileForm.add(Form(fieldName = "file", title = "Filename"))
                // createMultipleImageView()
                view = layoutInflater.inflate(R.layout.layout_recycler_veiw, null)
                val layoutManager = LinearLayoutManager(mBaseActivity)
                val recImageContainer = view.findViewById<RecyclerView>(R.id.rec_image_container)
                recImageContainer.layoutManager = layoutManager
                val fileFormAdapter =
                    FileFormAdapter(
                        requestType,
                        uniqueUploadId,
                        mBaseActivity,
                        fileForm,
                        true
                    )
                fileFormAdapter.setImageFormListner(object : FileFormAdapter.ImageFormListner {
                    override fun onPreviewGenerated(uploadForm: Form, positon: Int) {
                        fileForm[positon] = uploadForm


                    }
                })

                recImageContainer.adapter = fileFormAdapter


                val textTitleLable = view.findViewById<Button>(R.id.button)
                textTitleLable.text = "Add More Files"
                textTitleLable.setTextColor(Color.BLACK)
                textTitleLable.visibility = View.VISIBLE
                textTitleLable.setOnClickListener {
                    if (uploadFileCount < uploadFileLimit) {
                        fileForm.add(Form(fieldName = "file", title = "File"))
                        fileFormAdapter.notifyItemInserted(fileForm.size)
                        recImageContainer.smoothScrollToPosition(fileForm.size)
                        uploadFileCount++
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

