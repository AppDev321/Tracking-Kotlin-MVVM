package com.example.afjtracking.view.fragment.fuel.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.FuelForm
import com.example.afjtracking.model.responses.GetFuelFormResponse
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import com.example.afjtracking.retrofit.SuccessCallback
import retrofit2.Response

class FuelViewModel : ViewModel() {
    @JvmField
    var EmailAddress = MutableLiveData<String>()

    @JvmField
    var Password = MutableLiveData<String>()


    val _fuelForm = MutableLiveData<List<FuelForm>>()
    val getFuelForm: LiveData<List<FuelForm>> = _fuelForm


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
        private var instance: NotificationViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): NotificationViewModel? {
            if (instance == null) instance = NotificationViewModel()
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


}