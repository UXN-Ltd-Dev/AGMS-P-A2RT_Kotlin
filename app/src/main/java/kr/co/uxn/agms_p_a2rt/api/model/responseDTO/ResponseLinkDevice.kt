package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseLinkDevice(
    @SerializedName("user_device_id")
    val userDeviceId: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("is_success")
    val isSuccess: Boolean,

    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("serial_number")
    val serialNumber: String,

    @SerializedName("sensor_id")
    val sensorId: Int
)
