package kr.co.uxn.agms_p_a2rt

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
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


        // db에 더미 저장

//        Toast.makeText(this, "무결성 검증 통과!", Toast.LENGTH_SHORT).show()

        val VALID_SIGNATURE_HASH = "9E9233211A9157E699D49D8C0E7A4289B180A1B3DC4B9EE7AC3C96A6AA86FAB1"


//        fun checkAppSignature() {
//            try {
//                val packageInfo = packageManager.getPackageInfo(
//                    packageName,
//                    PackageManager.GET_SIGNATURES
//                )
//
//                for (signature: Signature in packageInfo.signatures) {
//                    Log.d("TEST", "서명정보 : ${signature.toCharsString()}")
//
//                    try {
//                        val md = MessageDigest.getInstance("SHA-256")
//                        md.update(signature.toByteArray())
//                        val currentHash = toHex(md.digest())
//
//                        if (currentHash.equals(VALID_SIGNATURE_HASH, ignoreCase = true)) {
//                            Log.e("TEST", "무결성 검증 통과!")
////                    Toast.makeText(this, "무결성 검증 통과!", Toast.LENGTH_SHORT).show()
//                        } else {
//                            Log.e("TEST", "무결성 검증 실패")
//                            Toast.makeText(
//                                this,
//                                "앱 실행에 문제가 감지되었습니다. 안전한 사용을 위해 앱을 다시 설치해 주세요.",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    } catch (e: NoSuchAlgorithmException) {
//                        Log.e("TEST", "무결성 검증 실패: ${e.message}")
//                        Toast.makeText(
//                            this,
//                            "앱 실행에 문제가 감지되었습니다. 안전한 사용을 위해 앱을 다시 설치해 주세요.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        throw RuntimeException(e)
//                    }
//                }
//            } catch (e: PackageManager.NameNotFoundException) {
//                Log.e("TEST", "무결성 검증 실패: ${e.message}")
//                Toast.makeText(
//                    this,
//                    "앱 실행에 문제가 감지되었습니다. 안전한 사용을 위해 앱을 다시 설치해 주세요.",
//                    Toast.LENGTH_SHORT
//                ).show()
//                throw RuntimeException(e)
//            }
//        }

    }
    fun uploadWorkRequest() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 워커 작동 주기 설정하는 곳!
        val sendServerRequest = PeriodicWorkRequestBuilder<SendDataWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sendServer",
            ExistingPeriodicWorkPolicy.KEEP,
            sendServerRequest
        )

        Log.e(TEST, "===========WORK 등록===============")
    }

}

