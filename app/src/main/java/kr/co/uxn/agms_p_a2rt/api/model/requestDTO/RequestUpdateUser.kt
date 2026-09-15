package kr.co.uxn.agms_p_a2rt.api.model.requestDTO

import com.google.gson.annotations.SerializedName

/**
 * POST /api/user/update 의 본문.
 *
 * 이 엔드포인트만 필드 이름이 낙타 표기다. 다른 요청들은 user_id 처럼 밑줄 표기를
 * 쓰는데, 명세가 userId / diabetesType / targetGlucoseLow 로 되어 있어 그대로 맞췄다.
 * 서로 달라 보이더라도 한쪽으로 통일하면 안 된다.
 */
data class RequestUpdateUser(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    /** 1601 남성, 1602 여성, 1603 선택 안 함. 회원가입 때 보내는 값과 같다. */
    @SerializedName("sex")
    val sex: Int,

    @SerializedName("age")
    val age: Int,

    @SerializedName("height")
    val height: Int,

    @SerializedName("weight")
    val weight: Int,

    /** 1701 제1형 … 1707 모름. 회원가입 때 보내는 값과 같다. */
    @SerializedName("diabetesType")
    val diabetesType: Int,

    @SerializedName("targetGlucoseLow")
    val targetGlucoseMin: Int,

    @SerializedName("targetGlucoseHigh")
    val targetGlucoseMax: Int
)
