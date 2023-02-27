package com.afjltd.tracking.view.fragment.forms.viewmodel

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afjltd.tracking.model.requests.FormRequest
import com.afjltd.tracking.model.requests.SaveFormRequest
import com.afjltd.tracking.model.responses.*
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.room.model.TableAPIData
import com.afjltd.tracking.room.repository.ApiDataRepo
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.Constants
import com.afjltd.tracking.utils.TextAnalyser
import com.google.mlkit.nl.entityextraction.*
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import com.afjltd.tracking.R
import com.afjltd.tracking.utils.ErrorCodes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.FileInputStream
import java.io.OutputStream
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.Error


class FormsViewModel : ViewModel() {


    val _vehicle = MutableSharedFlow<Vehicle>()
    val getVehicle=_vehicle.asSharedFlow()


    private val _dialogShow = MutableSharedFlow<Boolean>(1)
    val showDialog=_dialogShow.asSharedFlow()


    private val _dataUploaded = MutableSharedFlow<Boolean>()
    val apiUploadStatus= _dataUploaded.asSharedFlow()


    val _reportForm = MutableSharedFlow<List<Form>>()
    val getReportForm= _reportForm.asSharedFlow()


    val _formData = MutableSharedFlow<FormData>()
    val getFormData= _formData.asSharedFlow()


    private var mErrorsMsg = MutableSharedFlow<String>()
    val errorsMsg=mErrorsMsg.asSharedFlow()

