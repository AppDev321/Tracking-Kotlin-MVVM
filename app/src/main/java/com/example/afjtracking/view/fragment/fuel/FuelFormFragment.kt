package com.example.afjtracking.view.fragment.fuel

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentFuelFromBinding
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.FuelForm
import com.example.afjtracking.model.responses.UploadFileAPiResponse
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.fileupload.FileUploadDialog
import com.example.afjtracking.view.fragment.fileupload.UploadDialogListener
import com.example.afjtracking.view.fragment.fuel.viewmodel.FuelViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File

class FuelFormFragment : Fragment() {

    private var _binding: FragmentFuelFromBinding? = null
    private val binding get() = _binding!!

    var storedData: ArrayList<StoreFuelFormData> = arrayListOf()

    lateinit var fuelViewModel: FuelViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity
    val uniqueUploadId = Constants.FILE_UPLOAD_UNIQUE_ID
    var requestType = ""

    var imageForm: ArrayList<FuelForm> = arrayListOf()
    var formIndex: ArrayList<Int> = arrayListOf()
    var isOdoMeterErrorFound = false
    var lastOdoReading = 0
    var odoReadingError = ""
    lateinit var txtErrorMsg: TextView

    var vehicle: Vehicle = Vehicle()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {


        fuelViewModel = ViewModelProvider(this).get(FuelViewModel::class.java)
        _binding = FragmentFuelFromBinding.inflate(inflater, container, false)


        val root: View = binding.root
        txtErrorMsg = binding.txtErrorMsg

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
                    fuelViewModel.getFuelFormRequest(mBaseActivity)
                } else {
                    binding.root.removeAllViews()
                    binding.root.addView(authView)
                }

            }
        })




        fuelViewModel.showDialog.observe(viewLifecycleOwner) {
            mBaseActivity.showProgressDialog(it)
        }





        fuelViewModel.getFuelForm.observe(viewLifecycleOwner) {
            if (it != null) {
                try {
                    showInspectionCreationForm(it)
                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
                fuelViewModel._fuelForm.value = null
            }
        }

        fuelViewModel.getVehicle.observe(viewLifecycleOwner) {
            if (it != null) {
                vehicle = it
                fuelViewModel._vehicle.value = null
            }
        }


        fuelViewModel.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mBaseActivity.toast(it, false)
                binding.txtErrorMsg.visibility = View.VISIBLE
                binding.txtErrorMsg.text = it.toString()
                binding.baseLayout.visibility = View.GONE
                fuelViewModel.errorsMsg.value = null
            }
        })

        fuelViewModel.apiUploadStatus.observe(viewLifecycleOwner, {
            if (it) {
                mBaseActivity.onBackPressed()
                mBaseActivity.showSnackMessage(
                    "Request saved", requireView()
                )
            }
        })




        return root
    }


    fun showInspectionCreationForm(fuelList: List<FuelForm>) {

        binding.baseLayout.visibility = View.VISIBLE
        binding.txtErrorMsg.visibility = View.GONE

        binding.txtInspectionTitle.text = "Fuel Form"

        val odoReading = vehicle.odometerReading
        lastOdoReading = if (odoReading!!.isEmpty()) 0 else odoReading.toInt()
        odoReadingError = "Cannot less than previous reading $lastOdoReading"

        for (i in fuelList.indices) {
            val formData = fuelList[i]
            try {
                createViewChecks(formData.type!!.uppercase(), formData, i)
            } catch (e: Exception) {
                mBaseActivity.writeExceptionLogs(e.toString())
            }
        }

        /* //Create Form data
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
         }*/

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


            if (isAllImageUploaded && isOdoMeterErrorFound == false) {
                var isAllRequired = true
                for (i in storedData.indices) {
                    fuelList[i].value = storedData[i].formData!!.value

                    if (storedData[i].formData?.required == true) {
                        if (fuelList[i].value!!.isEmpty() == true) {

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
                    request.fuelForm = fuelList
                    fuelViewModel.saveFuelForm(request, mBaseActivity)


                }
            } else {
                mBaseActivity.showSnackMessage(odoReadingError, binding.root)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.removeAllViews()
        _binding = null


    }


    fun createViewChecks(uiType: String, formData: FuelForm, position: Int) {
        val containerChecks = binding.layoutVdiForm
        when (uiType) {
            resources.getString(R.string.ui_type_text) -> {
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

                val dataStore = StoreFuelFormData(inputText, formData)


                dataStore.editText!!.onFocusChangeListener = object : View.OnFocusChangeListener {
                    override fun onFocusChange(v: View?, hasFocus: Boolean) {
                        if (!hasFocus) {
                            val s = dataStore.formData!!.value
                            if (formData.title!!.lowercase().contains("odometer")) {
                                if (!s.toString().isEmpty()) {
                                    val reading = Integer.parseInt(s.toString())
                                    if (reading < lastOdoReading) {
                                        mBaseActivity.showSnackMessage(
                                            odoReadingError,
                                            binding.root
                                        )
                                        dataStore.editText.error = odoReadingError
                                        isOdoMeterErrorFound = true
                                    } else {
                                        isOdoMeterErrorFound = false
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
            resources.getString(R.string.ui_type_file) -> {
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

                val dataStore = StoreFuelFormData(null, formData)
                storedData.add(dataStore)
                containerChecks.addView(view)

            }
            resources.getString(R.string.ui_type_image) -> {

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

                val dataStore = StoreFuelFormData(null, formData)
                storedData.add(dataStore)
                imageForm.add(formData)
                formIndex.add(position)

            }
            else -> {
                AFJUtils.writeLogs("not thing to create view")
            }
        }


    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = mBaseActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }


}

data class StoreFuelFormData(
    val editText: EditText? = null,
    var formData: FuelForm? = null


)
