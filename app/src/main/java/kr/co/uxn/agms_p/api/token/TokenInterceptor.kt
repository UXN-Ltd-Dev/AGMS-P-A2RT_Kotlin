package kr.co.uxn.agms_p.api.token

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.HttpURLConnection.HTTP_OK

class TokenInterceptor() : Interceptor {
    companion object {
        const val NETWORK_ERROR = 401
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val token: String = runBlocking {
            DataStoreManager.getAccessToken().first()
        } ?: return errorResponse(chain.request())

        Log.e("TEST","DataStoreManager.getAcceesToken : ${token}")


        // 새로 받아온 토큰이 있으면 저장하는 로직
        val request = chain.request().newBuilder().header("AUTHORIZATION", "Bearer $token").build()

        val response = chain.proceed(request)
        if (response.code == HTTP_OK) {
            val newAccessToken: String = response.header("AUTHORIZATION", null) ?: return response
            Log.e("TEST","new Access Token = ${newAccessToken}")

            CoroutineScope(Dispatchers.IO).launch {
                val existedAccessToken = DataStoreManager.getAccessToken().first()
                if (existedAccessToken != newAccessToken) {
                    DataStoreManager.deleteAccessToken()
                    DataStoreManager.saveAccessToken(newAccessToken)
                    Log.e("TEST","newAccessToken = ${newAccessToken}\nExistedAccessToken = ${existedAccessToken}")
                }
            }
        } else {
            Log.e("TEST","${response.code} : ${response.request} \n ${response.message}")
        }

        return response
    }

    private fun errorResponse(request: Request): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_2)
        .code(NETWORK_ERROR)
        .message("")
//        .body(ResponseBody.create(null, ""))
        .body("".toResponseBody("text/plain".toMediaTypeOrNull()))
        .build()
}