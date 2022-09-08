package com.example.afjtracking.view.fragment.vehicle_weekly_inspection.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.InspectionCreateRequest
import com.example.afjtracking.model.requests.SavedWeeklyInspection
import com.example.afjtracking.model.requests.SingleInspectionRequest
import com.example.afjtracking.model.requests.WeeklyVehicleInspectionRequest
import com.example.afjtracking.model.responses.*
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.retrofit.SuccessCallback
import com.example.afjtracking.room.model.TableAPIData
import com.example.afjtracking.room.repository.ApiDataRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeeklyInspectionViewModel : ViewModel() {

    var _vehicleData = MutableLiveData<VehicleData>()
    var vehicleData: LiveData<VehicleData> = _vehicleData

    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow

    private val _isCompleted = MutableLiveData<Boolean>()
    val apiCompleted: LiveData<Boolean> = _isCompleted


    private val _hasData = MutableLiveData<Boolean>()
    val apiHasData: LiveData<Boolean> = _hasData


    val _weeklyInspectionCheck = MutableLiveData<WeeklyInspectionCheckData>()
    val weeklyInspectionCheck: LiveData<WeeklyInspectionCheckData> = _weeklyInspectionCheck

    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: WeeklyInspectionViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): WeeklyInspectionViewModel? {
            if (instance == null) instance = WeeklyInspectionViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun getWeeklyVehicleInspectionCheckList(
        context: Context?,
        body: WeeklyVehicleInspectionRequest
    ) {
        _dialogShow.postValue(true)
        getInstance(context)
        apiInterface!!.getWeeklyInspectionList(body)
            .enqueue(object : SuccessCallback<WeeklyInspectionListResponse?>() {

                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<WeeklyInspectionListResponse?>
                ) {
                    super.onSuccess(response)
                    var apiTableAPIData = TableAPIData(
                        apiName = Constants.WEEKLY_INSPECTION_CHECKS,
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
                    val resp = response.body()!!.data!!
                    _vehicleData.postValue(resp.vehicle!!)
                    _hasData.postValue(true)
                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    _hasData.postValue(false)
                    val exception = error
                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        fetchDataFromDBLastStore(context!!)
                    } else {
                        mErrorsMsg!!.postValue(exception)
                        _hasData.postValue(false)
                    }

                }

                override fun onFailure(response: Response<WeeklyInspectionListResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                    _hasData.postValue(false)
                }
            })

    }


    fun fetchDataFromDBLastStore(context: Context) {
        //Fetch Data from API
        ApiDataRepo.getApiSingleData(context, Constants.WEEKLY_INSPECTION_CHECKS).observeForever {

            if (it != null) {
                val response = AFJUtils.convertStringToObject(
                    it.apiGetResponse,
                    WeeklyInspectionListResponse::class.java
                )

                if (response.code == 200) {
                    val resp = response.data!!
                    _vehicleData.postValue(resp.vehicle!!)
                    _hasData.postValue(true)
                } else {
                    var errors = ""
                    for (i in response.errors.indices) {
                        errors = """
                                $errors${response.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)

                    _hasData.postValue(false)
                }
            } else {
                mErrorsMsg!!.postValue("No data found")
                _hasData.postValue(false)
            }
        }

    }


    fun createWeeklyInspectionRequest(context: Context?, body: InspectionCreateRequest) {
        _dialogShow.postValue(true)
        getInstance(context)
        apiInterface!!.createWeeklyInspection(body)
            .enqueue(object : SuccessCallback<LocationResponse?>() {

                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(

                    response: Response<LocationResponse?>
                ) {
                    val resp = response.body()!!.data!!
                    _isCompleted.postValue(true)
                }

                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors!!.indices) {
                        errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                    _isCompleted.postValue(false)
                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    _isCompleted.postValue(false)
                    val exception = error
                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {

                        fetchDataFromDBLastStore(context!!)
                    } else {
                        mErrorsMsg!!.postValue(exception)
                    }
                }

            })

    }


    fun saveWeeklyInspectionChecks(
        context: Context?,
        body: SavedWeeklyInspection,
        isOnBackPress: Boolean
    ) {
        _dialogShow.postValue(true)
        getInstance(context)
        apiInterface!!.saveWeeklyInspectionCheck(body)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }
                override fun onSuccess(

                    response: Response<LocationResponse?>
                ) {
                    super.onSuccess(response)
                     if (!isOnBackPress)
                       _isCompleted.postValue(response.body()!!.data!!.isCompleted)
                }

                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)
                    _isCompleted.postValue(false)
                    var errors = ""

                    for (i in response.body()!!.errors!!.indices) {
                        errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                    _hasData.postValue(false)
                }
                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    _isCompleted.postValue(false)
                    _dialogShow.postValue(false)

                    val exception = error
                    mErrorsMsg!!.postValue(exception)
                }

            })

    }

    fun getWeeklyInspectionCheckRequest(context: Context?, body: SingleInspectionRequest) {
        _dialogShow.postValue(true)
        getInstance(context)
        apiInterface!!.getWeeklyInspectionChecks(body)
            .enqueue(object : SuccessCallback<GetWeeklyInspectionChecksListResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }
                override fun onSuccess(

                    response: Response<GetWeeklyInspectionChecksListResponse?>
                ) {
                    super.onSuccess(response)
                            val resp = response.body()!!.data!!
                            _weeklyInspectionCheck.postValue(resp)
                            _hasData.postValue(true)
                }

                override fun onFailure(response: Response<GetWeeklyInspectionChecksListResponse?>) {
                    super.onFailure(response)
                    var errors = ""

                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                    _hasData.postValue(false)
                }

                override fun onAPIError(error: String) {
                    super.onAPIError(error)
                    _dialogShow.postValue(false)
                    _hasData.postValue(false)
                    val exception = error
                    mErrorsMsg!!.postValue(exception)
                }


            })

    }
}