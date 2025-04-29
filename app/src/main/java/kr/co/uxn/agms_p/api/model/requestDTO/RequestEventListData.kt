package kr.co.uxn.agms_p.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestEventListData(
    @SerializedName("value")
    val value: Double,

    @SerializedName("created_at")
    val createdAt: String
)
