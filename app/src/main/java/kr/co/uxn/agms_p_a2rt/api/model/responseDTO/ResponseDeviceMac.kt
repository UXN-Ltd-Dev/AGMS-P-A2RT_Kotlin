package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseDeviceMac(
    @SerializedName("device_mac")
    val deviceMac: String,

    @SerializedName("device_type")
    val deviceType:Int,

    @SerializedName("is_exists")
    val isExists: Boolean,

    @SerializedName("message")
    val message: String
)
