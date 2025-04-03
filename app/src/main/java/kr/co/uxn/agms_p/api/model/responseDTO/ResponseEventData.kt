package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseEventData(
    @SerializedName("message")
    val message: String,

    @SerializedName("is_success")
    val isSuccess: Boolean
)
