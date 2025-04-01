package kr.co.uxn.agms_p.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "RegisterViewModel"
    }

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _isGrant = MutableStateFlow(false)
    val isGrant: StateFlow<Boolean> = _isGrant


    fun changeGrantState(isGrant: Boolean) {
        Log.e("RegisterViewModel", "뷰모델 내부에서 받은 isGrant값은 : $isGrant")
        _isGrant.value = isGrant
    }
}