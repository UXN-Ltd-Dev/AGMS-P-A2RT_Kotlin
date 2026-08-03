package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

data class RequestEventData(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("event_type_code")
    val eventTypeCode: Int,

    @SerializedName("content")
    val content: String
)
