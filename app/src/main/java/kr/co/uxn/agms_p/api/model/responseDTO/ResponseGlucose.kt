package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseGlucose(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String

)
