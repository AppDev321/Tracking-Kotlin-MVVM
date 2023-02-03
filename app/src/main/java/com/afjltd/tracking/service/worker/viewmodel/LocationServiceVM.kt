package com.afjltd.tracking.service.worker.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.afjltd.tracking.model.requests.ErrorRequest
import com.afjltd.tracking.model.requests.LocationApiRequest
import com.afjltd.tracking.model.responses.LocationResponse
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.room.model.TableLocation
import com.afjltd.tracking.room.repository.LocationTableRepo
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response

class LocationServiceVM : ViewModel() {


    companion object {
        private var instance: LocationServiceVM? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): LocationServiceVM? {
            if (instance == null) instance = LocationServiceVM()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context, false).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }

    var apiCallCounter = 0
    lateinit var liveAPIData: LiveData<TableLocation>
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

    fun insertDataToTable(context: Context, tableData: TableLocation) {
        LocationTableRepo.insertLocationData(context, tableData)
    }

    fun updateApiDataValue(context: Context, tableData: TableLocation) {
        LocationTableRepo.updateLocationData(context, tableData)
    }

    fun getAllErrorData(context: Context): List<TableLocation> {
        val listUncompletedFiles = LocationTableRepo.getErrorResultsData(context)
        return listUncompletedFiles
    }

    fun getAllPendingRequest(context: Context): List<TableLocation> {
        val listUncompletedFiles = LocationTableRepo.getUnCompletedLocationRequest(context)
        return listUncompletedFiles
    }

    fun sendApiRequest(context: Context, tableAPIData: TableLocation) {
        getInstance(context)
        callLocationApi(context, tableAPIData)

    }

    fun callLocationApi(context: Context, apiRequest: TableLocation) {

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val requestBody = AFJUtils.convertStringToObject(
                    apiRequest.apiPostData,
                    LocationApiRequest::class.java
                )

                apiInterface!!.updateLocation(requestBody)
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

                    })

            }
        }

    }

    fun uploadErrorData(context: Context, locationTable: TableLocation) {

        getInstance(context)
        val body = ErrorRequest(
            deviceId = Constants.DEVICE_ID,
            endpoint = locationTable.apiName,
            error = locationTable.apiError,
            retries = locationTable.apiRetryCount.toString()

        )
        apiInterface!!.postErrorData(body).enqueue(object : SuccessCallback<LocationResponse?>() {
            override fun onResponse(
                call: Call<LocationResponse?>,
                response: Response<LocationResponse?>
            ) {
                locationTable.apiResponseTime = AFJUtils.getCurrentDateTime()
                AFJUtils.writeLogs("********* Error Upload Data Completed *********")
                locationTable.apiStatus = 0
                locationTable.errorPosted = "1"
                updateApiDataValue(context, locationTable)
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


