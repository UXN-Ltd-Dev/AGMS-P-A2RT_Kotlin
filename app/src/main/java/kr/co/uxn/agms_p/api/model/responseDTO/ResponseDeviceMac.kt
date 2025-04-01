package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseDeviceMac(
    @SerializedName("device_mac")
    val deviceMac: String
)
