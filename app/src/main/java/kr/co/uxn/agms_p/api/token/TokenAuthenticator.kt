package kr.co.uxn.agms_p.api.token

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.co.uxn.agms_p.api.RetrofitClient
import kr.co.uxn.agms_p.api.RetrofitClient.refreshRetrofit
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestRefreshToken
import kr.co.uxn.agms_p.ui.viewmodel.AuthEventNotifier
import okhttp3.Authenticator
import okhttp3.Response
import okhttp3.Route
import okhttp3.Request

// 401 에러가 났을경우, 리프레쉬토큰을 헤더에 담아서 요청
class TokenAuthenticator() : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {

        Log.d("AUTH", "authenticator 진입")

        if (responseCount(response) >= 2) return null

        val refreshToken = runBlocking {
            DataStoreManager.getRefreshToken().first()
        }
        val userId = runBlocking {
            DataStoreManager.getUserId().first() ?: -1
        }

//        if (response.message == "REFRESH_TOKEN_EXPIRED") {
//            // 이벤트 전송
//            // 메인 엑티비티에서 수신하다고 있다가, 로그인화면으로 이동.
//            AuthEventNotifier.notifyRefreshTokenExpired()
//            response.close()
//            return null
//        }

        if (refreshToken.isNullOrEmpty()) {
            response.close()
            return null
        }

//        runBlocking {
//            val result = tokenRetrofit.getNewAccessToken(RequestRefreshToken(userId, refreshToken))
//            val newAccessToken = result.body()?.accessToken
//            if (newAccessToken != null) {
//                DataStoreManager.deleteAccessToken()
//                DataStoreManager.saveAccessToken(newAccessToken)
//            }
//        }
//        if (response.code == 401) {
//            // 이벤트 전송
//            // 메인엑티비에서 수신하다고 있다가, 로그인화면으로 이동.
//            Log.d("AUTH", "TOKEN Expired")
//            response.close()
//            return null
//        }

        return newRequestWithToken(refreshToken, userId, response.request)
    }

    private fun newRequestWithToken(refreshToken: String, userId: Int, request: Request): Request? {
        Log.d("TEST", "newRequestWithToken() 0 call!")
        if (userId == -1) {
            return null
        }

        Log.d("TEST", "newRequestWithToken() 1 call!")


        val newAccessToken: String? = runBlocking {
            try {
                Log.d("TEST", "newRequestWithToken() 3 call!")
                val response = refreshRetrofit.getNewAccessToken(
                    refreshToken = "Bearer $refreshToken",
                    userInfo = RequestRefreshToken(userId, refreshToken)
                    )
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("TEST", "newRequestWithToken() 4 call!")
                    if (responseBody != null) {

                        Log.d("TEST", "newRequestWithToken() 5 call!")
                        Log.d("TEST", "body : ${responseBody.toString()}")
                        DataStoreManager.deleteAccessToken()
                        DataStoreManager.saveAccessToken(responseBody.accessToken)
//                        Log.d("TEST", "새 accessToken 저장 완료: ${responseBody.accessToken}")
                        return@runBlocking responseBody.accessToken
                    }
                } else {
                    Log.e("TEST", "API 에러 : ${response.errorBody()?.string()}")
                    Log.d("TEST", "newRequestWithToken() 6 call!")
                }
            } catch (e: Exception) {
                Log.e("TEST", "네트워크 에러: ${e.message}")
                Log.d("TEST", "newRequestWithToken() 7 call!")
            }
            Log.d("TEST", "newRequestWithToken() 8 call!")
            null
        }
//        Log.d("TEST", "newAccessToken : ${newAccessToken}")
        Log.d("TEST", "newRequestWithToken() 9 call!")

        if (newAccessToken == null) return null

        // 새 accessToken으로 원래 요청 복사
        return request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }


    // Too many follow-up requests: 21 에러 방어코드
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

}