package com.afjltd.tracking.model.responses

data class TrackingSettingFirebase(val vehicleID: String? = "", val tracking: Boolean? = false)
data class QRFireDatabase(
    val deviceId: String? = "", val status: Boolean? = false, val expiresAt: String? = "",
    val error_message: String? = "", val has_error: Boolean? = false,
    val data: QRFirebaseData? = QRFirebaseData()
)

data class QRFirebaseData(
    val token: String? = null,
    val user: QRFirebaseUser? = QRFirebaseUser()
)

data class QRFirebaseUser(

    val id: Int? = null,
    val firstname: String? = null,
    val middlename: String? = null,
    val lastname: String? = null,
    val contact_no: String? = null,
    val official_email: String? = null,
    val personal_email: String? = null,
    val date_of_birth: String? = null,
    val gender: String? = null,
    val emergency_contact_relationship: String? = null,
    val emergency_contact: String? = null,
    val emergency_contact_address: String? = null,
    val current_address: String? = null,
    val permanent_address: String? = null,
    val city: String? = null,
    val designation: String? = null,
    val picture: String? = null,
    val full_name: String? = null
)