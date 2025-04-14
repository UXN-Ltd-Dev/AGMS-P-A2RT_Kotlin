package kr.co.uxn.agms_p.api.token

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

        return newRequestWithToken(refreshToken, response.request)
    }

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