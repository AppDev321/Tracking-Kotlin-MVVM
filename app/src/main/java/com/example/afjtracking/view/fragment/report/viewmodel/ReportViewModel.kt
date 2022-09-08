package com.example.afjtracking.view.fragment.fuel.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.*
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.retrofit.SuccessCallback
import com.example.afjtracking.room.model.TableAPIData
import com.example.afjtracking.room.repository.ApiDataRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import retrofit2.Response

class ReportViewModel : ViewModel() {


    val _vehicle = MutableLiveData<Vehicle>()
    val getVehicle: LiveData<Vehicle> = _vehicle


    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow


    private val _dataUploaded = MutableLiveData<Boolean>()
    val apiUploadStatus: LiveData<Boolean> = _dataUploaded


    val _reportForm = MutableLiveData<List<InspectionForm>>()
    val getReportForm: LiveData<List<InspectionForm>> = _reportForm


    val _reportData = MutableLiveData<ReportData>()
    val getReportData: LiveData<ReportData> = _reportData


    private var mErrorsMsg: MutableLiveData<String>? = MutableLiveData()
    val errorsMsg: MutableLiveData<String>
        get() {
            if (mErrorsMsg == null) {
                mErrorsMsg = MutableLiveData()
            }
            return mErrorsMsg!!
        }


    companion object {
        private var instance: ReportViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): ReportViewModel? {
            if (instance == null) instance = ReportViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun getReportFormRequest(context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getReportForm()
            .enqueue(object : SuccessCallback<GetReportFormResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(

                    response: Response<GetReportFormResponse?>
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

                    _reportData.postValue(response.body()!!.data!!)
                    _vehicle.postValue(response.body()!!.data!!.vehicle!!)

                    _reportForm.postValue(response.body()!!.data!!.reportForm)


                }

                override fun onFailure(response: Response<GetReportFormResponse?>) {
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


                override fun onAPIError(t: String) {

                    _dataUploaded.postValue(false)
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                    // mErrorsMsg!!.postValue(exception)
                    if (exception.lowercase().contains(Constants.FAILED_API_TAG)) {
                        fetchReportFromDBLastStore(context!!)
                    } else {
                        mErrorsMsg!!.postValue(exception)
                    }

                }
            })

    }

    fun fetchReportFromDBLastStore(context: Context) {

        ApiDataRepo.getApiSingleData(context, Constants.REPORT_FORM).observeForever {
            if (it != null) {
                val response = AFJUtils.convertStringToObject(
                    it.apiGetResponse,
                    GetReportFormResponse::class.java
                )

                if (response.code == 200) {
                    val resp = response.data!!

                    _vehicle.postValue(resp.vehicle!!)
                    _reportForm.postValue(resp.reportForm)
                    _reportData.postValue(resp)

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
                mErrorsMsg!!.postValue("No data found")
            }
        }
    }


    fun saveReportForm(form: SaveFormRequest, context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.saveReportForm(form)
            .enqueue(object : SuccessCallback<LocationResponse?>() {

                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<LocationResponse?>
                ) {
                    _dataUploaded.postValue(true)
                    super.onSuccess(response)
                }
                override fun onFailure(response: Response<LocationResponse?>) {
                    super.onFailure(response)
                    _dataUploaded.postValue(false)
                    var errors = ""
                    for (i in response.body()!!.errors!!.indices) {
                        errors = """
                                $errors${response.body()!!.errors!![i].message}
                                
                                """.trimIndent()
                    }
                    mErrorsMsg!!.postValue(errors)
                }

                override fun onAPIError(error: String) {
                    val exception =error
                    _dataUploaded.postValue(false)
                    mErrorsMsg!!.postValue(exception)
                    _dialogShow.postValue(false)
                    super.onAPIError(error)

                }
            })

    }


}