package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseA2RTLastTime(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName(value = "experiment_date", alternate = ["target_time", "targetTime"])
    val targetTime: Long? = null
)
