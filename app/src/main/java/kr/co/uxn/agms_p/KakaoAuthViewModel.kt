package kr.co.uxn.agms_p

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class KakaoAuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "KakaoAuthViewModel"
    }

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    val isLoggedIn = MutableStateFlow<Boolean>(false)
    val userInfo = MutableStateFlow<User>(User(0, "test", "test", "test"))

    fun kakaoLogin(activityContext: Context) {
        viewModelScope.launch {
            isLoggedIn.emit(handleKakaoLogin(activityContext))
        }

    }
    fun kakaoLogout() {
        viewModelScope.launch {
            if (handleKakaoLogout()) {
                isLoggedIn.emit(false)
            } else {
                isLoggedIn.emit(true)
            }
        }
    }

    fun getUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                Log.i(TAG, "사용자 정보 요청 성공" +
                        "\n회원번호: ${user.id}" +
                        "\n이메일: ${user.kakaoAccount?.email}" +
                        "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                        "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")
                viewModelScope.launch {
                    userInfo.emit(User(user.id, user.kakaoAccount?.email ?: "", user.kakaoAccount?.profile?.nickname ?: "", user.kakaoAccount?.profile?.thumbnailImageUrl ?: ""))
                }
            }
        }
    }

    private suspend fun handleKakaoLogout() : Boolean =
        suspendCoroutine<Boolean> { continutation ->
            // 로그아웃
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                    continutation.resume(false)
                }
                else {
                    Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                    continutation.resume(true)
                }
            }
        }

    private suspend fun handleKakaoLogin(activityContext: Context): Boolean =
    // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        suspendCoroutine<Boolean> { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                    continuation.resume(false)
                } else if (token != null) {
                    Log.i(TAG, "카카오계정으로 로그인 성공\n" +
                            "accessToken : ${token.accessToken}\n" +
                            "idToken : ${token.idToken}\n" +
                            "refreshToken : ${token.refreshToken}\n" +
                            "accessTokenExpiresAt : ${token.accessTokenExpiresAt}\n" +
                            "refreshTokenExpiresAt : ${token.refreshTokenExpiresAt}"

                    )

                    continuation.resume(true)
                }
            }

            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activityContext)) {
                UserApiClient.instance.loginWithKakaoTalk(activityContext) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패, $error")

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(activityContext, callback = callback)
                    } else if (token != null) {
                        Log.i(
                            TAG,
                            "카카오톡으로 로그인 성공\n" +
                                    "accessToken : ${token.accessToken}\n" +
                                    "idToken : ${token.idToken}\n" +
                                    "refreshToken : ${token.refreshToken}\n" +
                                    "accessTokenExpiresAt : ${token.accessTokenExpiresAt}\n" +
                                    "refreshTokenExpiresAt : ${token.refreshTokenExpiresAt}"
                        )

                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(activityContext, callback = callback)
            }
        }

}