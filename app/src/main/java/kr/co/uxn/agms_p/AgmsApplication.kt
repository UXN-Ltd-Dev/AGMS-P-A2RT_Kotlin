package kr.co.uxn.agms_p

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class AgmsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.kakao_native_app_key)
    }
}