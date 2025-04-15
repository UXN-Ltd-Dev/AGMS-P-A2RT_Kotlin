package kr.co.uxn.agms_p.api

import kr.co.uxn.agms_p.api.model.requestDTO.RequestDataValue
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEmailCode
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEventData
import kr.co.uxn.agms_p.api.model.requestDTO.RequestLinkDevice
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignInNormal
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignUpNormal
import kr.co.uxn.agms_p.api.model.requestDTO.RequestOAuthSignUpAndLogin
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignUpOauthDetail
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseDataValue
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseDeviceMac
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseEmailCode
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseEventData
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseGetEvent
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseGetLastTime
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseLinkDevice
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseSignInNormal
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseSignUpNormal
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseSignUpOauthAndLogin
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseSignUpOauthDetail
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseVerificationCode
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RemoteDataSource {
    // API 연결 테스트
    @GET("/api/test/hello")
    suspend fun testRetrofit(): Response<String>

    // UXN 회원 가입
    @POST("/api/user/join")
    suspend fun uxnSignUp(@Body signUpInfo: RequestSignUpNormal): Response<ResponseSignUpNormal>

    // UXN 로그인
    @POST("/api/login")
    suspend fun uxnLogin(@Body signInInfo: RequestSignInNormal): Response<ResponseSignInNormal>

    // Oauth 로그인 및 회원 가입
    @POST("/api/oauth/mobile")
    suspend fun oAuthSignUpAndLogin(@Body oAuthSignUpAndLogin: RequestOAuthSignUpAndLogin): Response<ResponseSignUpOauthAndLogin>

    // Oauth 세부 정보 등록
    @POST("/api/user/detail")
    suspend fun oAuthSaveDetailInfo(
        @Body oAuthDetailInfo: RequestSignUpOauthDetail
    ): Response<ResponseSignUpOauthDetail>

    // 혈당, 식사, 운동, 인슐린 전송
    @POST("/api/event/add")
    suspend fun uploadEvent(@Body eventData: RequestEventData): Response<ResponseEventData>

    // 기기정보로 MAC 가져오기
    @GET("/api/device/check")
    suspend fun getDeviceMac(@Query("serial_number") serialNumber: String): Response<ResponseDeviceMac>

    // 유저와 기기를 연동
    @POST("/api/device/link")
    suspend fun linkDevice(@Body linkDevice: RequestLinkDevice) : Response<ResponseLinkDevice>

    // 마지막시간 가져오기
    @GET("/api/value/recent/time")
    suspend fun getLastTime(@Query("user_id") userId: Int): Response<ResponseGetLastTime>

    // 데이터 전송
    @POST("/api/value/save")
    suspend fun sendData(@Body data: List<RequestDataValue>): Response<ResponseDataValue>

    // 이메일 인증하기
    @GET("/api/email/simple")
    suspend fun requestVerficationCode(@Query("email") email: String): Response<ResponseVerificationCode>

    // 이메일 인증번호 확인
    @POST("/api/email/code/check")
    suspend fun checkVerificationCode(@Body emailCode: RequestEmailCode): Response<ResponseEmailCode>

    // 이벤트 가져오기
    @GET("/api/event/list")
    suspend fun getEventList(@Query("user_id") userId: Int): Response<List<ResponseGetEvent>>
}

