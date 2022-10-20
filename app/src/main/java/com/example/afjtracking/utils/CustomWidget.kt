package com.example.afjtracking.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.CompoundButtonCompat
import com.bumptech.glide.Glide
import com.example.afjtracking.R
import com.example.afjtracking.model.responses.Form
import com.example.afjtracking.model.responses.UploadFileAPiResponse
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.CustomDropDownAdapter
import com.example.afjtracking.view.fragment.fileupload.FileUploadDialog
import com.example.afjtracking.view.fragment.fileupload.UploadDialogListener

import java.lang.Exception
import java.sql.Time
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*

class CustomWidget {


    fun createDynamicFormViews(
        context: AppCompatActivity,
        formData: Form,
        position: Int,
        lastOdoReading: Int? = null,
        uniqueFileId: String? = null,
        inpsectionType: String? = null,
        containerChecks: LinearLayout
    )
            : StoreCustomFormData?

    {
        context as NavigationDrawerActivity
        val odoReadingError   = context.getString(R.string.odo_meter_error,lastOdoReading)


        when (formData.type?.uppercase()) {
            AFJUtils.UI_TYPE.TEXT.name -> {
                var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.setTextColor(Color.BLACK)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                containerChecks.addView(view)
                view = context.layoutInflater.inflate(R.layout.layout_edit_text_view, null)
                val inputText = view.findViewById<EditText>(R.id.edText)
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

                val dataStore = StoreCustomFormData(inputText, formData)


                dataStore.editText?.onFocusChangeListener = object : View.OnFocusChangeListener {
                    override fun onFocusChange(v: View?, hasFocus: Boolean) {
                        if (!hasFocus) {
                            val s = dataStore.formData?.value

                            //**** Check either its a odometer reading field
                            try {
                                if (formData.title?.lowercase()?.contains("odometer") == true) {
                                    if (!s.toString().isEmpty()) {
                                        val reading = s!!.toDouble()
                                        if (reading < lastOdoReading!!) {
                                            context.showSnackMessage(
                                                odoReadingError,
                                                containerChecks
                                            )
                                            dataStore.editText?.error = odoReadingError
                                            dataStore.isOdoMeterErrorFound = true
                                        } else {
                                            dataStore.isOdoMeterErrorFound = false
                                            dataStore.editText?.error = null
                                            dataStore.lastOdoReading =
                                                dataStore.formData!!.value!!.toInt()
                                        }
                                    }
                                }

                            }
                            catch (e:Exception)
                            {
                                context.showSnackMessage("Too Large Value",containerChecks)

                            }
                            //*****************************
                        }
                    }

                }

                dataStore.editText?.addTextChangedListener(object : TextWatcher {
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



                containerChecks.addView(view)
                return dataStore

            }
            AFJUtils.UI_TYPE.FILE.name -> {
                // "FILE" -> {
                var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                containerChecks.addView(view)


                view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textDescLable = view.findViewById<TextView>(R.id.text_label)
                textDescLable.setTypeface(null, Typeface.NORMAL)
                textDescLable.text = formData.comment

                containerChecks.addView(view)


                view = context.layoutInflater.inflate(R.layout.layout_file_choose_box, null)
                val imagePath = view.findViewById<TextView>(R.id.txtImagePath)
                val btnPickImage = view.findViewById<Button>(R.id.btnPickImage)


                btnPickImage.setOnClickListener {
                    val dialog = FileUploadDialog.newInstance(
                        isDocumentPickShow = true,
                        inpsectionType = inpsectionType.toString(), //This will be change after
                        uniqueFileId = uniqueFileId.toString(),
                        fieldName = formData.fieldName!!,
                        fileUploadListner = (object : UploadDialogListener {
                            override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                            }

                            override fun onFilePathReceived(path: String) {
                                imagePath.text = path
                                formData.value = uniqueFileId.toString()
                            }

                        })
                    )
                    dialog.isCancelable = false
                    dialog.show(context.supportFragmentManager, null)

                }
                containerChecks.addView(view)
                return StoreCustomFormData(null, formData)
            }
            AFJUtils.UI_TYPE.IMAGE.name-> {
                var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                //  containerChecks.addView(view)
                view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textDescLable = view.findViewById<TextView>(R.id.text_label)
                textDescLable.setTypeface(null, Typeface.NORMAL)
                textDescLable.text = formData.comment
                //  containerChecks.addView(view)

                view = context.layoutInflater.inflate(R.layout.layout_image_box, null)
                val imagePreview = view.findViewById<ImageView>(R.id.img_preview)
                val btnPickImage = view.findViewById<ImageView>(R.id.img_add)
                val btnImageDel = view.findViewById<ImageView>(R.id.img_del)
                imagePreview.visibility = View.GONE
                btnPickImage.visibility = View.VISIBLE
                btnImageDel.visibility = View.GONE

                btnPickImage.setOnClickListener {
                    val dialog = FileUploadDialog.newInstance(
                        isDocumentPickShow = false,
                        inpsectionType = inpsectionType.toString(), //This will be change after
                        uniqueFileId = uniqueFileId.toString(),
                        fieldName = formData.fieldName!!,
                        fileUploadListner = (object : UploadDialogListener {
                            override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                            }

                            override fun onFilePathReceived(path: String) {
                                formData.value = uniqueFileId.toString()
                                Glide.with(view.context)
                                    .load(path)
                                    .placeholder(
                                        AppCompatResources.getDrawable(
                                            view.context,
                                            R.drawable.ic_no_image
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
                    dialog.show(context.supportFragmentManager, null)

                }
                btnImageDel.setOnClickListener {
                    btnImageDel.visibility = View.GONE
                    btnPickImage.visibility = View.VISIBLE
                    imagePreview.visibility = View.GONE
                }
                return StoreCustomFormData(null, formData)
            }
            AFJUtils.UI_TYPE.MULTISELECT.name-> {
                val view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val titleLabel = view.findViewById<TextView>(R.id.text_label)
                titleLabel.text = formData.title + "${if (formData.required!!) "*" else ""}"
                titleLabel.setTextColor(Color.BLACK)
                containerChecks.addView(view)
                containerChecks.addView(addSpaceView(context))
                val dataStore = StoreCustomFormData(null, formData)
                val arrayChecks = arrayListOf<String>()
                for (i in dataStore.formData!!.options.indices) {
                    val data = dataStore.formData!!.options[i]
                    val checkBox = CheckBox(context)
                    checkBox.text = data.title
                    checkBox.isChecked = false
                    if (Build.VERSION.SDK_INT < 21) {
                        CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(context.resources.getColor(R.color.colorPrimary)))//Use android.support.v4.widget.CompoundButtonCompat when necessary else
                    } else {
                        checkBox.buttonTintList = ColorStateList.valueOf(context.resources.getColor(R.color.colorPrimary))//setButtonTintList is accessible directly on API>19
                    }

                    arrayChecks.add(data.fieldName.toString())
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        if (!isChecked) {
                            arrayChecks[i] = ""

                        } else {
                            arrayChecks[i] = data.fieldName.toString()
                        }


                        var checkValue = ""
                        for (z in arrayChecks.indices) {
                            if (arrayChecks[z].isNotEmpty()) {
                                checkValue = if (checkValue.isNotEmpty())
                                    checkValue + "," + arrayChecks[z]
                                else
                                    arrayChecks[z]
                            }
                        }

                        dataStore.formData!!.value = checkValue

                    }
                    containerChecks.addView(checkBox)
                }

                containerChecks.addView(addSpaceView(context))
                return dataStore
            }
            AFJUtils.UI_TYPE.MULTILINE.name -> {

                var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.setTextColor(Color.BLACK)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                containerChecks.addView(view)


                view = context.layoutInflater.inflate(R.layout.layout_multiline_comment_view, null)
                val inputText = view.findViewById<EditText>(R.id.edMultiline)
                inputText.hint = formData.comment

                val dataStore = StoreCustomFormData(inputText, formData)

                dataStore.editText!!.onFocusChangeListener =
                    View.OnFocusChangeListener { _, hasFocus ->
                        if(!hasFocus) {
                            val s = dataStore.formData!!.value
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



                containerChecks.addView(view)
                return dataStore

            }
            AFJUtils.UI_TYPE.OPTION.name -> {

                val dataStore = StoreCustomFormData(null, formData)
                val view = context.layoutInflater.inflate(R.layout.layout_spinner_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.spLable)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                val spinnerView = view.findViewById<Spinner>(R.id.spOption)
                spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        dataStore.formData!!.value =
                            formData.options[p2].fieldName.toString()
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
                val adapter = CustomDropDownAdapter(context, formData.options)
                spinnerView.adapter = adapter
                containerChecks.addView(view)
                return dataStore

            }
            AFJUtils.UI_TYPE.DATETIME.name -> {
                val dataStore = StoreCustomFormData(null, formData)
                var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                containerChecks.addView(view)

                containerChecks.addView(addSpaceView( context))
                view =  context.layoutInflater.inflate(R.layout.layout_date_time_view, null)
                val txtDate = view.findViewById<TextView>(R.id.txtDate)
                val btnDatePicker = view.findViewById<RelativeLayout>(R.id.btnDatePicker)
                containerChecks.addView(view)

                var year = 0
                var month = 0
                var day = 0

                var mHour: Int
                var mMinute: Int


                var calendar: Calendar = Calendar.getInstance()
                year = calendar.get(Calendar.YEAR)

                month = calendar.get(Calendar.MONTH)
                day = calendar.get(Calendar.DAY_OF_MONTH)


                var  reportDate = "$year-${month + 1}-$day"


                val c = Calendar.getInstance()
                mHour = c[Calendar.HOUR_OF_DAY]
                mMinute = c[Calendar.MINUTE]
                reportDate = reportDate + " " + getTime(mHour, mMinute)
                txtDate.text = reportDate
                formData.value = reportDate

                btnDatePicker.setOnClickListener {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            reportDate = "$year-${month + 1}-$day"
                            txtDate.text = reportDate
                            // Get Current Time
                            val c = Calendar.getInstance()
                            mHour = c[Calendar.HOUR_OF_DAY]
                            mMinute = c[Calendar.MINUTE]


                            val timePickerDialog = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    reportDate = reportDate + " " + getTime(hourOfDay, minute)
                                    txtDate.text = reportDate
                                    dataStore.formData!!.value = reportDate


                                },
                                mHour,
                                mMinute,
                                false
                            )
                            timePickerDialog.show()
                        }, year, month, day
                    ).show()

                }


                return dataStore
            }
            AFJUtils.UI_TYPE.DATE.name -> {
                val dataStore = StoreCustomFormData(null, formData)
                var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                containerChecks.addView(view)
                containerChecks.addView(addSpaceView( context))
                view =  context.layoutInflater.inflate(R.layout.layout_date_time_view, null)
                val txtDate = view.findViewById<TextView>(R.id.txtDate)
                val btnDatePicker = view.findViewById<RelativeLayout>(R.id.btnDatePicker)
                containerChecks.addView(view)
                var year = 0
                var month = 0
                var day = 0
                val calendar: Calendar = Calendar.getInstance()
                year = calendar.get(Calendar.YEAR)
                month = calendar.get(Calendar.MONTH)
                day = calendar.get(Calendar.DAY_OF_MONTH)
                var  reportDate = "$year-${month + 1}-$day"
                txtDate.text = reportDate
                formData.value = reportDate
                btnDatePicker.setOnClickListener {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            reportDate = "$year-${month + 1}-$day"
                            txtDate.text = reportDate
                            dataStore.formData!!.value = reportDate
                        }, year, month, day
                    ).show()

                }
                return dataStore
            }
            AFJUtils.UI_TYPE.TIME.name -> {
                val dataStore = StoreCustomFormData(null, formData)
                var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
                val textTitleLable = view.findViewById<TextView>(R.id.text_label)
                textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
                textTitleLable.setTextColor(Color.BLACK)
                containerChecks.addView(view)
                containerChecks.addView(addSpaceView( context))
                view =  context.layoutInflater.inflate(R.layout.layout_date_time_view, null)
                val txtDate = view.findViewById<TextView>(R.id.txtDate)
                val btnDatePicker = view.findViewById<RelativeLayout>(R.id.btnDatePicker)
                containerChecks.addView(view)
                var mHour: Int
                var mMinute: Int
                var  reportDate = ""
                val c = Calendar.getInstance()
                mHour = c[Calendar.HOUR_OF_DAY]
                mMinute = c[Calendar.MINUTE]
                reportDate = getTime(mHour, mMinute)
                txtDate.text = reportDate
                formData.value = reportDate
                btnDatePicker.setOnClickListener {
                    val timePickerDialog = TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            reportDate = getTime(hourOfDay, minute)
                            txtDate.text = reportDate
                            dataStore.formData!!.value = reportDate
                        },
                        mHour,
                        mMinute,
                        false
                    )
                    timePickerDialog.show()
                }
                return dataStore
            }

