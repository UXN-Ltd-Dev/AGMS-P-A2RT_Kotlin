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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.co.uxn.agms_p.BuildConfig
import kr.co.uxn.agms_p.api.model.requestDTO.RequestKakaoAccessCode
import kr.co.uxn.agms_p.api.RetrofitClient.retrofitMachine

class BleViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "BleViewModel"
    }

    private val _isFindDevice = MutableStateFlow(false)
    val isFindDevice = _isFindDevice.asStateFlow()

    private val _events = MutableSharedFlow<String>(replay = 0)
    val events = _events.asSharedFlow()

    suspend fun emit(event: String) {
        _events.emit(event)
    }

    fun updateIsFindDevice(value: Boolean) {
        _isFindDevice.value = value
        Log.e(TAG, "_isFindDevice 값 : ${_isFindDevice.value}")
    }
}


