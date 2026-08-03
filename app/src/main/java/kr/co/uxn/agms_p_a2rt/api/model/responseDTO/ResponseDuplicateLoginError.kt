package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class ResponseDuplicateLoginError(
    @SerializedName("result_code")
    val resultCode: Int,

    @SerializedName("action_required")
    val action: String,

    @SerializedName("message")
    val message: String
)
