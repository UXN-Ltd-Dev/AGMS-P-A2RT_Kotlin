package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseLinkDevice(
    @SerializedName("device_id")
    val deviceId: Int,

    @SerializedName("is_success")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("start_date")
    val startDate: String
)
