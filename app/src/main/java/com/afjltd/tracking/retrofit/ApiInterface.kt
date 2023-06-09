package com.afjltd.tracking.retrofit

import com.afjltd.tracking.model.requests.*
import com.afjltd.tracking.model.responses.*
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiInterface {


    @POST("vehicle-login")
    fun getLoginUser(@Body body: LoginRequest?): Call<LoginResponse?>

    @POST("vehicles/fetch-vehicle-data")
    fun getVehicleData(@Body body: LoginRequest?): Call<LoginResponse?>

    @POST("update-location")
    fun updateLocation(@Body body: LocationApiRequest?): Call<LocationResponse?>

    @GET("vehicles/daily-inspection/checks")
    fun getVehicleDailyInspectionCheckList(): Call<GetDailInspectionCheckListResponse?>

    @POST("vehicles/daily-inspection/checks/save")
    fun postVehicleDailyInspection(@Body body: InspectionCheckData): Call<LocationResponse?>


    @POST("vehicles/daily-inspection/all")
    fun getDailyInspectionList(@Body body: DailyInspectionListRequest): Call<GetDailyInspectionList?>

    @POST("vehicles/daily-inspection/single")
    fun getDailyInspectionReview(@Body body: SingleInspectionRequest): Call<GetDailyInspectionReview?>

    @POST("vehicles/daily-inspection/checks/fix")
    fun postReviewInspectionChecks(@Body body: InspectionReviewData): Call<LocationResponse?>


    @POST("upload")
    fun uploadFileApi(@Body body: RequestBody): Call<UploadFileAPiResponse?>

    @POST("api-error")
    fun postErrorData(@Body body: ErrorRequest): Call<LocationResponse?>

    @POST("vehicles/inspections")
    fun getWeeklyInspectionList(@Body body: WeeklyVehicleInspectionRequest): Call<WeeklyInspectionListResponse?>

    @POST("vehicles/inspection/create")
    fun createWeeklyInspection(@Body body: InspectionCreateRequest): Call<LocationResponse?>

    @POST("vehicles/inspection/check")
    fun getWeeklyInspectionChecks(@Body body: SingleInspectionRequest): Call<GetWeeklyInspectionChecksListResponse?>

    @POST("vehicles/inspection/check/save")
    fun saveWeeklyInspectionCheck(@Body body: SavedWeeklyInspection): Call<LocationResponse?>



    @POST("vehicles/get-form")
    fun getFormData(@Body body:FormRequest): Call<GetFormResponse?>

    @POST("vehicles/save-form")
    fun saveForms(@Body body: SaveFormRequest): Call<LocationResponse?>

    @POST("update-vehicle-fcm-token")
    fun sendFCMTokenToServer(@Body body: FCMRegistrationRequest): Call<LocationResponse?>

    @POST("get-qr-code")
    fun getQRCode(@Body body: FCMRegistrationRequest): Call<LocationResponse?>


    @POST("device-notification/get-notification-count")
    fun getNotificationCount(@Body body: LoginRequest): Call<LocationResponse?>

    @POST("device-notification")
    fun getNotificationData(@Body body: LoginRequest): Call<NotificationDataResponse?>

    @POST("device-notification/change-read-status")
    fun updateNotificationStatus(@Body body: LoginRequest): Call<LocationResponse?>
    @POST("device-notification/delete-notification")
    fun deleteNotification(@Body body: LoginRequest): Call<LocationResponse?>


    @GET("contact-list")
    fun getContactList(): Call<GetContactListResponse?>

    @GET("app-check")
    fun checkApiStatus(): Call<ApiVersionResponse?>
    @POST("vehicles/routes/list")
    fun fetchRouteList(@Body body: LoginRequest): Call<GetRouteListResponse?>
    @POST("vehicles/routes/update-status")
    fun updateRouteStatus(@Body body: LoginRequest): Call<GetRouteListResponse?>
    @POST("calculate-distance")
    fun getDistanceBtwLocation(@Body body: DistanceRequest): Call<DistanceResponse?>



}