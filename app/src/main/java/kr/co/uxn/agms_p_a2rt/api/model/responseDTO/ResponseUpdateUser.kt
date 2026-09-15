package kr.co.uxn.agms_p_a2rt.api.model.responseDTO

import com.google.gson.annotations.SerializedName

/**
 * POST /api/user/update 의 응답.
 *
 * 명세는 isSuccess 로 주는데 이 서버의 다른 응답들은 is_success 를 쓴다.
 * 어느 쪽으로 와도 읽히도록 alternate 를 달아 둔다.
 */
data class ResponseUpdateUser(
    @SerializedName(value = "isSuccess", alternate = ["is_success"])
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String
)
