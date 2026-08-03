package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class ResponseDataValue(
    @SerializedName("message")
    val message: String,

    @SerializedName("is_success")
    val isSuccess: Boolean
)
