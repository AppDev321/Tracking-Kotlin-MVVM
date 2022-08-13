package com.example.afjtracking.model.responses

import com.google.gson.annotations.SerializedName


data class WeeklyInspectionListResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: DataInspectionVehicle? = DataInspectionVehicle(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf()

)

data class DataInspectionVehicle(

    @SerializedName("vehicle") var vehicle: VehicleData? = null

)

data class WeeklyInspectionData(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("employee_id") var employeeId: Int? = null,
    @SerializedName("vehicle_id") var vehicleId: Int? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("odometer_reading") var odometerReading: String? = null,
    @SerializedName("vehicle_type") var vehicleType: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("is_read") var isRead: Int? = null,
    @SerializedName("source") var source: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null

)

data class VehicleData(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("employee_id") var employeeId: Int? = null,
    @SerializedName("vrn") var vrn: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("make") var make: String? = null,
    @SerializedName("model") var model: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("vehicle_type") var vehicleType: String?= null,
    @SerializedName("inspections") var inspections: List<WeeklyInspectionData> = arrayListOf(),
    @SerializedName("detail") var detail: Detail? = Detail()

)