            else -> {
                AFJUtils.writeLogs("not thing to create view")
                return null
            }
        }
    }

    private fun addSpaceView(context: Context): View {
        // Create Space programmatically.
        val tv = Space(context)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            30
        )
        tv.layoutParams = layoutParams
        return tv

    }

    fun getSingleImageCallBackInFuelForm(context: AppCompatActivity,
                                         formData: Form,
                                         position: Int,
                                         lastOdoReading: Int? = null,
                                         uniqueFileId: String? = null,
                                         inpsectionType: String? = null,
                                         containerChecks: LinearLayout,
                                         onImagePathReceived :(path:String)->Unit
    )  : StoreCustomFormData
    {
        var view = context.layoutInflater.inflate(R.layout.layout_text_view, null)
        val textTitleLable = view.findViewById<TextView>(R.id.text_label)
        textTitleLable.text = formData.title + "${if (formData.required!!) "*" else ""}"
        textTitleLable.setTextColor(Color.BLACK)
          containerChecks.addView(view)

        view = context.layoutInflater.inflate(R.layout.layout_image_box, null)
        val imagePreview = view.findViewById<ImageView>(R.id.img_preview)
        val btnPickImage = view.findViewById<ImageView>(R.id.img_add)
        val btnImageDel = view.findViewById<ImageView>(R.id.img_del)
        imagePreview.visibility = View.GONE
        btnPickImage.visibility = View.VISIBLE
        btnImageDel.visibility = View.GONE

        btnPickImage.setOnClickListener {
            val dialog = FileUploadDialog.newInstance(
                isDocumentPickShow = false,
                inpsectionType = inpsectionType.toString(), //This will be change after
                uniqueFileId = uniqueFileId.toString(),
                fieldName = formData.fieldName!!,
                fileUploadListner = (object : UploadDialogListener {
                    override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                    }

                    override fun onFilePathReceived(path: String) {
                        formData.value = uniqueFileId.toString()
                        Glide.with(view.context)
                            .load(path)
                            .placeholder(
                                AppCompatResources.getDrawable(
                                    view.context,
                                    R.drawable.ic_no_image
                                )
                            )
                            .into(imagePreview)
                        btnPickImage.visibility = View.GONE
                        btnImageDel.visibility = View.VISIBLE
                        imagePreview.visibility = View.VISIBLE

                        onImagePathReceived.invoke(path)
                    }

                })
            )
            dialog.isCancelable = false
            dialog.show(context.supportFragmentManager, null)

        }
        btnImageDel.setOnClickListener {
            btnImageDel.visibility = View.GONE
            btnPickImage.visibility = View.VISIBLE
            imagePreview.visibility = View.GONE
        }

        //val dataStore = StoreFormData(null, formData)
        //storedData.add(dataStore)
        containerChecks.addView(view)
        return StoreCustomFormData(null, formData)
    }

    private fun getTime(hr: Int, min: Int): String {
        val tme = Time(hr, min, 0)
        val formatter: Format
        formatter = SimpleDateFormat("H:mm")
        return formatter.format(tme)
    }
}

data class StoreCustomFormData(
    val editText: EditText? = null,
    var formData: Form? = null,
    var isOdoMeterErrorFound: Boolean? = false,
    var lastOdoReading: Int? = 0
)


