package com.example.afjtracking.view.fragment.vehicle_daily_inspection

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentVdiCreateInspectionBinding
import com.example.afjtracking.model.responses.InspectionCheckData
import com.example.afjtracking.model.responses.InspectionForm
import com.example.afjtracking.model.responses.UploadFileAPiResponse
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.ImageFormAdapter
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.fileupload.FileUploadDialog
import com.example.afjtracking.view.fragment.fileupload.UploadDialogListener
import com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel.DailyInspectionViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File


class DailyInspectionForm : Fragment() {

    private var _binding: FragmentVdiCreateInspectionBinding? = null
    private val binding get() = _binding!!

    var storedData: ArrayList<StoreFormData> = arrayListOf()

    lateinit var inspectionViewModel: DailyInspectionViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity
    val uniqueUploadId = Constants.FILE_UPLOAD_UNIQUE_ID
    var requestType = ""

    var imageForm: ArrayList<InspectionForm> = arrayListOf()
    var formIndex: ArrayList<Int> = arrayListOf()
    var lastOdoReading = 0
    var odoReadingError = ""

    lateinit var txtErrorMsg: TextView

    var isOdoMeterErrorFound = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    fun moveToChecksScreen(it: InspectionCheckData) {
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
                    binding.baseLayout.visibility = View.VISIBLE
                    inspectionViewModel.getDailyVehicleInspectionCheckList(mBaseActivity)
                } else {
                    binding.root.removeAllViews()
                    binding.root.addView(authView)
                }
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
        inspectionViewModel.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mBaseActivity.toast(it, false)
                mBaseActivity.showProgressDialog(false)
                binding.txtErrorMsg.visibility = View.VISIBLE
                binding.txtErrorMsg.text = it.toString()
                binding.baseLayout.visibility = View.GONE
                inspectionViewModel.errorsMsg.value = null
            }
        })


        return root
    }


    fun showInspectionCreationForm(it: InspectionCheckData) {

        val inpsecitonData: InspectionCheckData = it

        val data = if (inpsecitonData.vehicleType!!.lowercase()
                .contains("psv")
        ) inpsecitonData.psvForm else inpsecitonData.ptsForm


        binding.baseLayout.visibility = View.VISIBLE
        binding.txtErrorMsg.visibility = View.GONE

        binding.txtInspectionTitle.text =
            "Inspection | ${it.vehicle!!.vrn} ( ${it.vehicle!!.detail!!.vehicleType})"

        val odoReading =  inpsecitonData.vehicle!!.odometerReading
        lastOdoReading =   if(odoReading!!.isEmpty()) 0 else odoReading.toInt()
        odoReadingError= "Cannot less than previous reading $lastOdoReading"

        for (i in data.indices) {
            val formData = data[i]
            try {
                createViewChecks(formData.type!!.uppercase(), formData, i)
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
                override fun onPreviewGenerated(uploadForm: InspectionForm, positon: Int) {

                    val index = formIndex[positon]
                    imageForm[positon] = uploadForm
                    storedData[index].formData = uploadForm
                }
            })
            binding.recImageContainer.adapter = imageFormAdapter
        }


        binding.btnSubmit.setOnClickListener {

            var isAllImageUploaded = true

            if (imageForm.size > 0) {
                for (i in imageForm.indices) {
                    if (imageForm[i].required == true) {
                        if (imageForm[i].value!!.isEmpty() == true) {
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


            if (isAllImageUploaded && isOdoMeterErrorFound==false) {
                var isAllRequired = true
                for (i in storedData.indices) {
                    data[i].value = storedData[i].formData!!.value

                    if (storedData[i].formData?.required == true) {
                        if (data[i].value!!.isEmpty() == true) {

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
                    if (inpsecitonData.vehicleType!!.lowercase().contains("psv")) {
                        inpsecitonData.psvForm = data
                    } else {
                        inpsecitonData.ptsForm = data
                    }
                    inpsecitonData.uploadID = uniqueUploadId
                    inpsecitonData.vehicle!!.odometerReading = "$lastOdoReading"
                    moveToChecksScreen(inpsecitonData)
                }
            }
            else
            {
                mBaseActivity.showSnackMessage(  odoReadingError,  binding.root  )
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }


    fun createViewChecks(uiType: String, formData: InspectionForm, position: Int) {
        val containerChecks = binding.layoutVdiForm
        when (uiType) {
           resources.getString(R.string.ui_type_text)  -> {
                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                var textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.setTextColor(Color.BLACK)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                containerChecks.addView(view)


                view = layoutInflater.inflate(R.layout.layout_edit_text_view, null)
                var inputText = view.findViewById<EditText>(R.id.edText)
                inputText.hint = formData.comment
                if (formData.accept == "number")
                    inputText.inputType = InputType.TYPE_CLASS_NUMBER
                else if (formData.accept == "float")
                    inputText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                else if (formData.accept == "phone")
                    inputText.inputType = InputType.TYPE_CLASS_PHONE
                else if (formData.accept == "password")
                    inputText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                else
                    inputText.inputType = InputType.TYPE_CLASS_TEXT

                val dataStore = StoreFormData(inputText, formData)


               dataStore.editText!!.onFocusChangeListener = object : View.OnFocusChangeListener {
                   override fun onFocusChange(v: View?, hasFocus: Boolean) {
                       if(!hasFocus) {
                           val s =  dataStore.formData!!.value
                           if (formData.title!!.lowercase().contains("odometer")) {
                               if (!s.toString().isEmpty()) {
                                   val reading = Integer.parseInt(s.toString())
                                   if (reading < lastOdoReading) {
                                       mBaseActivity.showSnackMessage(  odoReadingError,  binding.root  )
                                       dataStore.editText.error=  odoReadingError
                                       isOdoMeterErrorFound  = true
                                   } else {
                                       isOdoMeterErrorFound  = false
                                       dataStore.editText.error = null
                                       lastOdoReading = dataStore.formData!!.value!!.toInt()
                                   }
                               }
                           }
                       }
                   }

               }


                dataStore.editText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {
                            dataStore.formData!!.value = s.toString()
                    }
                    override fun beforeTextChanged(
                        s: CharSequence, start: Int,
                        count: Int, after: Int
                    ) {
                    }
                    override fun onTextChanged(
                        s: CharSequence, start: Int,
                        before: Int, count: Int
                    ) {

                        val enterDetail = dataStore.editText.text.toString()
                        if (formData.title!!.lowercase().contains("odometer")) {
                            if (!enterDetail.isEmpty()) {
                                val reading = Integer.parseInt(   enterDetail)
                                if (reading < lastOdoReading) {
                                    mBaseActivity.showSnackMessage(  odoReadingError,  binding.root  )
                                    dataStore.editText.error=  odoReadingError
                                    isOdoMeterErrorFound  = true
                                }
                                else
                                {
                                    isOdoMeterErrorFound  = false
                                    dataStore.editText.error = null
                                    dataStore.formData!!.value = enterDetail
                                }
                            }
                        }
                        else
                        {
                            dataStore.formData!!.value = enterDetail
                        }
                    }
                })

                storedData.add(dataStore)

                containerChecks.addView(view)

            }
            resources.getString(R.string.ui_type_file)   -> {
                // "FILE" -> {
                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                var textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                containerChecks.addView(view)


                view = layoutInflater.inflate(R.layout.layout_text_view, null)
                var textDescLable = view.findViewById<TextView>(R.id.text_label)
                textDescLable.setTypeface(null, Typeface.NORMAL)
                textDescLable.text = formData.comment

                containerChecks.addView(view)


                view = layoutInflater.inflate(R.layout.layout_file_choose_box, null)
                var imagePath = view.findViewById<TextView>(R.id.txtImagePath)
                var btnPickImage = view.findViewById<Button>(R.id.btnPickImage)


                btnPickImage.setOnClickListener {
                    val dialog = FileUploadDialog.newInstance(
                        isDocumentPickShow = true,
                        inpsectionType = requestType, //This will be change after
                        uniqueFileId = uniqueUploadId,
                        fieldName = formData.fieldName!!,
                        fileUploadListner = (object : UploadDialogListener {
                            override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                            }

                            override fun onFilePathReceived(path: String) {
                                imagePath.text = path
                                formData.value = uniqueUploadId
                            }

                        })
                    )
                    dialog.isCancelable = false
                    dialog.show(mBaseActivity.supportFragmentManager, null)

                }

                val dataStore = StoreFormData(null, formData)
                storedData.add(dataStore)
                containerChecks.addView(view)

            }
            resources.getString(R.string.ui_type_image)     -> {

                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                var textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                //  containerChecks.addView(view)


                view = layoutInflater.inflate(R.layout.layout_text_view, null)
                var textDescLable = view.findViewById<TextView>(R.id.text_label)
                textDescLable.setTypeface(null, Typeface.NORMAL)
                textDescLable.text = formData.comment

                //  containerChecks.addView(view)


                view = layoutInflater.inflate(R.layout.layout_image_box, null)
                var imagePreview = view.findViewById<ImageView>(R.id.img_preview)
                var btnPickImage = view.findViewById<ImageView>(R.id.img_add)
                var btnImageDel = view.findViewById<ImageView>(R.id.img_del)
                imagePreview.visibility = View.GONE
                btnPickImage.visibility = View.VISIBLE
                btnImageDel.visibility = View.GONE




                btnPickImage.setOnClickListener {
                    val dialog = FileUploadDialog.newInstance(
                        isDocumentPickShow = false,
                        inpsectionType = requestType, //This will be change after
                        uniqueFileId = uniqueUploadId,
                        fieldName = formData.fieldName!!,
                        fileUploadListner = (object : UploadDialogListener {
                            override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                            }

                            override fun onFilePathReceived(path: String) {

                                formData.value = uniqueUploadId


                                Glide.with(view.context)
                                    .load(path)
                                    .placeholder(
                                        AppCompatResources.getDrawable(
                                            view.context,
                                            R.drawable.ic_launch
                                        )
                                    )

                                    .into(imagePreview)
                                btnPickImage.visibility = View.GONE
                                btnImageDel.visibility = View.VISIBLE
                                imagePreview.visibility = View.VISIBLE
                            }

                        })
                    )
                    dialog.isCancelable = false
                    dialog.show(mBaseActivity.supportFragmentManager, null)

                }
                btnImageDel.setOnClickListener {
                    btnImageDel.visibility = View.GONE
                    btnPickImage.visibility = View.VISIBLE
                    imagePreview.visibility = View.GONE
                }

                //val dataStore = StoreFormData(null, formData)
                //storedData.add(dataStore)
                // containerChecks.addView(view)

                val dataStore = StoreFormData(null, formData)
                storedData.add(dataStore)
                imageForm.add(formData)
                formIndex.add(position)

            }
            else -> {
                AFJUtils.writeLogs("not thing to create view")
            }
        }


    }




}


data class StoreFormData(
    val editText: EditText? = null,
    var formData: InspectionForm? = null


)





