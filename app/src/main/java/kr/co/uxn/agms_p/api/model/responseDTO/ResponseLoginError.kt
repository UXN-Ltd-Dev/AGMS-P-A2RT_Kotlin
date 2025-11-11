package kr.co.uxn.agms_p.api.model.responseDTO

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class ResponseLoginError(
    @SerializedName("result_code")
    val resultCode: Int,

    @SerializedName("fail_message")
    val failMessage: String
)
