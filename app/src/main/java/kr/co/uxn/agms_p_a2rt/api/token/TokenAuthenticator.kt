package kr.co.uxn.agms_p_a2rt.api.token

import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kr.co.uxn.agms_p_a2rt.AuthEvent
import kr.co.uxn.agms_p_a2rt.AuthEventNotifier
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.refreshRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestRefreshToken
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {

    // 1. Mutex 인스턴스 생성 (동기화 락 역할)
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("AUTH", "authenticator 진입")

        // 2. 무한 루프 방지 (기존 코드 유지)
        if (responseCount(response) >= 2) {
            Log.d("AUTH", "response count가 2이상으로 return null")
            return null
        }

        return runBlocking {
            // 3. Mutex 획득 (한 번에 하나의 스레드만 진입)
            mutex.withLock {

                // 4. [중요] 락을 얻은 후, 현재 저장된 토큰을 다시 확인합니다.
                // 대기하던 다른 요청이 이미 토큰을 갱신했을 수 있기 때문입니다.
                val currentAccessToken = DataStoreManager.getAccessToken().first()
                val currentRefreshToken = DataStoreManager.getRefreshToken().first()
                val userId = DataStoreManager.getUserId().first() ?: -1

                // 방금 실패한 요청(response)의 헤더에 있던 토큰 추출
                val failedAccessToken = response.request.header("Authorization")
                    ?.replace("Bearer ", "")

                // 5. 시나리오 분기
                // Case A: 저장소의 토큰이 요청 실패한 토큰과 다르다면? -> 이미 다른 요청이 갱신 성공함!
                if (currentAccessToken != null && currentAccessToken != failedAccessToken) {
                    Log.d("AUTH", "이미 다른 스레드에서 토큰이 갱신되었습니다. 재요청만 진행합니다.")
                    return@withLock newRequestWithAccessToken(response.request, currentAccessToken)
                }

                // Case B: 여전히 토큰이 같다면? -> 내가 갱신해야 함 (API 호출)
                Log.d("AUTH", "토큰 갱신 API 호출 시작")

                // 리프레시 토큰이 없거나 유저 아이디가 없으면 종료
                if (currentRefreshToken.isNullOrEmpty() || userId == -1) {
                    return@withLock null
                }

                try {
                    val tokenResponse = refreshRetrofit.getNewAccessToken(
                        refreshToken = "Bearer $currentRefreshToken",
                        userInfo = RequestRefreshToken(userId, currentRefreshToken)
                    )

                    if (tokenResponse.isSuccessful && tokenResponse.body() != null) {
                        val newAccessToken = tokenResponse.body()!!.accessToken

                        // 새 토큰 저장
                        DataStoreManager.deleteAccessToken()
                        DataStoreManager.saveAccessToken(newAccessToken)
                        Log.d("AUTH", "토큰 갱신 및 저장 성공")

                        return@withLock newRequestWithAccessToken(response.request, newAccessToken)
                    } else {
                        val errorCode = tokenResponse.code()

                        if (errorCode == 401) {
                            Log.e("AUTH", "다중로그인 발생 401 에러")
                            AuthEventNotifier.notify(AuthEvent.DUPLICATE_LOGIN)
                            return@withLock null
                        }

                        Log.e("AUTH", "API 에러: ${tokenResponse.errorBody()?.string()}, 에러 코드 : $errorCode")
                        return@withLock null
                    }
                } catch (e: Exception) {
                    Log.e("AUTH", "네트워크 에러: ${e.message}")
                    return@withLock null
                }
            }
        }
    }

    // 헤더만 교체해서 Request를 다시 만드는 헬퍼 함수
    private fun newRequestWithAccessToken(request: Request, accessToken: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }

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