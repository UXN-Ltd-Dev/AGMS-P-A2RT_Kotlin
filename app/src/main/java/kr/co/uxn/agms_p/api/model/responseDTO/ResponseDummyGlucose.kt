package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class ResponseDummyGlucose(
    @SerializedName("w1_current")
    val weo1: Double,

    @SerializedName("w2_current")
    val weo2: Double,

    @SerializedName("glucose")
    val glucose: Int,

    @SerializedName("created_at")
    val createdAt: String
)
