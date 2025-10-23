package kr.co.uxn.agms_p.ble

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.api.token.DataStoreManager


class AlwaysBroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("ObsoleteSdkInt")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.action != null) {
            if (shouldStartService(intent)) {
                Log.d("BROADCASTRECEIVER", "인텐트는 : ${intent.action!!}")
                if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                    val uptime = android.os.SystemClock.elapsedRealtime()
                    // 부팅 5분 이내 서비스 실행
                    if (uptime < 300_000L) {
                        Log.d("TEST", "uptime : $uptime")
                        startService(context)
                    }
                } else {

                    startService(context)
                }
            }
        }
    }


    private fun shouldStartService(intent: Intent): Boolean {
        val action = intent.action
         return TextUtils.equals(action, Intent.ACTION_BOOT_COMPLETED)
    }

    /**
     * TextUtils.equals(action, Intent.ACTION_PACKAGE_RESTARTED)
     * TextUtils.equals(action, Intent.ACTION_MY_PACKAGE_REPLACED)
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun startService(context: Context) {
        val serviceIntent = Intent(context, AlwaysService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.e("BROADCASTRECEIVER", "브로드캐스트에서 startForegroundService 호출 완료")
    }
}