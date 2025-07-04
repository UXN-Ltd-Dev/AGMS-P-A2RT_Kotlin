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

//        Log.e("TEST", "DataStoreManager.getAcceesToken : ${token}")
        val request = chain.request().newBuilder().header("Authorization", "Bearer $token").build()
        val response = chain.proceed(request)
        return response
    }

    private fun errorResponse(request: Request): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_2)
        .code(NETWORK_ERROR)
        .message("")
        .body("".toResponseBody("text/plain".toMediaTypeOrNull()))
        .build()
}