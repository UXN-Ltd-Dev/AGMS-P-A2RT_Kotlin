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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.gson.Gson
import com.kakao.sdk.auth.AuthCodeClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.BuildConfig
import kr.co.uxn.agms_p.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestOAuthSignUpAndLogin
import kr.co.uxn.agms_p.api.model.responseDTO.ResponseSignUpOauthAndLogin
import kr.co.uxn.agms_p.api.token.DataStoreManager

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "LoginViewModel"
    }

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    val isLoggedIn = MutableStateFlow<Boolean>(false)
    val userInfo = MutableStateFlow<User>(User(0, "test", "test", "test"))

    var userIdTest = -1
    var signUpType = -1

    private val _navigationEvent = MutableStateFlow<LoginNavigationEvent?>(null)
    val navigationEvent = _navigationEvent

    // 로딩 인디케이터를 위한 변수
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 중복 로그인 (일반)
    private val _isShowDuplicateLoginDialog = MutableStateFlow(false)
    val isShowDuplicateLoginDialog: StateFlow<Boolean> = _isShowDuplicateLoginDialog

    // 중복 로그인 (oAuth)
    private val _isShowDuplicateKakaoLoginDialog = MutableStateFlow(OauthDuplicateStatus(false, -1))
    val isShowDuplicateKakaoLoginDialog: StateFlow<OauthDuplicateStatus> = _isShowDuplicateKakaoLoginDialog

    // oAuth 다중로그인용 이메일 (우선은 카카오만)
    private val _oAuthEmail = MutableStateFlow("")
    val oAuthEmail: StateFlow<String> = _oAuthEmail



    fun updateIsLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun updateUserId(userId: Int) {
        this@LoginViewModel.userIdTest = userId
    }

    fun kakaoLogin(activityContext: Context, isForced: Boolean) {
        viewModelScope.launch {
            handleKakaoLogin(activityContext, isForced)
        }
    }

    fun showDuplicateLoginDialog(isShow: Boolean) {
        _isShowDuplicateLoginDialog.value = isShow
    }

    fun showDuplicateKakaoLoginDialog(isShow: Boolean, type: Int) {
//        _isShowDuplicateKakaoLoginDialog.value = isShow
        _isShowDuplicateKakaoLoginDialog.update { it ->
            it.copy (
                isShow = isShow,
                type = type
            )
        }
    }


    fun apiTest() {
        viewModelScope.launch {
            try {
                val result = emptyRetrofit.testRetrofit()
                if (result.isSuccessful) {
                    val resultBody = result.body()
                    if (resultBody != null) {
                        Log.e(TAG, "apiTest : $resultBody")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "네트워크 에러 : $e")
            }
        }
    }

    @SuppressLint("LogNotTimber")
    fun googleLogin(activityContext: Context, isForced: Boolean) {
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
                handleGoogleLogin(result, isForced)
            } catch (e: Exception) {
                Log.e(TAG, "구글로그인 실패 : $e")
                updateIsLoading(false)
            }
        }
    }


    @SuppressLint("LogNotTimber")
    suspend fun handleGoogleLogin(result: GetCredentialResponse, isForced: Boolean) {
        val credential = result.credential
        when (credential) {
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

                        Log.e(TAG, "idToken : $idToken\nphone : $phoneNumber\nemail : $email\nname : $name\npictureUri : $pictureUri")
                        // 회원 여부 체크
                        isMemberCheckAndLogin(idToken, 1801, isForced)

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
    suspend fun handleKakaoLogin(activityContext: Context, isForced: Boolean) {
        // 카카오톡이 설치되어 있으면
        if (AuthCodeClient.instance.isKakaoTalkLoginAvailable(activityContext)) {
            // 카카오톡으로 로그인
            AuthCodeClient.instance.authorizeWithKakaoTalk(activityContext) { authCode, error ->
                val accessCode = authCode.toString()
//                Log.e(TAG, "카카오 인가코드 : $authCode")
                isMemberCheckAndLogin(accessCode, 1802, isForced)
            }
        } else {
            // 카카오계정으로 로그인
            AuthCodeClient.instance.authorizeWithKakaoAccount(activityContext) { authCode, error ->
                val accessCode = authCode.toString()
//                Log.e(TAG, "카카오 인가코드 : $authCode")
                isMemberCheckAndLogin(accessCode, 1802, isForced)
            }
        }
    }


    @SuppressLint("LogNotTimber")
    fun isMemberCheckAndLogin(authenticationCode: String, type: Int, isForced: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 로그인 타입 : 구글(1801)
                // 로그인 타입 : 카카오(1802)

                if (!isForced) {
                    val response = emptyRetrofit.oAuthSignUpAndLogin(RequestOAuthSignUpAndLogin(authenticationCode, type, isForced = false))
                    val httpCode = response.code()
                    Log.e("TEST", "http 코드 : $httpCode")

                    if (response.isSuccessful && httpCode == 200) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            if (responseBody.isSignUp) {
                                // isSignUp = true, 회원 가입
                                val accessToken = responseBody.accessToken
                                val refreshToken = responseBody.refreshToken

                                val oAuthEmail = responseBody.email
                                Log.d("TEST", "responseBody.email: $oAuthEmail")

                                val preToken = responseBody.preToken
                                Log.d("TEST", "responseBody.preToken: $preToken")

                                val userId = responseBody.userId
                                // 토큰 저장

                                DataStoreManager.deletePreToken()
                                DataStoreManager.deleteEmail()
                                DataStoreManager.savePreToken(preToken)

                                // mac 정리
                                DataStoreManager.deleteDeviceMac()
                                // 회원가입 화면으로 이동
                                _navigationEvent.value = LoginNavigationEvent.NavigateToSignUp(userId, type.toString(), oAuthEmail)

                            } else {
                                // isSignUp 이 false 일 경우, 로그인
                                val accessToken = responseBody.accessToken
                                val refreshToken = responseBody.refreshToken
                                val userId = responseBody.userId
                                val email = responseBody.email
                                // 토큰 저장
                                DataStoreManager.deleteAccessToken()
                                DataStoreManager.deleteUserId()
                                DataStoreManager.deleteRefreshToken()
                                DataStoreManager.deleteEmail()

                                DataStoreManager.saveAccessToken(accessToken)
                                DataStoreManager.saveRefreshToken(refreshToken)
                                DataStoreManager.saveUserId(userId)
                                DataStoreManager.saveEmail(email)
                                // mac 정리
                                DataStoreManager.deleteDeviceMac()
                                // 토큰 저장 테스트
                                val verifyAccessToken = DataStoreManager.getAccessToken().first()
                                val verifyRefreshToken = DataStoreManager.getRefreshToken().first()
                                val verifyUserId = DataStoreManager.getUserId().first()
                                withContext(Dispatchers.Main) {
//                                Log.d("TEST", "TokenManager | accessToken : $verifyAccessToken\nrefreshToken : $verifyRefreshToken\nuserId : $verifyUserId")
                                    updateIsLoading(false)
                                }
                                // 세팅 화면으로 이동
                                _navigationEvent.value = LoginNavigationEvent.NavigateToSettingPermission(type)
                            }
                        }
                    } else if (httpCode == 409) { // 다중로그인 인식
                        Log.d("TEST", "409 로 진입")
                        val errorBody = response.errorBody()?.string()
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, ResponseSignUpOauthAndLogin::class.java)
                        if (errorResponse != null) {
                            Log.d("TEST", "카카오 409일때 응답받은 DTD : ${errorResponse.toString()}")
                            val email = errorResponse.email
                            val message = errorResponse.message
                            Log.d("TEST", "responseBody.email: $email")
                            Log.d("TEST", "responseBody.message: $message")

                            // 뷰모델에 email 저장
                            _oAuthEmail.value = email


                            withContext(Dispatchers.Main) {
                                Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
                                updateIsLoading(false)
                                showDuplicateKakaoLoginDialog(true, type = type)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
                            updateIsLoading(false)
                        }
                    }


                } else { // isForced == true
                    val response = emptyRetrofit.oAuthSignUpAndLogin(RequestOAuthSignUpAndLogin(oAuthEmail.value, type, isForced = true))

                    val httpCode = response.code()
                    Log.e("TEST", "http 코드 : $httpCode")

                    if (response.isSuccessful && httpCode == 200) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                                // isSignUp 이 false 일 경우, 로그인
                                val accessToken = responseBody.accessToken
                                val refreshToken = responseBody.refreshToken
                                val userId = responseBody.userId
                                val email = responseBody.email
                                // 토큰 저장
                                DataStoreManager.deleteAccessToken()
                                DataStoreManager.deleteUserId()
                                DataStoreManager.deleteEmail()
                                DataStoreManager.saveAccessToken(accessToken)
                                DataStoreManager.saveRefreshToken(refreshToken)
                                DataStoreManager.saveUserId(userId)
                                DataStoreManager.saveEmail(email)

                                // mac 정리
                                DataStoreManager.deleteDeviceMac()
                                // 토큰 저장 테스트
                                val verifyAccessToken = DataStoreManager.getAccessToken().first()
                                val verifyRefreshToken = DataStoreManager.getRefreshToken().first()
                                val verifyUserId = DataStoreManager.getUserId().first()
                                withContext(Dispatchers.Main) {
//                                Log.d("TEST", "TokenManager | accessToken : $verifyAccessToken\nrefreshToken : $verifyRefreshToken\nuserId : $verifyUserId")
                                    updateIsLoading(false)
                                }
                                // 세팅 화면으로 이동
                                _navigationEvent.value = LoginNavigationEvent.NavigateToSettingPermission(type)

                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e("TAG", "API 실패: ${response.errorBody()?.string()}")
                            updateIsLoading(false)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("TAG", "네트워크 오류 발생: ${e.message}")
                    updateIsLoading(false)
                }
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

sealed class LoginNavigationEvent {
    data class NavigateToSettingPermission(val type: Int) : LoginNavigationEvent()
    data class NavigateToSignUp(val userId: Int, val type: String, val oAuthEmail: String?) : LoginNavigationEvent()
}
