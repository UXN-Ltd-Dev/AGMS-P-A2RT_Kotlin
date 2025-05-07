package kr.co.uxn.agms_p.api.token

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

        if (responseCount(response) >= 2) return null
        val refreshToken = runBlocking {
            DataStoreManager.getRefreshToken().first()
        }
        val userId = runBlocking {
            DataStoreManager.getUserId().first() ?: -1
        }

        if (response.message == "REFRESH_TOKEN_EXPIRED") {
            // 이벤트 전송
            // 메인엑티비에서 수신하다고 있다가, 로그인화면으로 이동.
            AuthEventNotifier.notifyRefreshTokenExpired()
            response.close()
            return null
        }

        if (refreshToken.isNullOrEmpty() || refreshToken == "LOGIN") {
            response.close()
            return null
        }

        if (response.message == "TOKEN Expired") {
            // 이벤트 전송
            // 메인엑티비에서 수신하다고 있다가, 로그인화면으로 이동.
            AuthEventNotifier.notifyRefreshTokenExpired()
            response.close()
            return null
        }

        return newRequestWithToken(refreshToken, response.request)
    }

//    private fun newRequestWithToken(refreshToken: String, userId: Int, request: Request): Request? {
//        if (userId == -1) {
//            return null
//        }
//
//        val newAccessToken: String? = runBlocking {
//            try {
//                val response = tokenRetrofit.getNewAccessToken(RequestRefreshToken(userId, refreshToken))
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    if (body != null && body.isSuccess) {
//                        DataStoreManager.deleteAccessToken()
//                        DataStoreManager.saveAccessToken(body.accessToken)
//                        Log.d("TEST", "새 accessToken 저장 완료: ${body.accessToken}")
//                        return@runBlocking body.accessToken
//                    }
//                } else {
//                    Log.e("TEST", "API 에러 : ${response.errorBody()?.string()}")
//                }
//            } catch (e: Exception) {
//                Log.e("TEST", "네트워크 에러: ${e.message}")
//            }
//            null
//        }
//
//        if (newAccessToken == null) return null
//
//        // 새 accessToken으로 원래 요청 복사
//        return request.newBuilder()
//            .header("Authorization", "Bearer $newAccessToken")
//            .build()
//    }

    private fun newRequestWithToken(refreshToken: String, request: Request): Request =
        request.newBuilder()
            .header("Authorization", "Bearer $refreshToken")
            .build()


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