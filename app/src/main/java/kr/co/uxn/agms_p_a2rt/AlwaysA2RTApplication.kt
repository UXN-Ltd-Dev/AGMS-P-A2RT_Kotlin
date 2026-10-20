package kr.co.uxn.agms_p_a2rt

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.kakao.sdk.common.KakaoSdk
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ble.BleManager.Companion.TEST
import kr.co.uxn.agms_p_a2rt.ble.SendDataWorker
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import java.util.concurrent.TimeUnit

class AlwaysA2RTApplication : Application() {

    private val localDbRepository by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, BuildConfig.kakao_native_app_key)

        // 토큰매니저 초기화
        DataStoreManager.init(this)

        // 파이썬 초기화
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        silencePythonOutputInRelease()


        // db에 더미 저장

        // 무결성 검사 - 확인만 하고 집행하지 않는다
        checkAppSignature()

        // 서비스 워치독 등록 - 15 분마다 서비스가 살아 있는지 보고, 죽었으면 되살린다.
        // AGMS-P 의 scheduleServiceWork() 와 같은 자리다.
        uploadWorkRequest()

    }

    private fun checkAppSignature() {
        val VALID_SIGNATURE_HASH =
            "5FB974AD26663D6CECA631E88F6CBA7243192828FE1062E4DB25F4E803EC164E"

        val expected = hexToBytes(VALID_SIGNATURE_HASH)
        if (expected == null) {
            // 지문이 아니라 상수가 깨진 경우다. 서명을 의심할 일이 아니다.
            Log.e(TEST, "무결성 검증 건너뜀 - 지문 상수가 16진수 64자가 아니다")
            return
        }

        val matched = packageManager.hasSigningCertificate(
            packageName,
            expected,
            PackageManager.CERT_INPUT_SHA256
        )

        if (matched) {
            Log.d(TEST, "무결성 검증 통과")
        } else {
            Log.w(TEST, "무결성 검증 불일치 - 서명 지문이 다르다")
        }
    }

    /**
     * 릴리즈에서는 파이썬 print 가 logcat(python.stdout) 으로 나가지 않게 한다.
     *
     * Kotlin 의 Log 는 R8 규칙으로 지우지만 파이썬 출력은 Chaquopy 가 따로 logcat 에
     * 쓰므로 여기서 막는다. 계산 스크립트가 측정값 목록을 통째로 찍는다.
     * __stdout__ 까지 바꾸는 이유: 스크립트가 출력을 잠시 가로챈 뒤
     * sys.stdout = sys.__stdout__ 로 되돌리기 때문이다.
     */
    private fun silencePythonOutputInRelease() {
        if (BuildConfig.DEBUG) return
        runCatching {
            val python = Python.getInstance()
            val sys = python.getModule("sys")
            val devNull = python.builtins.callAttr("open", python.getModule("os")["devnull"], "w")
            listOf("stdout", "stderr", "__stdout__", "__stderr__").forEach { sys[it] = devNull }
        }
    }

    private fun hexToBytes(hex: String): ByteArray? {
        if (hex.length != 64) return null
        return try {
            ByteArray(32) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * 서비스 워치독을 등록한다. 15 분은 WorkManager 가 허용하는 가장 짧은 주기다.
     *
     * **제약을 걸지 않는다.** 전에는 네트워크 연결을 요구했는데, 서비스를 되살리는
     * 데 망은 필요 없고 걸어 두면 망이 없을 때 - 되살릴 필요가 더 큰 때 - 돌지 않는다.
     *
     * 정책은 KEEP 이다. 앱이 뜰 때마다 불리는데 REPLACE 로 두면 그때마다 주기가
     * 처음부터 다시 세어져, 자주 껐다 켜는 사용자에게는 영영 돌지 않는다.
     */
    fun uploadWorkRequest() {
        val serviceWorkerRequest =
            PeriodicWorkRequestBuilder<SendDataWorker>(15, TimeUnit.MINUTES).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sendServer",
            ExistingPeriodicWorkPolicy.KEEP,
            serviceWorkerRequest
        )

        Log.e(TEST, "===========SERVICE WATCHDOG 등록===============")
    }

}

