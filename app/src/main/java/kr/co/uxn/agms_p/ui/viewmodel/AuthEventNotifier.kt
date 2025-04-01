package kr.co.uxn.agms_p.ui.viewmodel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object AuthEventNotifier {
    private val _refreshTokenExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshTokenExpired: SharedFlow<Unit> = _refreshTokenExpired

    fun notifyRefreshTokenExpired() {
        _refreshTokenExpired.tryEmit(Unit)
    }
}