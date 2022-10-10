package com.example.afjtracking.view.fragment.fuel.viewmodel


import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.GetFuelFormResponse
import com.example.afjtracking.model.responses.InspectionForm
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.retrofit.SuccessCallback
import com.example.afjtracking.utils.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.TextBlock
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import retrofit2.Response


class FuelViewModel : ViewModel() {
    @JvmField
    var EmailAddress = MutableLiveData<String>()

    @JvmField
    var Password = MutableLiveData<String>()


    val _fuelForm = MutableLiveData<List<InspectionForm>>()
    val getFuelForm: LiveData<List<InspectionForm>> = _fuelForm


    val _vehicle = MutableLiveData<Vehicle>()
    val getVehicle: LiveData<Vehicle> = _vehicle


    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow


    private val _dataUploaded = MutableLiveData<Boolean>()
    val apiUploadStatus: LiveData<Boolean> = _dataUploaded


    private var userMutableLiveData: MutableLiveData<LoginRequest>? = null
    val user: MutableLiveData<LoginRequest>
        get() {
            if (userMutableLiveData == null) {
                userMutableLiveData = MutableLiveData()
            }
            return userMutableLiveData!!
        }


    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: FuelViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): FuelViewModel? {
            if (instance == null) instance = FuelViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }

    fun onClick(view: View?) {
        val loginUser = LoginRequest(
            EmailAddress.value.toString(),
            Password.value.toString(), ""
        )

        userMutableLiveData!!.postValue(loginUser)
    }

    fun getFuelFormRequest(context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getFuelForm()
            .enqueue(object : SuccessCallback<GetFuelFormResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onFailure(response: Response<GetFuelFormResponse?>) {
                    super.onFailure(response)
                    _dataUploaded.postValue(false)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }

                override fun onSuccess(

                    response: Response<GetFuelFormResponse?>
                ) {
                    _vehicle.postValue(response.body()!!.data!!.vehicle!!)
                    _fuelForm.postValue(response.body()!!.data!!.fuelForm)
                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    val exception = error
                    mErrorsMsg!!.postValue(exception)

                    _dataUploaded.postValue(false)
                }


            })

    }

    fun saveFuelForm(form: SaveFormRequest, context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.saveFuelForm(form)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<LocationResponse?>
                ) {
                    _dataUploaded.postValue(true)
                }

                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)
                    _dataUploaded.postValue(false)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    val exception = error
                    _dataUploaded.postValue(false)
                    mErrorsMsg!!.postValue(exception)
                    _dialogShow.postValue(false)
                }

            })

    }



     fun runTextRecognition(imagePath: String,context: Context) {
        val picBitmap = BitmapFactory.decodeFile(imagePath)
        val image = InputImage.fromBitmap(picBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts,context)
            }
            .addOnFailureListener { e ->
                AFJUtils.writeLogs("Exception:==$e")
            }
    }


    //perform operation on the full text recognized in the image.
    private fun processTextRecognitionResult(texts: Text,context: Context) {
        AFJUtils.writeLogs("Fuel Text=== ${texts.text}")
        val blocks: List<TextBlock> = texts.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(
                context,
                "No text found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        else
        processDataText(blocks)
    }


    fun processDataText(textBlocks:List<TextBlock> )
    {

        val strBuilder = StringBuilder()
        for (i in textBlocks.indices) {
            val item: TextBlock = textBlocks.get(i)
            strBuilder.append(item.text)
            strBuilder.append("/")
        }

        //Regex Operation
        val str = strBuilder.toString()
        val str2 = str.replace("/", "\n")
        val str1 = str2.split("\n").toTypedArray()

        if(str1.isNotEmpty()) {
            val companyName = str1[0] +"\n"+ str1[1]

             val dateLabel = StringBuilder()
            val totalPriceLabel = StringBuilder()
            val itemsLabel = StringBuilder()
            for (i in str1.indices) {
                if(str1[i].lowercase().isTotaPriceandTax()) {
                    totalPriceLabel.append(str1[i] +" "+str1[i+1] +"\n")
                }
                else if( str1[i].lowercase().isDateString())
                {
                    dateLabel.append(str1[i] )
                }
                else if(str1[i].lowercase().isItemPrices())
                {
                    itemsLabel.append(str[i-1])
                    itemsLabel.append(str[i] +"\n")
                }
            }

            AFJUtils.writeLogs("Fuel Header=$companyName")
            AFJUtils.writeLogs("Fuel Date=  $dateLabel")
            AFJUtils.writeLogs("Fuel Items=  $itemsLabel")
            AFJUtils.writeLogs("Fuel Footer=$totalPriceLabel")
        }






    }


}