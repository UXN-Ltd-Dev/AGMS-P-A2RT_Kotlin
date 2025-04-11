package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class ResponseDataValue(
    @SerializedName("message")
    val message: String,

    @SerializedName("is_success")
    val isSuccess: Boolean
)
