package com.example.afjtracking.view.fragment.fuel.viewmodel

import android.content.Context
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.afjtracking.model.requests.LoginRequest
import com.example.afjtracking.model.requests.SaveFuelFormRequest
import com.example.afjtracking.model.responses.FuelForm
import com.example.afjtracking.model.responses.GetFuelFormResponse
import com.example.afjtracking.model.responses.LocationResponse
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.retrofit.ApiInterface
import com.example.afjtracking.retrofit.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
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
            Password.value.toString(),""
        )

        userMutableLiveData!!.postValue(loginUser)
    }

    fun getFuelFormRequest(context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getFuelForm()
            .enqueue(object : Callback<GetFuelFormResponse?> {
                override fun onResponse(
                    call: Call<GetFuelFormResponse?>,
                    response: Response<GetFuelFormResponse?>
                ) {
                    _dialogShow.postValue(false)
                    if (response.body() != null) {
                        if (response.body()!!.code == 200) {
                            _vehicle.postValue(response.body()!!.data!!.vehicle!!)
                            _fuelForm.postValue(response.body()!!.data!!.fuelForm)


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

                override fun onFailure(call: Call<GetFuelFormResponse?>, t: Throwable) {
                    val exception = t.toString()
                     mErrorsMsg!!.postValue(exception)

                    _dataUploaded.postValue(false)


                }
            })

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