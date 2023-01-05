package com.example.afjtracking.model.responses

import android.os.Parcelable
import com.example.afjtracking.utils.AFJUtils
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class GetDailInspectionCheckListResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: InspectionCheckData? = InspectionCheckData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf(),


    )

class Error {
    @SerializedName("message")
    @Expose
    var message: String? = null
}

@Parcelize
data class InspectionCheckData(

    @SerializedName("pts_checks") var ptsChecks: ArrayList<PTSCheck> = arrayListOf(),
    @SerializedName("psv_checks") var psvChecks: ArrayList<PSVCheck> = arrayListOf(),
    @SerializedName("vehicle") var vehicle: Vehicle? = Vehicle(),
    @SerializedName("total_count_checks") var totalCountChecks: Int? = 0,
    @SerializedName("total_count_form") var totalCountForm: Int? = 0,
    @SerializedName("inspection") var inspection: Inspection? = Inspection(),
    @SerializedName("psv_form") var psvForm: ArrayList<Form> = arrayListOf(),
    @SerializedName("pts_form") var ptsForm: ArrayList<Form> = arrayListOf(),
    @SerializedName("isCompleted") var isCompleted: Boolean? = false,
    @SerializedName("upload_id") var uploadID: String? = "abc",
    @SerializedName("request_name") var requestName: String? = "PTS",
    @SerializedName("vehicle_type") var vehicleType: String? = "",
   // @SerializedName("sensor_data") var sensorData: SensorData? = SensorData(),
    @SerializedName("sensor_data") var sensorData: List<SensorOrientationData>? = arrayListOf(),
    @SerializedName("time_spent") var inspectionTimeSpent:String? = null



) : Parcelable



@Parcelize
data class SensorOrientationData(
    @SerializedName("orientation")
    val orientation: FloatArray,
    @SerializedName("degrees")
    val degrees: Double,
    @SerializedName("direction")
    val direction: String,
    @SerializedName("angle")
    val angle: Long,
    @SerializedName("timeStamp")
    val timeStamp: Long



): Parcelable


@Parcelize
data class SensorData(
    @SerializedName("accelerometer")
    var acceleroMeterReding: List<FloatArray>? = arrayListOf(),

    @SerializedName("gyrometer")
    var gyroSensorReding: List<FloatArray>? = arrayListOf(),


    @SerializedName("linear")
    var linearSensorReading: List<FloatArray>? = arrayListOf(),

    @SerializedName("magnetometer")
    var magnetoSensorReading: List<FloatArray>? = arrayListOf(),
    @SerializedName("rotation")
    var rotationSensorReading: List<FloatArray>? = arrayListOf(),

    @SerializedName("orientation")
    var orientationSensorReading: List<FloatArray>? = arrayListOf(),

) : Parcelable


@Parcelize
data class Inspection(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("employee_id") var employeeId: Int? = null,
    @SerializedName("vehicle_id") var vehicleId: Int? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("vehicle_type") var vehicleType: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("is_read") var isRead: Int? = null,
    @SerializedName("source") var source: String? = null,

    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null

) : Parcelable {
    fun getReadableDate(): String {
        if (date.toString().contains("T"))
            return AFJUtils.convertServerDateTime(date.toString(), false)
        else
            return date.toString()
    }
}

@Parcelize
data class Vehicle(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("employee_id") var employeeId: Int? = null,
    @SerializedName("vrn") var vrn: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("make") var make: String? = null,
    @SerializedName("model") var model: String? = null,
    @SerializedName("odometer_reading") var odometerReading: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("detail") var detail: Detail? = Detail(),


    ) : Parcelable


@Parcelize
data class Detail(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("vehicle_id") var vehicleId: Int? = null,
    @SerializedName("purchase_condition") var purchaseCondition: String? = null,
    @SerializedName("purchase_date") var purchaseDate: String? = null,
    @SerializedName("spare_key") var spareKey: Int? = null,
    @SerializedName("road_status") var roadStatus: String? = null,
    @SerializedName("vehicle_type") var vehicleType: String? = null,
    @SerializedName("isTrackerInstalled") var isTrackerInstalled: Int? = null,
    @SerializedName("last_location_lat") var lastLocationLat: String? = null,
    @SerializedName("last_location_long") var lastLocationLong: String? = null,
    @SerializedName("last_location_time") var lastLocationTime: String? = null,

    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null

) : Parcelable

@Parcelize
data class PTSCheck(

    @SerializedName("class") var classCheck: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("count") var count: Int? = null,
    @SerializedName("checks") var checks: ArrayList<Checks> = arrayListOf()

) : Parcelable


@Parcelize
data class Checks(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("check_no") var checkNo: Int? = null,
    @SerializedName("class") var classCheck: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("issue_check") var issueCheck: Boolean? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("inspection_data") var savedInspection: SolvedInspection? = SolvedInspection()

) : Parcelable {


    fun getSolvedInspection(): SolvedInspection {
        if (savedInspection == null) {
            savedInspection = SolvedInspection()
        }
        return savedInspection!!
    }

}

@Parcelize
data class SolvedInspection(

    @SerializedName("issue_check") var issueCheck: Boolean? = false,
    @SerializedName("checked") var checked: Boolean? = false,
    @SerializedName("worn_refit") var wornRefit: String? = null,
    @SerializedName("quantity_required") var quantityRequired: String? = null,
    @SerializedName("quantity_on_vehicle") var quantityOnVehicle: String? = null,
    @SerializedName("quantity") var quantity: String? = null,
    @SerializedName("fleet_no") var fleetNo: String? = null

) : Parcelable

@Parcelize
data class Form(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("input_no") var inputNo: Int? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("field_name") var fieldName: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("accept") var accept: String? = null,
    @SerializedName("required") var required: Boolean? = null,
    @SerializedName("comment") var comment: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("value") var value: String? = null,
    @SerializedName("options") var options: ArrayList<Options> = arrayListOf(),


    ) : Parcelable


@Parcelize
data class Options(

    @SerializedName("option") var option: Int? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("field_value") var fieldName: String? = null

) : Parcelable


@Parcelize
data class PSVCheck(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("check_no") var checkNo: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    //   @SerializedName("saved_inspection") var savedInspection: Boolean? = null,
    @SerializedName("inspection_data") var savedInspection: SolvedInspection? = SolvedInspection()

) : Parcelable {


    fun getSolvedInspection(): SolvedInspection {
        if (savedInspection == null) {
            savedInspection = SolvedInspection()
        }
        return savedInspection!!
    }

}