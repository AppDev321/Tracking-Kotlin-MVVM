package com.example.afjtracking.view.fragment.fuel.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.SaveFuelFormRequest
import com.example.afjtracking.model.responses.GetReportFormResponse
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.model.responses.ReportForm
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.room.model.TableAPIData
import com.example.afjtracking.room.repository.ApiDataRepo
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportViewModel : ViewModel() {


    val _vehicle = MutableLiveData<Vehicle>()
    val getVehicle: LiveData<Vehicle> = _vehicle



    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow


    private val _dataUploaded = MutableLiveData<Boolean>()
    val apiUploadStatus: LiveData<Boolean> = _dataUploaded


    val _reportForm = MutableLiveData<List<ReportForm> >()
    val getReportForm: LiveData<List<ReportForm> > = _reportForm



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
            .enqueue(object : Callback<GetReportFormResponse?> {
                override fun onResponse(
                    call: Call<GetReportFormResponse?>,
                    response: Response<GetReportFormResponse?>
                ) {
                    _dialogShow.postValue(false)
                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {

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


                            _vehicle.postValue(response.body()!!.data!!.vehicle!!)

                            _reportForm.postValue(response.body()!!.data!!.reportForm)

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
                        _dataUploaded.postValue(false)
                        mErrorsMsg!!.postValue(response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<GetReportFormResponse?>, t: Throwable) {

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
                val response = AFJUtils.convertStringToObject(it.apiGetResponse,
                    GetReportFormResponse::class.java)

                if (response!!.code == 200) {
                    val resp = response!!.data!!

                    _vehicle.postValue(resp.vehicle!!)
                    _reportForm.postValue(resp.reportForm)


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




    fun saveFuelForm(form: SaveFuelFormRequest, context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.saveFuelForm(form)
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
                        _dataUploaded.postValue(false)
                        mErrorsMsg!!.postValue(response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<LocationResponse?>, t: Throwable) {
                    val exception = t.toString()
                    _dataUploaded.postValue(false)
                    mErrorsMsg!!.postValue(exception)
                    _dialogShow.postValue(false)


                }
            })

    }





}