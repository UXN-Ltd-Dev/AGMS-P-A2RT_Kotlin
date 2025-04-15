package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseGetEvent(
    @SerializedName("event_type_code")
    val eventTypeCode: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("is_success")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String
)
