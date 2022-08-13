package com.example.afjtracking.model.responses

import com.google.gson.annotations.SerializedName


data class LoginResponse (

    @SerializedName("code"    ) var code    : Int?              = null,
    @SerializedName("message" ) var message : String?           = null,
    @SerializedName("data"    ) var data    : Data?             = Data(),
    @SerializedName("errors"  ) var errors  : ArrayList<Error> = arrayListOf()

)



data class VehicleDetail (

    @SerializedName("id"          ) var id         : Int?    = null,
    @SerializedName("employee_id" ) var employeeId : Int?    = null,
    @SerializedName("vrn"         ) var vrn        : String? = null,
    @SerializedName("type"        ) var type       : String? = null,
    @SerializedName("make"        ) var make       : String? = null,
    @SerializedName("model"       ) var model      : String? = null,
    @SerializedName("created_at"  ) var createdAt  : String? = null,
    @SerializedName("updated_at"  ) var updatedAt  : String? = null,
    @SerializedName("odometer_reading") var odometerReading: String? = null,
    @SerializedName("detail") var detail: Detail? = Detail()
)
data class User (

    @SerializedName("id"                             ) var id                           : Int?    = null,
    @SerializedName("firstname"                      ) var firstname                    : String? = null,
    @SerializedName("middlename"                     ) var middlename                   : String? = null,
    @SerializedName("lastname"                       ) var lastname                     : String? = null,
    @SerializedName("sage_id"                        ) var sageId                       : String? = null,
    @SerializedName("national_insurance_number"      ) var nationalInsuranceNumber      : String? = null,
    @SerializedName("contact_no"                     ) var contactNo                    : String? = null,
    @SerializedName("official_email"                 ) var officialEmail                : String? = null,
    @SerializedName("personal_email"                 ) var personalEmail                : String? = null,
    @SerializedName("identity_no"                    ) var identityNo                   : String? = null,
    @SerializedName("date_of_birth"                  ) var dateOfBirth                  : String? = null,
    @SerializedName("gender"                         ) var gender                       : String? = null,
    @SerializedName("emergency_contact_relationship" ) var emergencyContactRelationship : String? = null,
    @SerializedName("emergency_contact"              ) var emergencyContact             : String? = null,
    @SerializedName("emergency_contact_address"      ) var emergencyContactAddress      : String? = null,
    @SerializedName("current_address"                ) var currentAddress               : String? = null,
    @SerializedName("permanent_address"              ) var permanentAddress             : String? = null,
    @SerializedName("city"                           ) var city                         : String? = null,
    @SerializedName("designation"                    ) var designation                  : String? = null,
    @SerializedName("type"                           ) var type                         : String? = null,
    @SerializedName("status"                         ) var status                       : Int?    = null,
    @SerializedName("employment_status"              ) var employmentStatus             : String? = null,
    @SerializedName("employment_type"                ) var employmentType               : String? = null,
    @SerializedName("picture"                        ) var picture                      : String? = null,
    @SerializedName("joining_date"                   ) var joiningDate                  : String? = null,
    @SerializedName("exit_date"                      ) var exitDate                     : String? = null,
    @SerializedName("gross_salary"                   ) var grossSalary                  : Int?    = null,
    @SerializedName("bonus"                          ) var bonus                        : Int?    = null,
    @SerializedName("branch_id"                      ) var branchId                     : Int?    = null,
    @SerializedName("department_id"                  ) var departmentId                 : String? = null,
    @SerializedName("device_type"                    ) var deviceType                   : String? = null,
    @SerializedName("deleted_at"                     ) var deletedAt                    : String? = null,
    @SerializedName("created_at"                     ) var createdAt                    : String? = null,
    @SerializedName("updated_at"                     ) var updatedAt                    : String? = null,
    @SerializedName("full_name"                      ) var fullName                     : String? = null

)
data class Data (

    @SerializedName("token"   ) var token   : String?  = null,
    @SerializedName("vehicle" ) var vehicle : VehicleDetail? = VehicleDetail(),
    @SerializedName("user" ) var user : User? = User()


)