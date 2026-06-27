package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestA2RTData(
    @SerializedName("user_device_id")
    val userDeviceId: Int,

    @SerializedName("frequency_type")
    val frequencyType: String,

    @SerializedName("a2rt_time")
    val createdAt: String,

    @SerializedName("a2rt_temperature")
    val temperature: Double,

    @SerializedName("real")
    val real: Long,

    @SerializedName("imaginary")
    val imaginary: Long,

    @SerializedName("magnitude")
    val magnitude: Long,

    @SerializedName("phase")
    val phase: Long
)
