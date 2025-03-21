package kr.co.uxn.agms_p.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.User
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.credentials.GetCredentialRequest.Builder
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.AuthCodeClient
import kr.co.uxn.agms_p.BuildConfig
import kr.co.uxn.agms_p.api.model.requestDTO.RequestKakaoAccessCode
import kr.co.uxn.agms_p.api.RetrofitClient.retrofitMachine

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "LoginViewModel"
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

    fun testRetrofit() {

        viewModelScope.launch {
            try {
                val response = retrofitMachine.testRetrofit()
                if (response.isSuccessful) {
                    Log.d("TAG", "서버 응답: ${response.body()}")
                } else {
                    Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TAG", "네트워크 오류 발생: ${e.message}")
            }
        }
    }

//    fun getKakaoCode() {
//        AuthCodeClient.instance.isKakaoTalkLoginAvailable(context) { authCode, error ->
//            if (error != null) {
//                Log.e("KakaoAuth", "인가 코드 요청 실패: ${error.message}")
//            } else if (authCode != null) {
//                Log.d("KakaoAuth", "인가 코드 획득 성공: $authCode")
//            }
//        }
//    }

    fun googleLogin(activityContext: Context) {
        val googleWebClientId = BuildConfig.google_web_client_id
        Log.e(TAG, "webClientId : $googleWebClientId ")

        val googleIdOption = GetSignInWithGoogleOption.Builder(googleWebClientId)
//            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = Builder()
            .addCredentialOption(googleIdOption)
            .build()

        Log.e(TAG, "request : ${request.credentialOptions} ")

        val credentialManager = CredentialManager.create(activityContext)

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext,
                )
                handleSignIn(result)
            } catch (e: Exception) {
                Log.e(TAG, "구글로그인 실패 : $e ")
            }
        }
    }


    fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            // Passkey credential
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse on your server to
                // validate and authenticate
                val responseJson = credential.authenticationResponseJson
                Log.e("TAG", "responseJson: $responseJson")
            }
            // Password credential
            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
            }
            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract the ID to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken
                        val phoneNumber = googleIdTokenCredential.phoneNumber
                        val email = googleIdTokenCredential.id
                        val name = googleIdTokenCredential.displayName
                        val pictureUri = googleIdTokenCredential.profilePictureUri

                        Log.e(
                            TAG,
                            "idToken : $idToken\nphone : $phoneNumber\nemail : $email\nname : $name\npictureUri : $pictureUri"
                        )
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private suspend fun handleKakaoLogout(): Boolean =
        suspendCoroutine<Boolean> { continutation ->
            // 로그아웃
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                    continutation.resume(false)
                } else {
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
                    Log.i(
                        TAG, "카카오계정으로 로그인 성공\n" +
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
//
                AuthCodeClient.instance.authorizeWithKakaoTalk(activityContext) { authCode, error ->

                    val accessCode = authCode.toString()
                    Log.e(TAG, "authCode : $authCode")

                    sendAccessCode(accessCode)


                }

                UserApiClient.instance.loginWithKakaoTalk(activityContext) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패, $error")

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(
                            activityContext,
                            callback = callback
                        )
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

    fun sendAccessCode(accessCode: String) {
        viewModelScope.launch {
            try {
                val response = retrofitMachine.sendKakaoAccessCode(RequestKakaoAccessCode(accessCode))

                if (response.isSuccessful) {
                    Log.d("TAG", "서버 응답: ${response.body()}")
                } else {
                    Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TAG", "네트워크 오류 발생: ${e.message}")
            }
        }

    }
}