    companion object {
        private var instance: FormsViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): FormsViewModel? {
            if (instance == null) instance = FormsViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun getReportFormRequest(context: Context?,formIdentifer:String) {
        getInstance(context)
        viewModelScope.launch {
            _dialogShow.emit(true)
        }

        apiInterface!!.getFormData(FormRequest(formIdentifer,AFJUtils.getDeviceDetail()))
            .enqueue(object : SuccessCallback<GetFormResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)

                    viewModelScope.launch {
                        _dialogShow.emit(show)
                    }
                }

                override fun onSuccess(

                    response: Response<GetFormResponse?>
                ) {

                    super.onSuccess(response)

                    var apiTableAPIData = TableAPIData(
                        apiName = Constants.REPORT_FORM,
                        apiPostData = "",
                        apiPostResponse = "",
                        apiGetResponse = AFJUtils.convertObjectToJson(response.body()!!),
                        apiError = "",
                        apiRequest = AFJUtils.getCurrentDateTime(),
                        apiRetryCount = 0,
                        lastTimeApiError = "",
                        apiRequestTime = "",
                        apiResponseTime = "",
                        errorPosted = "0"

                    )
                    ApiDataRepo.insertData(context!!, apiTableAPIData)


                    viewModelScope.launch {
                        _formData.emit(response.body()!!.data!!)
                        _vehicle.emit(response.body()!!.data!!.vehicle!!)
                        _reportForm.emit(response.body()!!.data!!.formList)
                    }

                }

                override fun onFailure(response: Response<GetFormResponse?>) {
                    super.onFailure(response)



                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    viewModelScope.launch {
                        _dataUploaded.emit(false)
                        mErrorsMsg.emit(errors)
                    }

                }


                override fun onAPIError(t: String) {

                    viewModelScope.launch {
                        _dataUploaded.emit(false)
                        _dialogShow.emit(false)
                    }
                    val exception = t.toString()

                    // mErrorsMsg!!.postValue(exception)
                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        fetchReportFromDBLastStore(context!!)
                    } else {
                        viewModelScope.launch {
                            mErrorsMsg.emit(exception)

                        }
                    }





                }
            })

    }

    fun fetchReportFromDBLastStore(context: Context) {

        ApiDataRepo.getApiSingleData(context, Constants.REPORT_FORM).observeForever {
            if (it != null) {
                val response = AFJUtils.convertStringToObject(
                    it.apiGetResponse,
                    GetFormResponse::class.java
                )

                if (response.code == 200) {
                    val resp = response.data!!
                    viewModelScope.launch {
                        _vehicle.emit(resp.vehicle!!)
                        _reportForm.emit(resp.formList)
                        _formData.emit(resp)
                    }

                } else {
                    var errors = ""

                    for (i in response.errors.indices) {
                        errors = """
                                $errors${response.errors[i].message}
                                
                                """.trimIndent()
                    }
                    viewModelScope.launch {
                        mErrorsMsg.emit(errors)
                    }


                }
            } else {

                viewModelScope.launch {
                    mErrorsMsg.emit(context.resources.getString(R.string.no_data_found))
                }

            }
        }
    }


    fun saveReportForm(form: SaveFormRequest, context: Context?) {
        getInstance(context)

        viewModelScope.launch {
            _dialogShow.emit(true)
        }
        apiInterface!!.saveForms(form)
            .enqueue(object : SuccessCallback<LocationResponse?>() {

                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    viewModelScope.launch {
                        _dialogShow.emit(show)
                    }
                }

                override fun onSuccess(
                    response: Response<LocationResponse?>
                ) {


                    viewModelScope.launch {
                        _dialogShow.emit(true)
                    }
                    super.onSuccess(response)
                }
                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)

                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }


                    viewModelScope.launch {
                        _dataUploaded.emit(false)
                        mErrorsMsg.emit(errors)
                    }
                }

                override fun onAPIError(error: String) {
                    val exception =error


                    viewModelScope.launch {
                        _dataUploaded.emit(false)
                        mErrorsMsg.emit(exception)
                        _dialogShow.emit(false)
                    }
                    super.onAPIError(error)

                }
            })

    }


    fun getBitmap(f:File): Bitmap{
        var bitmap:Bitmap?=null
        try{

            var options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f),null,options)
        }catch (e:Exception){

        }

        val grey =  toGrayscale(bitmap as Bitmap)
        return grey//createContrast(grey ,0.5)
    }


    suspend fun performAction(f:File): Bitmap =
        withContext(Dispatchers.Default){
            //do long work
            val sum:Bitmap = getBitmap(f)
            return@withContext sum
        }




    fun getTextFromFuelSlip(file : File,context: Context,callback:(Any)->Unit)
    {

        GlobalScope.launch(Dispatchers.IO){
         val myresult = performAction(file)
          saveBitmap(myresult,file.toUri(),context) //in to same path
            TextAnalyser({ result ->
                if (result.text.isEmpty()) {
                    Toast.makeText(
                        context,
                        "No Text Detected",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //result
                   // parseFuelStringData(result,context)
                    processTextBlock(result,callback)
                }

            }, context, Uri.fromFile(file)).analyseImage()

        }


    }
    fun saveBitmap(target: Bitmap, uri: Uri?,context:Context):Bitmap {
        try {
            val output: OutputStream? = context.contentResolver.openOutputStream(uri!!)
            target.compress(Bitmap.CompressFormat.JPEG, 100, output)

        } catch (e: Exception) {
            Log.d("onBtnSavePng", e.toString()) // java.io.IOException: Operation not permitted
        }
        return target
    }
    fun createContrast(src: Bitmap, value: Double): Bitmap {
        // image size
        val width = src.width
        val height = src.height
        // create output bitmap
        val bmOut = Bitmap.createBitmap(width, height, src.config)
        // color information
        var A: Int
        var R: Int
        var G: Int
        var B: Int
        var pixel: Int
        // get contrast value
        val contrast = Math.pow((100 + value) / 100, 2.0)

        // scan through all pixels
        for (x in 0 until width) {
            for (y in 0 until height) {
                // get pixel color
                pixel = src.getPixel(x, y)
                A = Color.alpha(pixel)
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel)
                R = (((R / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
                if (R < 0) {
                    R = 0
                } else if (R > 255) {
                    R = 255
                }
                G = Color.red(pixel)
                G = (((G / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
                if (G < 0) {
                    G = 0
                } else if (G > 255) {
                    G = 255
                }
                B = Color.red(pixel)
                B = (((B / 255.0 - 0.5) * contrast + 0.5) * 255.0).toInt()
                if (B < 0) {
                    B = 0
                } else if (B > 255) {
                    B = 255
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B))
            }
        }
        return bmOut
    }

    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width: Int
        val height: Int
        height = bmpOriginal.height
        width = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }



    private fun processTextBlock(result: Text,callback:(Any)->Unit) {

        val callBackData = mutableMapOf<String,String>()
        val resultText = result.text
        for (block in result.textBlocks.indices) {
            var blockText = result.textBlocks[block].text


            if(blockText.lowercase().contains("pum") || blockText.lowercase().contains("pun") )
            {
                try {
                    AFJUtils.writeLogs(blockText)
                if(blockText.contains(":"))
                {
                    blockText= blockText.split(":")[1]
                }

                val data = blockText.split(" ")
                val re = Regex("[^.0-9 ]")
                val qty =  re.replace(data[1], "")
                val liter =  re.replace(data[2], "")
                val totalPrice =qty.toDouble() * liter.toDouble()
                val df = DecimalFormat("###########.##")
                val price = df.format(totalPrice)

                    callBackData["total_liter"] = qty
                    callBackData["per_liter"] = liter
                    callBackData["total_price"] = price


               if(liter.toDouble() < 9)
               {
                   callback(callBackData)
               }


                }
                catch (e:Exception)
                {
                    callback(ErrorCodes.receiptScanInfo)
                }
            }

            if(blockText.lowercase().contains("700676") )
            {
                AFJUtils.writeLogs("total = $blockText")
                val ccPattern = "(^\\s*(?:\\S\\s*){18}\$)"
                val pattern: Pattern = Pattern.compile(ccPattern)
                val matcher: Matcher =
                    pattern.matcher(blockText)
                if (matcher.find()) {
                    println("Pattern matches")

                    AFJUtils.writeLogs("PATTERN = ${matcher.group(0)}")
                } else {
                    println("Does not matches")
                }
                val extractCardNumber = blockText.substringAfter("bp")
                //AFJUtils.writeLogs("subtract = $extractCardNumber")
                callBackData["card_number"] = blockText
                callback(callBackData)
            }

        }

        callback(ErrorCodes.receiptScanMsg)
    }

}