package kr.co.uxn.agms_p.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.User
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.BuildConfig
import kr.co.uxn.agms_p.api.model.requestDTO.RequestKakaoAccessCode
import kr.co.uxn.agms_p.api.RetrofitClient.retrofitMachine
import kr.co.uxn.agms_p.api.model.requestDTO.RequestGoogleIdToken

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "LoginViewModel"
    }

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    val isLoggedIn = MutableStateFlow<Boolean>(false)
    val userInfo = MutableStateFlow<User>(User(0, "test", "test", "test"))
    var userIdentity = ""
    var signUpType = -1

    private val _navigationEvent = MutableStateFlow<LoginNavigationEvent?>(null)
    val navigationEvent = _navigationEvent


    fun kakaoLogin(activityContext: Context) {
        viewModelScope.launch {
            handleKakaoLogin(activityContext)
        }
    }

    @SuppressLint("LogNotTimber")
    fun googleLogin(activityContext: Context) {
//        viewModelScope.launch(Dispatchers.IO) {
        viewModelScope.launch() {
            val googleWebClientId = BuildConfig.google_web_client_id
            Log.e(TAG, "webClientId : $googleWebClientId ")

            val googleIdOption = GetSignInWithGoogleOption.Builder(googleWebClientId)
                .build()

            val request: GetCredentialRequest = Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)

            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext,
                )
                handleGoogleLogin(result)
            } catch (e: Exception) {
                Log.e(TAG, "구글로그인 실패 : $e")
            }
        }
    }


    @SuppressLint("LogNotTimber")
    suspend fun handleGoogleLogin(result: GetCredentialResponse) {
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
//            // GoogleIdToken credential
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


                        Log.e(TAG, "idToken : $idToken\nphone : $phoneNumber\nemail : $email\nname : $name\npictureUri : $pictureUri")

                        // idToken 해독
//                        val verifier = GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory())
//                                                .setAudience(listOf(BuildConfig.google_web_client_id))
//                                                .build()
//
//                        val verifiedIdToken = verifier.verify(idToken) ?: throw Exception("Google IdToken이 유효하지 않아 회원 정보를 가져올 수 없습니다.")
//                        // 토큰에 존재하는 이메일 정보
//                        val email2 = verifiedIdToken.payload.email
//                        Log.e("TAG", "해독된 email2 : $email2")

                        // 서버에 토큰전송
                        isMemberCheck(idToken, 2)

//                        sendGoogleIdToken(idToken)

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

    @SuppressLint("LogNotTimber")
    suspend fun handleKakaoLogin(activityContext: Context) {
        // 카카오톡이 설치되어 있으면
        if (AuthCodeClient.instance.isKakaoTalkLoginAvailable(activityContext)) {
            // 카카오톡으로 로그인
            AuthCodeClient.instance.authorizeWithKakaoTalk(activityContext) { authCode, error ->
                val accessCode = authCode.toString()
                Log.e(TAG, "카카오 인가코드 : $authCode")
                isMemberCheck(accessCode, 1)
//                sendKakaoAccessCode(accessCode)
            }
        } else {
            // 카카오계정으로 로그인
            AuthCodeClient.instance.authorizeWithKakaoAccount(activityContext) { authCode, error ->

                val accessCode = authCode.toString()
                Log.e(TAG, "카카오 인가코드 : $authCode")

//                sendKakaoAccessCode(accessCode)
                isMemberCheck(accessCode, 1)

            }
        }
    }

//    @SuppressLint("LogNotTimber")
//    fun sendKakaoAccessCode(accessCode: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val response =
//                    retrofitMachine.sendKakaoAccessCode(RequestKakaoAccessCode(accessCode))
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    if (responseBody != null) {
//
//                        if (responseBody.isSignUp) {
//                            // 회원가입이 되어있으면, 세팅 화면으로 보내는 이벤트 처리.
//                            _navigationEvent.value =
//                                LoginNavigationEvent.NavigateToSettingPermission("세팅화면으로 이동")
//                        } else {
//                            // 회원가입이 안되어있으면, 회원가입 화면으로 보내는 이벤트 처리
//                            _navigationEvent.value =
//                                LoginNavigationEvent.NavigateToSignUp("회원가입화면으로 이동")
//                        }
//                        withContext(Dispatchers.Main) {
//                            Log.d("TAG", "서버 응답: $responseBody")
//                        }
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Log.e("TAG", "네트워크 오류 발생: ${e.message}")
//                }
//            }
//        }
//    }

    @SuppressLint("LogNotTimber")
    fun sendKakaoAccessCode(accessCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 로그인 타입 : 카카오(1)
                val response = retrofitMachine.userCheck(accessCode, 1)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.isUserExist) {
                            // 회원가입이 되어있으면, 세팅 화면으로 보내는 이벤트 처리.
                            _navigationEvent.value =
                                LoginNavigationEvent.NavigateToSettingPermission("세팅화면으로 이동")
                        } else {
                            // 회원가입이 안되어있으면, 회원가입 화면으로 보내는 이벤트 처리
                            _navigationEvent.value =
                                LoginNavigationEvent.NavigateToSignUp("회원가입화면으로 이동")
                            userIdentity = responseBody.userIdentity
                        }
                        withContext(Dispatchers.Main) {
                            Log.d("TAG", "서버 응답: $responseBody")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("TAG", "네트워크 오류 발생: ${e.message}")
                }
            }
        }
    }

    @SuppressLint("LogNotTimber")
    fun isMemberCheck(authenticationCode: String, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 로그인 타입 : 카카오(1)
                // 로그인 타입 : 구글(2)
                val response = retrofitMachine.userCheck(authenticationCode, type)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.isUserExist) {
                            // 회원가입이 되어있으면, 세팅 화면으로 보내는 이벤트 처리.
                            _navigationEvent.value =
                                LoginNavigationEvent.NavigateToSettingPermission("세팅화면으로 이동")
                        } else {
                            // 회원가입이 안되어있으면, 회원가입 화면으로 보내는 이벤트 처리
                            _navigationEvent.value =
                                LoginNavigationEvent.NavigateToSignUp("회원가입화면으로 이동")
                            userIdentity = responseBody.userIdentity
                            signUpType = type
                        }
                        withContext(Dispatchers.Main) {
                            Log.d("TAG", "서버 응답: $responseBody")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("TAG", "네트워크 오류 발생: ${e.message}")
                }
            }
        }
    }




