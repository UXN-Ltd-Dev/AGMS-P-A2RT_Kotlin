package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseA2RTData(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("error")
    val error: String
)
