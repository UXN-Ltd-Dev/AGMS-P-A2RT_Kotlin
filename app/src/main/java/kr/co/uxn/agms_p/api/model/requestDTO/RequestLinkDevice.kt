package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestLinkDevice(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("serial_number")
    val serialNumber: String
)
