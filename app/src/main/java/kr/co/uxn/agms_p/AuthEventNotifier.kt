package kr.co.uxn.agms_p

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthEventNotifier {
    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    suspend fun notify(event: AuthEvent) {
        _eventFlow.emit(event)
    }
}

// 이벤트 종류 정의
enum class AuthEvent {
    DUPLICATE_LOGIN
}