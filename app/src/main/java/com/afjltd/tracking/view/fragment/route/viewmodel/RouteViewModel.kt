package com.afjltd.tracking.view.fragment.route.viewmodel

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import com.afjltd.tracking.broadcast.TrackingAppBroadcast
import com.afjltd.tracking.model.requests.DistanceRequest
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.DistanceResponse
import com.afjltd.tracking.model.responses.GetRouteListResponse
import com.afjltd.tracking.model.responses.Sheets
import com.afjltd.tracking.retrofit.ApiInterface
import com.afjltd.tracking.retrofit.RetrofitUtil
import com.afjltd.tracking.retrofit.SuccessCallback
import com.afjltd.tracking.service.location.ForegroundLocationService
import com.afjltd.tracking.service.location.LocationRepository
import com.afjltd.tracking.utils.AFJUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.StrictMath.cos
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt


class RouteViewModel : ViewModel() {

    private val _dialogShow = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _dialogShow


    val _routeList = MutableSharedFlow<List<Sheets>>()

    val getRouteList = _routeList.asSharedFlow()


    private var mErrorsMsg = MutableSharedFlow<String>()
    val errorsMsg =mErrorsMsg.asSharedFlow()


    companion object {
        private var instance: RouteViewModel? = null
        private var apiInterface: ApiInterface? = null
        fun getInstance(context: Context?): RouteViewModel? {
            if (instance == null) instance = RouteViewModel()
            if (apiInterface == null) apiInterface =
                RetrofitUtil.getRetrofitHeaderInstance(context).create(
                    ApiInterface::class.java
                )
            return instance
        }
    }


    fun getRouteList(context: Context?) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.fetchRouteList(LoginRequest(deviceDetail =AFJUtils.getDeviceDetail()))
            .enqueue(object : SuccessCallback<GetRouteListResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<GetRouteListResponse?>
                ) {
                    super.onSuccess(response)

                    val res = response.body()?.data?.sheets ?: arrayListOf()
                    if (res.size > 0) {
                        viewModelScope.launch {
                            _routeList.emit(res)
                        }
                    } else {
                        viewModelScope.launch {
                            mErrorsMsg.emit("No Route Found")
                        }

                    }
                }

                override fun onFailure(response: Response<GetRouteListResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }

                    viewModelScope.launch {
                        mErrorsMsg.emit(errors)
                    }
                }

                override fun onAPIError(t: String) {
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                    viewModelScope.launch {
                        mErrorsMsg.emit(exception)
                    }



                }
            })

    }


    fun updateRouteListStatus(context: Context?,request: LoginRequest,responseCallback:(Any)->Unit) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.updateRouteStatus(request)
            .enqueue(object : SuccessCallback<GetRouteListResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<GetRouteListResponse?>
                ) {
                    super.onSuccess(response)
                    var res = response.body()
                    responseCallback("Success")

                }
                override fun onFailure(response: Response<GetRouteListResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    responseCallback(errors)
                }
                override fun onAPIError(t: String) {
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                    responseCallback(exception)

                }
            })

    }


    fun getDistance(context: Context?,request: DistanceRequest,responseCallback:(Any)->Unit) {
        getInstance(context)
        _dialogShow.postValue(true)
        apiInterface!!.getDistanceBtwLocation(request)
            .enqueue(object : SuccessCallback<DistanceResponse?>() {
                override fun loadingDialog(show: Boolean) {
                    super.loadingDialog(show)
                    _dialogShow.postValue(show)
                }

                override fun onSuccess(
                    response: Response<DistanceResponse?>
                ) {
                    super.onSuccess(response)
                    var res = response.body()
                    responseCallback(res?.data!!)

                }
                override fun onFailure(response: Response<DistanceResponse?>) {
                    super.onFailure(response)
                    var errors = ""
                    for (i in response.body()!!.errors.indices) {
                        errors = """
                                $errors${response.body()!!.errors[i].message}
                                
                                """.trimIndent()
                    }
                    responseCallback(errors)
                }
                override fun onAPIError(t: String) {
                    val exception = t.toString()
                    _dialogShow.postValue(false)
                    responseCallback(exception)

                }
            })

    }


    fun distanceBetween(location1: Location, location2: Sheets): Double {
        val R = 6371 // Earth's radius in kilometers
        val lat1 = location1.latitude.toRadians()
        val lat2 = location2.latitude!!.toRadians()
        val dLat = (lat2 - lat1).toRadians()
        val dLng = (location2.longitude!! - location1.longitude).toRadians()

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    fun Double.toRadians(): Double {
        return this * PI / 180
    }

    fun rearrangeByDistance(currentLatLng: Location, routeSheet: List<Sheets>): List<Sheets> {
        return routeSheet.sortedBy { distanceBetween(currentLatLng, it) }
    }
    fun openDirectionUsingGoogleMap(currentLocation: Location, routeSheet:List<Sheets>?=null,destination:Location?=null,context:Context)
    {

        val current = LatLng(currentLocation.latitude,currentLocation.longitude).let {
            "${it.latitude},${it.longitude}"

        }
        val stops =
            routeSheet?.joinToString("/") { "${it.latitude},${it.longitude}" }
                ?: destination.let{
                    "${it?.latitude},${it?.longitude}"
                }

        val uri = Uri.parse("https://www.google.com/maps/dir/$current/$stops")

        AFJUtils.writeLogs(uri.toString())
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)


    }

}