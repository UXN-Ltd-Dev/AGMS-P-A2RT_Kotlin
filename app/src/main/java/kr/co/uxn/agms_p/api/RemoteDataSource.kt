package kr.co.uxn.agms_p.api


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Path

interface RemoteDataSource {
//    @POST("/data_collect/insert_device")
//    fun registerDeviceToServer(@Body devices: RegisterDevice): Call<ResponseData>
    @GET("/api/sample_test/hello")
    suspend fun testRetrofit() : Response<String>

    @POST("/api/oauth/mobile/kakao")
    suspend fun sendAccessCode(@Body accessCode: RequestKCode) : Response<ResponseKCode>
}