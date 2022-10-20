package com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.R
import com.example.afjtracking.model.requests.DailyInspectionListRequest
import com.example.afjtracking.model.requests.SingleInspectionRequest
import com.example.afjtracking.model.responses.*
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.retrofit.SuccessCallback
import com.example.afjtracking.room.model.TableAPIData
import com.example.afjtracking.room.repository.ApiDataRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import retrofit2.Call
import retrofit2.Response


class DailyInspectionViewModel : ViewModel() {

    val _inspectionData = MutableLiveData<InspectionCheckData>()
    val inspectionChecksData: LiveData<InspectionCheckData> = _inspectionData

    val _vehicleClassChecks = MutableLiveData<ArrayList<PTSCheck>>()
    val getInspcetionClassList: LiveData<ArrayList<PTSCheck>> = _vehicleClassChecks


    private val _checkedClass = MutableLiveData<ArrayList<PSVCheck>>()
    val _getCheckedClass: LiveData<ArrayList<PSVCheck>> = _checkedClass

    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow

    val _vehicleInfo = MutableLiveData<Vehicle>()
    val getVehicleInfo: LiveData<Vehicle> = _vehicleInfo

     var _dataUploaded = MutableLiveData<Boolean>()
    val apiUploadStatus: LiveData<Boolean> = _dataUploaded


    val _inspections = MutableLiveData<List<Inspections>>()
    val getInspectionList: LiveData<List<Inspections>> = _inspections

    val _inspectionReviewData = MutableLiveData<InspectionReviewData>()
    val getInspectionReviewData: LiveData<InspectionReviewData> = _inspectionReviewData

    private var startPage = 0
    private var limit = 10


