package com.afjltd.tracking.service.worker.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afjltd.tracking.model.requests.ErrorRequest
import com.afjltd.tracking.model.responses.InspectionCheckData
import com.afjltd.tracking.model.responses.LocationResponse
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.room.model.TableAPIData
import com.afjltd.tracking.room.repository.ApiDataRepo
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response

class ApiServiceViewModel : ViewModel() {


    companion object {
        private var instance: ApiServiceViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): ApiServiceViewModel? {
            if (instance == null) instance = ApiServiceViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context, false).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }

    var apiCallCounter =0
    lateinit var liveAPIData: LiveData<TableAPIData>
    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()

    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }

     val apiRequestStatus = MutableLiveData<Boolean>()
    val getApiRequestStatus: LiveData<Boolean>
        get() = apiRequestStatus

    private val toastMsg = MutableLiveData<String?>()
    val showToastMsg: LiveData<String?>
        get() = toastMsg

    fun insertDataToTable(context: Context, tableData: TableAPIData) {
        ApiDataRepo.insertData(context, tableData)
    }

    fun updateApiDataValue(context: Context, tableData: TableAPIData) {
        ApiDataRepo.updateApiData(context, tableData)
    }

    fun getAllErrorData(context: Context): List<TableAPIData> {
        val listUncompletedFiles = ApiDataRepo.getErrorResultsData(context)
        return listUncompletedFiles
    }

    fun getAllPendingRequest(context: Context): List<TableAPIData> {
        val listUncompletedFiles = ApiDataRepo.getUnCompletedRequest(context)
        return listUncompletedFiles
    }

    fun sendApiRequest( context: Context, tableAPIData: TableAPIData )
    {
        getInstance(context)
        when (tableAPIData.apiName)
        {
            Constants.DAILY_INSPECTION_CHECKS ->
            {
                callInspectionApi(context,tableAPIData.apiName)
            }
        }
    }

   fun callInspectionApi(context: Context,apiName: String) {

       GlobalScope.launch {
           withContext(Dispatchers.IO) {
               val it = ApiDataRepo.getApiData(context, apiName)
               val requestBody = AFJUtils.convertStringToObject(it.apiPostData,
                   InspectionCheckData::class.java
               )
               apiInterface!!.postVehicleDailyInspection(requestBody)
                   .enqueue(object : SuccessCallback<LocationResponse?>() {
                       override fun onSuccess(

                           response: Response<LocationResponse?>
                       ) {
                           super.onSuccess(response)

                                   apiRequestStatus.postValue(true)

                       }

                       override fun onFailure(response: Response<LocationResponse?>) {
                           super.onFailure(response)
                           var errors = ""
                           for (i in response.body()!!.errors.indices) {
                               errors = "$errors${response.body()!!.errors[i].message}"
                           }
                           mErrorsMsg!!.postValue(errors)
                       }

                       override fun onAPIError(error: String) {
                           super.onAPIError(error)
                           mErrorsMsg!!.postValue(error)
                       }


                   }
                   )

           }

       }
   }
    fun uploadErrorData(
        context: Context,
        fileUploadData: TableAPIData

    ) {

      getInstance(context)
        val body=   ErrorRequest(
            deviceId = Constants.DEVICE_ID,
            endpoint = fileUploadData.apiName,
            error = fileUploadData.apiError,
            retries =  fileUploadData.apiRetryCount.toString()

        )
        apiInterface!!.postErrorData(body).enqueue(object : SuccessCallback<LocationResponse?>() {
            override fun onResponse(
                call: Call<LocationResponse?>,
                response: Response<LocationResponse?>
            ) {
                         fileUploadData.apiResponseTime = AFJUtils.getCurrentDateTime()
                         AFJUtils.writeLogs("********* Error Upload Data Completed *********")

                        fileUploadData.apiStatus = 0
                        fileUploadData.errorPosted = "1"
                        updateApiDataValue(context, fileUploadData)
                        AFJUtils.writeLogs("********* Backup Api Request Completed *********")
            }

            override fun onFailure(response: Response<LocationResponse?>) {
                super.onFailure(response)
                var errors = ""
                for (i in response.body()!!.errors.indices) {
                    errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                }
                AFJUtils.writeLogs("********* Error Upload Data Api=$errors *********")
            }
            override fun onAPIError(error: String) {
                super.onAPIError(error)
                AFJUtils.writeLogs("********* Error Upload Data Api=${error} *********")
            }

        })

    }

}

