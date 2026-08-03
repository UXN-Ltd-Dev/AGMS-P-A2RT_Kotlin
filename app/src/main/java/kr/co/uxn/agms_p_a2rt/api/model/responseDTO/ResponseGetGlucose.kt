package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseGetGlucose(
    @SerializedName("glucose")
    val glucose: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("w1_current")
    val weo1: Double,

    @SerializedName("w2_current")
    val weo2: Double
)
