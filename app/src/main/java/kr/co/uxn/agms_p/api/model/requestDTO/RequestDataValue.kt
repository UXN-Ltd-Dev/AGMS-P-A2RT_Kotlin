package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestDataValue(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("we_current")
    val weCurrent: Double,

    @SerializedName("ae_current")
    val aeCurrent: Double
)
