package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestEventListData(
    @SerializedName("value")
    val value: Double,

    @SerializedName("created_at")
    val createdAt: String
)