//    @SuppressLint("LogNotTimber")
//    fun sendGoogleIdToken(idToken: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val result =
//                    retrofitMachine.sendGoogleIdToken(RequestGoogleIdToken(idToken = idToken))
//                if (result.isSuccessful) {
//                    val resultBody = result.body()
//                    withContext(Dispatchers.Main) {
//                        Log.e("TAG", "API 성공: ${resultBody.toString()}")
//                    }
//                    if (resultBody != null) {
//                        // 회원가입 테스트 성공 여부
//                        if (resultBody.isSignup) {
//                            // 회원가입이 되어있으면, 세팅 화면으로 보내는 이벤트 처리.
//                            _navigationEvent.value =
//                                LoginNavigationEvent.NavigateToSettingPermission("세팅화면으로 이동")
//                        } else {
//                            // 회원가입이 안되어있으면, 회원가입 화면으로 보내는 이벤트 처리
//                            _navigationEvent.value =
//                                LoginNavigationEvent.NavigateToSignUp("회원가입화면으로 이동")
//                        }
//                        withContext(Dispatchers.Main) {
//                            Log.d("TAG", "서버 응답: $resultBody")
//                        }
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        Log.e("TAG", "API 실패: ${result.errorBody()?.string()}")
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Log.e("TAG", "네트워크 오류 발생: ${e.message}")
//                }
//            }
//        }
//    }

    @SuppressLint("LogNotTimber")
    fun sendGoogleIdToken(idToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 로그인 타입 : 구글(2)
                val result =
                    retrofitMachine.userCheck(idToken, 2)
                if (result.isSuccessful) {
                    val resultBody = result.body()
                    withContext(Dispatchers.Main) {
                        Log.e("TAG", "API 성공: ${resultBody.toString()}")
                    }
                    if (resultBody != null) {
                        // 회원가입 테스트 성공 여부
                        if (resultBody.isUserExist) {
                            // 회원가입이 되어있으면, 세팅 화면으로 보내는 이벤트 처리.
                            _navigationEvent.value =
                                LoginNavigationEvent.NavigateToSettingPermission("세팅화면으로 이동")
                        } else {
                            // 회원가입이 안되어있으면, 회원가입 화면으로 보내는 이벤트 처리
                            _navigationEvent.value =
                                LoginNavigationEvent.NavigateToSignUp("회원가입화면으로 이동")
                        }
                        withContext(Dispatchers.Main) {
                            Log.d("TAG", "서버 응답: $resultBody")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("TAG", "API 실패: ${result.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("TAG", "네트워크 오류 발생: ${e.message}")
                }
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

sealed class LoginNavigationEvent {
    data class NavigateToSettingPermission(val test: String) : LoginNavigationEvent()
    data class NavigateToSignUp(val test: String) : LoginNavigationEvent()
}
