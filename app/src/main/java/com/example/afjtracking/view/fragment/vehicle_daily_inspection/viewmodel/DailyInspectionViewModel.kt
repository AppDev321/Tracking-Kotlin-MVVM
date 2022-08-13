package com.example.afjtracking.view.fragment.vehicle_daily_inspection.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.DailyInspectionListRequest
import com.example.afjtracking.model.requests.SingleInspectionRequest
import com.example.afjtracking.model.responses.*
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.room.model.TableAPIData
import com.example.afjtracking.room.repository.ApiDataRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import retrofit2.Call
import retrofit2.Callback
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

    private val _dataUploaded = MutableLiveData<Boolean>()
    val apiUploadStatus: LiveData<Boolean> = _dataUploaded



    val _inspections = MutableLiveData<List<Inspections>>()
    val getInspectionList: LiveData<List<Inspections> > = _inspections

    val _inspectionReviewData  = MutableLiveData<InspectionReviewData>()
    val getInspectionReviewData : LiveData<InspectionReviewData> = _inspectionReviewData

    private var startPage= 0
    private var limit =10


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


    fun getDailyInspectionList(context: Context){
       var body = DailyInspectionListRequest(startPage,limit)
                getInstance(context)
                _dialogShow.postValue(true)
                apiInterface!!.getDailyInspectionList(body)
                    .enqueue(object : Callback<GetDailyInspectionList?> {
                        override fun onResponse(
                            call: Call<GetDailyInspectionList?>,
                            response: Response<GetDailyInspectionList?>
                        ) {
                            _dialogShow.postValue(false)
                            if (response.body() != null) {
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

                                if (response.body()!!.code == 200) {
                                    val res = response.body()!!.data!!

                                    _inspections.postValue(res.inspections!!)
                                    _vehicleInfo.postValue(res.vehicle!!)

                                } else {
                                    var errors = ""
                                    for (i in response.body()!!.errors.indices) {
                                        errors = """
                                        $errors${response.body()!!.errors[i].message}
                                        
                                        """.trimIndent()
                                    }
                                    mErrorsMsg!!.postValue(errors)
                                }
                            } else {
                                mErrorsMsg!!.postValue(response.errorBody().toString())
                            }

                        }

                        override fun onFailure(
                            call: Call<GetDailyInspectionList?>,
                            t: Throwable
                        ) {
                            val exception = t.toString()
                            _dialogShow.postValue(false)

                            if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                                fetchDataFromDBDailyInspection(context!!)
                            } else {
                                mErrorsMsg!!.postValue(exception)
                            }
                        }
                    })
    }


    fun getDailyInspectionReview(context: Context,id :Int){
        var body = SingleInspectionRequest(dailyInspectionId = id.toString())
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getDailyInspectionReview(body)
            .enqueue(object : Callback<GetDailyInspectionReview?> {
                override fun onResponse(
                    call: Call<GetDailyInspectionReview?>,
                    response: Response<GetDailyInspectionReview?>
                ) {
                    _dialogShow.postValue(false)
                    if (response.body() != null) {

                        if (response.body()!!.code == 200) {
                            val res = response.body()!!.data!!
                            _inspectionReviewData.postValue(res)

                        } else {
                            var errors = ""
                            for (i in response.body()!!.errors.indices) {
                                errors = """
                                        $errors${response.body()!!.errors[i].message}
                                        
                                        """.trimIndent()
                            }
                            mErrorsMsg!!.postValue(errors)
                        }
                    } else {
                        mErrorsMsg!!.postValue(response.errorBody().toString())
                    }

                }

                override fun onFailure(
                    call: Call<GetDailyInspectionReview?>,
                    t: Throwable
                ) {
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                    mErrorsMsg!!.postValue(exception)

                }
            })
    }

    fun getDailyVehicleInspectionCheckList(context: Context?){
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getVehicleDailyInspectionCheckList()
            .enqueue(object : Callback<GetDailInspectionCheckListResponse?> {
                override fun onResponse(
                    call: Call<GetDailInspectionCheckListResponse?>,
                    response: Response<GetDailInspectionCheckListResponse?>
                ) {
                    _dialogShow.postValue(false)
                    if (response.body() != null) {


                        if (response.body()!!.code == 200) {


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
                        } else {
                            var errors = ""
                            for (i in response.body()!!.errors!!.indices) {
                                errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                            }
                            mErrorsMsg!!.postValue(errors)
                        }
                    } else {
                        mErrorsMsg!!.postValue(response.errorBody().toString())
                    }

                }

                override fun onFailure(
                    call: Call<GetDailInspectionCheckListResponse?>,
                    t: Throwable
                ) {
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                   // mErrorsMsg!!.postValue(exception)
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
                val response = AFJUtils.convertStringToObject(it.apiGetResponse,GetDailInspectionCheckListResponse::class.java)

                if (response!!.code == 200) {
                    val resp = response!!.data!!
                        _inspectionData.postValue(resp)
                    _vehicleInfo.postValue(resp.vehicle!!)
                    if (resp.ptsChecks.size > 0) {
                        _vehicleClassChecks.postValue(resp.ptsChecks)
                    } else if (resp.psvChecks.size > 0) {
                        _checkedClass.postValue(resp.psvChecks)
                    }
                } else {
                    var errors = ""

                    for (i in response!!.errors!!.indices) {
                        errors = """
                                $errors${response!!.errors!![i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }
            }
            else{
                mErrorsMsg!!.postValue("No data found")
            }
        }
    }

    fun fetchDataFromDBDailyInspection(context: Context) {
        ApiDataRepo.getApiSingleData(context, Constants.DAILY_INSPECTION_LIST).observeForever {
            if (it != null) {
                val response = AFJUtils.convertStringToObject(it.apiGetResponse,GetDailyInspectionList::class.java)
                if (response!!.code == 200) {
                    val resp = response!!.data!!
                    _inspections.postValue(resp.inspections!!)
                    _vehicleInfo.postValue(resp.vehicle!!)
                } else {
                    var errors = ""
                    for (i in response!!.errors!!.indices) {
                        errors = """
                                $errors${response!!.errors!![i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }
            }
            else{
                mErrorsMsg!!.postValue("No data found")
            }
        }
    }

    fun postInspectionVDI(context: Context, body: InspectionCheckData) {
            getInstance(context)
            _dialogShow.postValue(true)
            ApiDataRepo.getApiSingleData(context, Constants.DAILY_INSPECTION_CHECKS)
                .observeForever {
                    it.apiPostData = AFJUtils.convertObjectToJson(body)
                    ApiDataRepo.insertData(context!!, it)
                }
            apiInterface!!.postVehicleDailyInspection(body)
                .enqueue(object : Callback<LocationResponse?> {
                    override fun onResponse(
                        call: Call<LocationResponse?>,
                        response: Response<LocationResponse?>
                    ) {
                        _dialogShow.postValue(false)
                        if (response.body() != null) {
                            if (response.body()!!.code == 200) {
                                _dataUploaded.postValue(true)
                                val liveData = ApiDataRepo.getApiSingleData(
                                    context,
                                    Constants.DAILY_INSPECTION_CHECKS
                                )
                                liveData.observe(context as AppCompatActivity) {
                                    it.apiStatus = 1
                                    it.apiPostResponse = response.body()!!.data!!.message!!
                                    ApiDataRepo.updateApiData(context!!, it)
                                    liveData.removeObservers(context as AppCompatActivity)
                                }
                            } else {
                                _dataUploaded.postValue(false)
                                var errors = ""

                                for (i in response.body()!!.errors!!.indices) {
                                    errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
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
                                    ApiDataRepo.updateApiData(context!!, it)

                                    liveData.removeObservers(context as AppCompatActivity)
                                }

                            }
                        } else {
                            _dataUploaded.postValue(false)
                            mErrorsMsg!!.postValue(response.raw().message)

                            val liveData = ApiDataRepo.getApiSingleData(
                                context,
                                Constants.DAILY_INSPECTION_CHECKS
                            )
                            liveData.observe(context as AppCompatActivity) {
                                it.apiStatus = 0
                                it.apiError = response.raw().message
                                it.apiRetryCount = it.apiRetryCount + 1
                                it.lastTimeApiError = AFJUtils.getCurrentDateTime()
                                ApiDataRepo.updateApiData(context!!, it)
                                liveData.removeObservers(context as AppCompatActivity)
                            }
                        }
                    }

                    override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                        _dialogShow.postValue(false)
                        val exception = t.toString()
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
                            it.apiError = t.toString()
                            it.lastTimeApiError = AFJUtils.getCurrentDateTime()
                            ApiDataRepo.updateApiData(context!!, it)

                            liveData.removeObservers(context as AppCompatActivity)
                        }

                    }
                })

        }



    fun postChecksInspection(context: Context, body: InspectionReviewData) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.postReviewInspectionChecks(body)
            .enqueue(object : Callback<LocationResponse?> {
                override fun onResponse(
                    call: Call<LocationResponse?>,
                    response: Response<LocationResponse?>
                ) {
                    _dialogShow.postValue(false)
                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {
                            _dataUploaded.postValue(true)

                        } else {
                            _dataUploaded.postValue(false)
                            var errors = ""

                            for (i in response.body()!!.errors!!.indices) {
                                errors = """
                                $errors${response.body()!!.errors!![i].message}

                                """.trimIndent()
                            }
                            mErrorsMsg!!.postValue(errors)

                        }
                    } else {
                        mErrorsMsg!!.postValue(response.raw().message)
                        _dataUploaded.postValue(false)

                    }
                }

                override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                    _dialogShow.postValue(false)
                    val exception = t.toString()
                    mErrorsMsg!!.postValue(exception)
                    _dataUploaded.postValue(false)

                }
            })

    }



}