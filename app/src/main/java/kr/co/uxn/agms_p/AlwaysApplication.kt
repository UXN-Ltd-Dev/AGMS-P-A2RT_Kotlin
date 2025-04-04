package kr.co.uxn.agms_p

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import kr.co.uxn.agms_p.api.token.TokenManager
import kr.co.uxn.agms_p.ble.BleRepository

class AlwaysApplication : Application() {
    lateinit var bleRepository: BleRepository
    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.kakao_native_app_key)

        // 토큰매니저 초기화
        TokenManager.init(this)

        bleRepository = BleRepository()
    }
}