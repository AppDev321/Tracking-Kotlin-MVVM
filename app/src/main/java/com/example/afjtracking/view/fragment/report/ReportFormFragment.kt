package com.example.afjtracking.view.fragment.fuel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentReportFromBinding
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.InspectionForm
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.utils.*
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.CustomDropDownAdapter
import com.example.afjtracking.view.adapter.FileFormAdapter
import com.example.afjtracking.view.adapter.ImageFormAdapter
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.fuel.viewmodel.ReportViewModel
import com.google.android.material.snackbar.Snackbar
import java.sql.Time
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*


class ReportFormFragment : Fragment() {

    private var _binding: FragmentReportFromBinding? = null
    private val binding get() = _binding!!

    var storedData: ArrayList<StoreReportFormData> = arrayListOf()

    lateinit var reportViewModel: ReportViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity
    val uniqueUploadId = Constants.FILE_UPLOAD_UNIQUE_ID
    var requestType = ""

    var imageForm: ArrayList<InspectionForm> = arrayListOf()
    var formIndex: ArrayList<Int> = arrayListOf()
    var isOdoMeterErrorFound = false
    var lastOdoReading = 0
    var odoReadingError = ""
    lateinit var txtErrorMsg: TextView

    var vehicle: Vehicle = Vehicle()
    var uploadPhotoCount = 1
    var uploadFileCount = 1
    lateinit var imageFormAdapter: ImageFormAdapter
    var reportDate: String = ""
    var imageUploadLimit = 0
    var uploadFileLimit = 1


    var fileForm: ArrayList<InspectionForm> = arrayListOf()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        reportViewModel = ViewModelProvider(this).get(ReportViewModel::class.java)
        _binding = FragmentReportFromBinding.inflate(inflater, container, false)

        val root: View = binding.root
        txtErrorMsg = binding.txtErrorMsg


