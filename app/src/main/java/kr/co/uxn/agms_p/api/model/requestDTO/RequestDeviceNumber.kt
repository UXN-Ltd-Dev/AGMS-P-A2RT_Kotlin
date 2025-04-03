package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestDeviceNumber(
    @SerializedName("serial_number")
    val serialNumber: String
)
