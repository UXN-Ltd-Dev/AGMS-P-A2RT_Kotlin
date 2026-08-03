package kr.co.uxn.agms_p_a2rt.ble

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager


class AlwaysA2RTBroadcastReceiver : BroadcastReceiver() {
    @SuppressLint("ObsoleteSdkInt")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null && intent.action != null) {
            if (shouldStartService(intent)) {
                Log.d("BROADCASTRECEIVER", "인텐트는 : ${intent.action!!}")

                if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                    val pendingResult: PendingResult = goAsync()

                    CoroutineScope(Dispatchers.IO).launch {
                        var deviceMac: String? = null
                        try {
                            deviceMac = DataStoreManager.getDeviceMac().firstOrNull()
                        } finally {
                            withContext(Dispatchers.Main) {
                                if (!deviceMac.isNullOrEmpty()) {
                                    startService(context)
                                }
                                pendingResult.finish()
                            }
                        }
                    }
                } else {
                    // BOOT_COMPLETED가 아닌 다른 액션은 바로 서비스 시작
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
        val serviceIntent = Intent(context, AlwaysA2RTService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        Log.e("BROADCASTRECEIVER", "브로드캐스트에서 startForegroundService 호출 완료")
    }
}