        reportViewModel.showDialog.observe(mBaseActivity) {
            mBaseActivity.showProgressDialog(it)
        }

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
                    reportViewModel.getReportFormRequest(mBaseActivity)
                } else {
                    binding.root.removeAllViews()
                    binding.root.addView(authView)
                }
            }

            override fun onAuthForceClose(boolean: Boolean) {
                mBaseActivity.onBackPressed()
            }
        })







        reportViewModel.getReportData.observe(viewLifecycleOwner)
        {

            uploadFileLimit = it.fileUploadLimit!!
            requestType = it.requestName!!
            imageUploadLimit = it.imageUploadLimit!!
        }


        reportViewModel.getReportForm.observe(viewLifecycleOwner) {
            if (it != null) {

           try {

                    showReportFormField(it)

                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
                reportViewModel._reportForm.value = null
            }
        }

        reportViewModel.getVehicle.observe(viewLifecycleOwner) {
            if (it != null) {
                vehicle = it
                reportViewModel._vehicle.value = null
            }
        }

        // Add observer for score
        reportViewModel.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mBaseActivity.toast(it, true)
                mBaseActivity.showProgressDialog(false)
               // binding.txtErrorMsg.visibility = View.VISIBLE
                binding.txtErrorMsg.text = it.toString()
               // binding.layoutCreateInspection.visibility = View.GONE
                reportViewModel.errorsMsg.value = null
            }
        })

        reportViewModel.apiUploadStatus.observe(viewLifecycleOwner, {
            if (it) {
              /*  mBaseActivity.onBackPressed()
                mBaseActivity.showSnackMessage(
                    "Request saved", requireView()
                )*/
                CustomDialog().showTaskCompleteDialog(
                    mBaseActivity,
                    isShowTitle =  true,
                    isShowMessage = true,
                    titleText=getString(R.string.request_submited),
                    msgText =getString(R.string.request_msg,"report"),
                    lottieFile = R.raw.report,
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
        })




        return root
    }


    fun showReportFormField(formList: List<InspectionForm>) {

        binding.baseLayout.visibility = View.VISIBLE
        binding.txtErrorMsg.visibility = View.GONE

        binding.txtInspectionTitle.text = "Report Form"

        val odoReading = vehicle.odometerReading
        lastOdoReading = if (odoReading!!.isEmpty()) 0 else odoReading.toInt()
        odoReadingError = "Cannot less than previous reading $lastOdoReading"

        for (i in formList.indices) {
            val formData = formList[i]
         try {
                createViewChecks(formData.type!!.uppercase(), formData, i)
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


            if (isAllImageUploaded  && isOdoMeterErrorFound == false) {
                var isAllRequired = true
                for (i in storedData.indices) {
                    formList[i].value = storedData[i].formData!!.value

                    if (storedData[i].formData?.required == true) {
                        if (formList[i].value!!.isEmpty() == true) {

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
                    request.reportForm = formList
                    reportViewModel.saveReportForm(request, mBaseActivity)

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
            "TEXT" -> {
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

                val dataStore = StoreReportFormData(inputText, formData)


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
                    }
                })

                storedData.add(dataStore)

                containerChecks.addView(view)

            }

            "MULTILINE" -> {

                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.setTextColor(Color.BLACK)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                containerChecks.addView(view)


                view = layoutInflater.inflate(R.layout.layout_multiline_comment_view, null)
                val inputText = view.findViewById<EditText>(R.id.edMultiline)
                inputText.hint = formData.comment

                val dataStore = StoreReportFormData(inputText, formData)

                dataStore.editText!!.onFocusChangeListener = object : View.OnFocusChangeListener {
                    override fun onFocusChange(v: View?, hasFocus: Boolean) {
                        if(!hasFocus) {
                            val s = dataStore.formData!!.value
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
                    }
                })

                storedData.add(dataStore)

                containerChecks.addView(view)

            }


            "OPTION" -> {

                val view = layoutInflater.inflate(R.layout.layout_spinner_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.spLable)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                val spinnerView = view.findViewById<Spinner>(R.id.spOption)
                spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        //AFJUtils.writeLogs(formData.options[p2].fieldName.toString())
                        for (i in storedData.indices) {
                            if (storedData[i].formData!!.inputNo == formData.inputNo) {
                                storedData[i].formData!!.value =
                                    formData.options[p2].fieldName.toString()
                            }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
                val adapter = CustomDropDownAdapter(mBaseActivity, formData.options)
                spinnerView.adapter = adapter
                containerChecks.addView(view)


                val dataStore = StoreReportFormData(null, formData)
                storedData.add(dataStore)

            }

            "IMAGE" -> {

                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                val titleLabel = view.findViewById<TextView>(R.id.text_label)
                titleLabel.text = formData.title + "${if (formData.required!!) "*" else ""}"
                titleLabel.setTextColor(Color.BLACK)
                containerChecks.addView(view)


                imageForm.add(InspectionForm(fieldName = "image_$uploadPhotoCount"))
                // createMultipleImageView()
                view = layoutInflater.inflate(R.layout.layout_recycler_veiw, null)
                val layoutManager = GridLayoutManager(mBaseActivity, 3)
                val recImageContainer = view.findViewById<RecyclerView>(R.id.rec_image_container)
                recImageContainer.layoutManager = layoutManager
                val imageFormAdapter =
                    ImageFormAdapter(requestType, uniqueUploadId, mBaseActivity, imageForm, true)
                imageFormAdapter.setImageFormListner(object : ImageFormAdapter.ImageFormListner {
                    override fun onPreviewGenerated(uploadForm: InspectionForm, positon: Int) {
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
                        imageForm.add(InspectionForm(fieldName = "image"))
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


                fileForm.add(InspectionForm(fieldName = "file", title = "Filename"))
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
                    override fun onPreviewGenerated(uploadForm: InspectionForm, positon: Int) {
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
                        fileForm.add(InspectionForm(fieldName = "file", title = "File"))
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


            "MULTISELECT" -> {



                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                val titleLabel = view.findViewById<TextView>(R.id.text_label)
                titleLabel.text = formData.title + "${if (formData.required!!) "*" else ""}"
                titleLabel.setTextColor(Color.BLACK)
                containerChecks.addView(view)
                containerChecks.addView(addSpaceView())



                val dataStore = StoreReportFormData(null, formData)
                storedData.add(dataStore)


                var arrayChecks = arrayListOf<String>()
                for (i in formData.options.indices) {
                    val data = formData.options[i]
                    val checkBox = CheckBox(mBaseActivity)
                    checkBox.text = data.title
                    checkBox.isChecked = false
                        if (Build.VERSION.SDK_INT < 21) {
                            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(R.color.colorPrimary))//Use android.support.v4.widget.CompoundButtonCompat when necessary else
                        } else {
                            checkBox.buttonTintList = ColorStateList.valueOf(R.color.colorPrimary)//setButtonTintList is accessible directly on API>19
                        }

                    arrayChecks.add(data.fieldName.toString())
                    checkBox.setOnCheckedChangeListener(object :
                        CompoundButton.OnCheckedChangeListener {
                        override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
                            if (!isChecked) {
                                arrayChecks[i] = ""
                            } else {
                                arrayChecks[i] = data.fieldName.toString()
                            }

                            for (i in storedData.indices) {
                                if (storedData[i].formData!!.inputNo == formData.inputNo) {
                                        var checkValue= ""
                                    for (z in arrayChecks.indices) {
                                        if(arrayChecks[z].isNotEmpty())
                                        {
                                            if(checkValue.isNotEmpty())
                                                  checkValue = checkValue + "," + arrayChecks[z]
                                            else
                                                checkValue = arrayChecks[z]
                                        }
                                    }

                                    storedData[i].formData!!.value = checkValue
                                    AFJUtils.writeLogs(checkValue)


                                }
                            }
                        }

                    })
                    containerChecks.addView(checkBox)
                }

                containerChecks.addView(addSpaceView())
            }


            "DATETIME" -> {

                var view = layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                containerChecks.addView(view)

                containerChecks.addView(addSpaceView())
                view = layoutInflater.inflate(R.layout.layout_date_time_view, null)
                val txtDate = view.findViewById<TextView>(R.id.txtDate)
                val btnDatePicker = view.findViewById<RelativeLayout>(R.id.btnDatePicker)
                containerChecks.addView(view)

                var year = 0
                var month = 0
                var day = 0

                var mHour: Int
                var mMinute: Int


                lateinit var datePicker: DatePicker
                lateinit var calendar: Calendar
                calendar = Calendar.getInstance()
                year = calendar.get(Calendar.YEAR)

                month = calendar.get(Calendar.MONTH)
                day = calendar.get(Calendar.DAY_OF_MONTH)


                reportDate = "$year-${month + 1}-$day"


                val c = Calendar.getInstance()
                mHour = c[Calendar.HOUR_OF_DAY]
                mMinute = c[Calendar.MINUTE]
                reportDate = reportDate + " " + getTime(mHour, mMinute)
                txtDate.text = reportDate
                formData.value = reportDate

                btnDatePicker.setOnClickListener {

                    DatePickerDialog(
                        mBaseActivity,
                        object : DatePickerDialog.OnDateSetListener {
                            override fun onDateSet(
                                p0: DatePicker?,
                                year: Int,
                                month: Int,
                                day: Int
                            ) {

                                reportDate = "$year-${month + 1}-$day"
                                txtDate.text = reportDate


                                // Get Current Time
                                val c = Calendar.getInstance()
                                mHour = c[Calendar.HOUR_OF_DAY]
                                mMinute = c[Calendar.MINUTE]


                                val timePickerDialog = TimePickerDialog(
                                    mBaseActivity,
                                    OnTimeSetListener { view, hourOfDay, minute ->
                                        reportDate = reportDate + " " + getTime(hourOfDay, minute)
                                        txtDate.text = reportDate

                                        for (i in storedData.indices) {
                                            if (storedData[i].formData!!.inputNo == formData.inputNo) {
                                                storedData[i].formData!!.value = reportDate
                                            }
                                        }

                                    },
                                    mHour,
                                    mMinute,
                                    false
                                )
                                timePickerDialog.show()
                            }

                        }, year, month, day
                    ).show()

                }

                val dataStore = StoreReportFormData(null, formData)
                storedData.add(dataStore)
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


    private fun getTime(hr: Int, min: Int): String {
        val tme = Time(hr, min, 0)
        val formatter: Format
        formatter = SimpleDateFormat("H:mm")
        return formatter.format(tme)
    }
}


data class StoreReportFormData(
    val editText: EditText? = null,
    var formData: InspectionForm? = null


)
