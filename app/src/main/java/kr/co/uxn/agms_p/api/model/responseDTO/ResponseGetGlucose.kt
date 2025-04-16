package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseGetGlucose(
    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("current")
    val current: Double,

    @SerializedName("glucose")
    val glucose: Int
)
