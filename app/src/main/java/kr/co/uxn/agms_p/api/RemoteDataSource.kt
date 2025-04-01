package kr.co.uxn.agms_p.api


import kr.co.uxn.agms_p.api.model.requestDTO.RequestDeviceNumber
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEmailVerificationCode
import kr.co.uxn.agms_p.api.model.requestDTO.RequestGlucose
import kr.co.uxn.agms_p.api.model.requestDTO.RequestGoogleIdToken
import kr.co.uxn.agms_p.api.model.requestDTO.RequestKakaoAccessCode
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignInNormal
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignUpNormal
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseDeviceMac
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseEmailVerificationCode
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseGlucose
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseGoogleIdToken
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseKakaoAccessCode
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseSignInNormal
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseSignUpNormal
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseUserCheck
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RemoteDataSource {
//    @POST("/data_collect/insert_device")
//    fun registerDeviceToServer(@Body devices: RegisterDevice): Call<ResponseData>

    //     API 연결 테스트
//    @GET("/api/test/hello")
//    suspend fun testRetrofit() : Response<String>

    // UXN 회원가입
    @POST("/api/user/join")
    suspend fun sendUxnSignUp(@Body signUpInfo: RequestSignUpNormal) : Response<ResponseSignUpNormal>

    // 카카오 회원가입
    @POST("/api/oauth/mobile/kakao")
    suspend fun sendKakaoSignUp(@Body signUpInfo: RequestSignUpNormal) : Response<ResponseSignUpNormal>

    // 구글 회원가입
    @POST("/api/oauth/mobile/google")
    suspend fun sendGoogleSignUp(@Body signUpInfo: RequestSignUpNormal) : Response<ResponseSignUpNormal>


//    이메일 인증하기 1 : 인증번호 전송
//    @POST("api/test")
//    suspend fun sendVerificationCode(@Body email: RequestEmailVerificationCode) : Response<ResponseEmailVerificationCode>

    // 이메일 인증하기 2 : 인증번호 전송
    @POST("api/test")
    suspend fun sendVerificationCode(@Body email: RequestEmailVerificationCode) : Response<ResponseEmailVerificationCode>

    // 카카오 로그인 : 인가코드 전송
    @POST("/api/oauth/mobile/kakao")
    suspend fun sendKakaoAccessCode(@Body accessCode: RequestKakaoAccessCode) : Response<ResponseKakaoAccessCode>

    // 구글 로그인 : 토큰 전송
    @POST("/api/oauth/mobile/google")
    suspend fun sendGoogleIdToken(@Body idToken: RequestGoogleIdToken) : Response<ResponseGoogleIdToken>

    // UXN 로그인
    @POST("/api/login")
    suspend fun loginNormal(@Body signInInfo: RequestSignInNormal) : Response<ResponseSignInNormal>

    // 혈당입력 값 전송
    @POST("/api/event/glucose")
    suspend fun uploadGlucose(@Header("Authorization") token: String, @Body glucose: RequestGlucose) : Response<ResponseGlucose>

    // 기기정보로 MAC 가져오기
    @POST("/api/device")
    suspend fun getDeviceMac(@Header("Authorization") token: String, @Body deviceNumber: RequestDeviceNumber) : Response<ResponseDeviceMac>

    // 간편로그인 회원 체크
    @GET("/api/user/check")
    suspend fun userCheck(
        @Query("check") check: String,
        @Query("login_type") loginType: Int
    ): Response<ResponseUserCheck>
}