    var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: DailyInspectionViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): DailyInspectionViewModel? {
            if (instance == null) instance = DailyInspectionViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun getDailyInspectionList(context: Context,body:DailyInspectionListRequest) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getDailyInspectionList(body)
            .enqueue(object : SuccessCallback<GetDailyInspectionList?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<GetDailyInspectionList?>
                ) {
                    super.onSuccess(response)
                    var apiTableAPIData = TableAPIData(
                        apiName = Constants.DAILY_INSPECTION_LIST,
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
                    ApiDataRepo.insertData(context, apiTableAPIData)
                    val res = response.body()!!.data!!

                    _inspections.postValue(res.inspections)
                    _vehicleInfo.postValue(res.vehicle!!)
                }

                override fun onFailure(response: Response<GetDailyInspectionList?>) {
                    super.onFailure(response)
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
                    _dialogShow.postValue(false)

                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        fetchDataFromDBDailyInspection(context)
                    } else {
                        mErrorsMsg!!.postValue(exception)
                    }

                }


            })
    }


    fun getDailyInspectionReview(context: Context, id: Int) {
        var body = SingleInspectionRequest(dailyInspectionId = id.toString())
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getDailyInspectionReview(body)
            .enqueue(object : SuccessCallback<GetDailyInspectionReview?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(

                    response: Response<GetDailyInspectionReview?>
                ) {
                    super.onSuccess(response)
                    val res = response.body()!!.data!!
                    _inspectionReviewData.postValue(res)
                }

                override fun onFailure(response: Response<GetDailyInspectionReview?>) {
                    super.onFailure(response)
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
                    _dialogShow.postValue(false)
                    mErrorsMsg!!.postValue(exception)
                }

            })
    }

    fun getDailyVehicleInspectionCheckList(context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getVehicleDailyInspectionCheckList()
            .enqueue(object : SuccessCallback<GetDailInspectionCheckListResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(

                    response: Response<GetDailInspectionCheckListResponse?>
                ) {


                    var apiTableAPIData = TableAPIData(
                        apiName = Constants.DAILY_INSPECTION_CHECKS,
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
                    _vehicleInfo.postValue(resp.vehicle!!)
                    _inspectionData.postValue(resp)
                    if (resp.ptsChecks.size > 0) {
                        _vehicleClassChecks.postValue(resp.ptsChecks)
                    } else if (resp.psvChecks.size > 0) {
                        _checkedClass.postValue(resp.psvChecks)
                    }
                }

                override fun onFailure(response: Response<GetDailInspectionCheckListResponse?>) {
                    super.onFailure(response)
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
                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        fetchDataFromDBLastStore(context!!)
                    } else {
                        mErrorsMsg!!.postValue(exception)
                    }
                }


            })

    }

    fun fetchDataFromDBLastStore(context: Context) {

        ApiDataRepo.getApiSingleData(context, Constants.DAILY_INSPECTION_CHECKS).observeForever {
            if (it != null) {
                val response = AFJUtils.convertStringToObject(
                    it.apiGetResponse,
                    GetDailInspectionCheckListResponse::class.java
                )
                if (response.code == 200) {
                    val resp = response.data!!
                    _inspectionData.postValue(resp)
                    _vehicleInfo.postValue(resp.vehicle!!)
                    if (resp.ptsChecks.size > 0) {
                        _vehicleClassChecks.postValue(resp.ptsChecks)
                    } else if (resp.psvChecks.size > 0) {
                        _checkedClass.postValue(resp.psvChecks)
                    }
                } else {
                    var errors = ""

                    for (i in response.errors.indices) {
                        errors = """
                                $errors${response.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }
            } else {
                mErrorsMsg!!.postValue(context.resources.getString(R.string.no_data_found))
            }
        }
    }

    fun fetchDataFromDBDailyInspection(context: Context) {
        ApiDataRepo.getApiSingleData(context, Constants.DAILY_INSPECTION_LIST).observeForever {
            if (it != null) {
                val response = AFJUtils.convertStringToObject(
                    it.apiGetResponse,
                    GetDailyInspectionList::class.java
                )
                if (response.code == 200) {
                    val resp = response.data!!
                    _inspections.postValue(resp.inspections)
                    _vehicleInfo.postValue(resp.vehicle!!)
                } else {
                    var errors = ""
                    for (i in response.errors.indices) {
                        errors = """
                                $errors${response.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }
            } else {
                mErrorsMsg!!.postValue(context.resources.getString(R.string.no_data_found))
            }
        }
    }

    fun postInspectionVDI(context: Context, body: InspectionCheckData) {
        getInstance(context)
        _dialogShow.postValue(true)
        ApiDataRepo.getApiSingleData(context, Constants.DAILY_INSPECTION_CHECKS)
            .observeForever {
                it.apiPostData = AFJUtils.convertObjectToJson(body)
                ApiDataRepo.insertData(context, it)
            }
        apiInterface!!.postVehicleDailyInspection(body)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(

                    response: Response<LocationResponse?>
                ) {


                    _dataUploaded.postValue(true)
                    val liveData = ApiDataRepo.getApiSingleData(
                        context,
                        Constants.DAILY_INSPECTION_CHECKS
                    )
                    liveData.observe(context as AppCompatActivity) {
                        it.apiStatus = 1
                        it.apiPostResponse = response.body()!!.data!!.message!!
                        ApiDataRepo.updateApiData(context, it)
                        liveData.removeObservers(context)
                    }

                }

                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)

                    var errors = ""

                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)


                    val liveData = ApiDataRepo.getApiSingleData(
                        context,
                        Constants.DAILY_INSPECTION_CHECKS
                    )
                    liveData.observe(context as AppCompatActivity) {
                        it.apiStatus = 0
                        it.apiError = errors
                        it.apiRetryCount = it.apiRetryCount + 1
                        it.lastTimeApiError = AFJUtils.getCurrentDateTime()
                        ApiDataRepo.updateApiData(context, it)

                        liveData.removeObservers(context)
                    }

                }

                override fun onAPIError(error: String) {
                    val exception = error
                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        _dataUploaded.postValue(true)
                    } else {
                        mErrorsMsg!!.postValue(exception)
                    }
                    val liveData =
                        ApiDataRepo.getApiSingleData(context, Constants.DAILY_INSPECTION_CHECKS)
                    liveData.observe(context as AppCompatActivity) {
                        it.apiStatus = 0
                        it.apiRetryCount = it.apiRetryCount + 1
                        it.apiError = error
                        it.lastTimeApiError = AFJUtils.getCurrentDateTime()
                        ApiDataRepo.updateApiData(context, it)
                        liveData.removeObservers(context)
                    }
                }

                override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                    _dialogShow.postValue(false)


                }
            })

    }


    fun postChecksInspection(context: Context, body: InspectionReviewData) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.postReviewInspectionChecks(body)
            .enqueue(object : SuccessCallback<LocationResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(

                    response: Response<LocationResponse?>
                ) {
                    super.onSuccess(response)
                    _dataUploaded.postValue(true)
                }

                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)
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
                    mErrorsMsg!!.postValue(exception)
                    _dataUploaded.postValue(false)
                }


            })

    }


}