package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName

data class ResponseEmailDuplicateCheck(
    @SerializedName("isDuplicated")
    val isDuplicated: Boolean,

    @SerializedName("message")
    val message: String
)
