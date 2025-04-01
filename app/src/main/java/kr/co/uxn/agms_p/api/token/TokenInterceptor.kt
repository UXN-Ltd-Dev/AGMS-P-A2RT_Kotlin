package kr.co.uxn.agms_p.api.token

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
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_OK
import java.util.Timer

class TokenInterceptor() : Interceptor {
    companion object {
        const val NETWORK_ERROR = 401
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val token: String = runBlocking {
            TokenManager.getAccessToken().first()
        } ?: return errorResponse(chain.request())

        Timber.e("TokenManager.getAcceesToken : ${token}")


        // 새로 받아온 토큰이 있으면 저장하는 로직
        val request = chain.request().newBuilder().header("AUTHORIZATION", "Bearer $token").build()

        val response = chain.proceed(request)
        if (response.code == HTTP_OK) {
            val newAccessToken: String = response.header("AUTHORIZATION", null) ?: return response
            Timber.d("new Access Token = ${newAccessToken}")

            CoroutineScope(Dispatchers.IO).launch {
                val existedAccessToken = TokenManager.getAccessToken().first()
                if (existedAccessToken != newAccessToken) {
                    TokenManager.deleteAccessToken()
                    TokenManager.saveAccessToken(newAccessToken)
                    Timber.d("newAccessToken = ${newAccessToken}\nExistedAccessToken = ${existedAccessToken}")
                }
            }
        } else {
            Timber.e("${response.code} : ${response.request} \n ${response.message}")
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