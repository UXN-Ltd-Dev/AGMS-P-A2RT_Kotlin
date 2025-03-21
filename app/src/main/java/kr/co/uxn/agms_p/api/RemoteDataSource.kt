package kr.co.uxn.agms_p.api


import kr.co.uxn.agms_p.api.model.requestDTO.RequestKakaoAccessCode
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseKakaoAccessCode
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response

interface RemoteDataSource {
//    @POST("/data_collect/insert_device")
//    fun registerDeviceToServer(@Body devices: RegisterDevice): Call<ResponseData>
    @GET("/api/sample_test/hello")
    suspend fun testRetrofit() : Response<String>

    @POST("/api/oauth/mobile/kakao")
    suspend fun sendKakaoAccessCode(@Body accessCode: RequestKakaoAccessCode) : Response<ResponseKakaoAccessCode>

}