package kr.co.uxn.agms_p.ui.components.ready

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service.NOTIFICATION_SERVICE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestLinkDevice
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.room.AppDatabase

@Composable
fun StabilizationCompleteScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    var isNotiStabilization = false
    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    LaunchedEffect(Unit) {
        DataStoreManager.deleteRoute()
        DataStoreManager.saveRoute("StabilizationCompleteScreen")
        val route = DataStoreManager.getRoute().first()
        Log.e("TEST", "안정화 완료 화면에서 Route : ${route}")
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.size(280.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.stabilization_complete_title),
                fontSize = 30.sp,
//                fontWeight = FontWeight.Bold,
                color = Color(0xFF385DAB)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.stabilization_complete_sub_title),
                fontSize = 30.sp,
//                fontWeight = FontWeight.Bold,
                color = Color(0xFF385DAB)
            )


            Spacer(modifier = Modifier.height(260.dp))

            // 다음 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = if (isKorean)R.drawable.btn_next else R.drawable.btn_eng_continue),
                    contentDescription = "다음 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            navController.navigate("EnterFirstGlucose")

                            // 메인화면으로 고정 isMain = true
                            coroutine.launch(Dispatchers.IO) {
                                DataStoreManager.deleteRoute()
                                DataStoreManager.saveRoute("MainScreen/0")
                                DataStoreManager.saveIsMain(true)
                                val isMain = DataStoreManager.getIsMain().first()
                                val route = DataStoreManager.getRoute().first()
                                Log.e("TEST", "안정화 화면에서 버튼 눌럿을시isMain : ${isMain}")
                                Log.e("TEST", "안정화 화면에서 버튼 눌럿을시 Route : ${route}")

                                // 알림 혈당 설정
//                                val userId = DataStoreManager.getUserId().first() ?: -1
//                                try {
//                                    val detectorList = localDbRepository?.dataDao()?.getListAfterLastTime(userId, 0)
//                                    Log.d("StabilizationCompleteScreen, 알림혈당 기본값 설정", "detectorList is null or empty!")
//                                    if (detectorList.isNullOrEmpty()) {
//                                        DataStoreManager.setTargetLowGlucose(70)
//                                        DataStoreManager.setTargetHighGlucose(170)
//                                    } else {
//                                        Log.d("TEST", "detectorList is exist : ${detectorList}")
//                                    }
//
//                                } catch (e: Exception) {
//                                    Log.d("TEST","룸 DB에러 발생 : ${e.message}")
//                                }

                                withContext(Dispatchers.Main){
                                    NotificationManagerCompat.from(context).cancel(90)
                                }
                            }
                        }
                )
            }
        }
    }
}

//@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
//fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
//    val channelId = "stabilization_channel"
//
//    // Oreo 이상은 채널 필요
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        val channel = NotificationChannel(
//            channelId,
//            "stabilization alert",
//            NotificationManager.IMPORTANCE_HIGH
//        ).apply {
//            description = "Alerts for stabilization"
//        }
//
//        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.createNotificationChannel(channel)
//    }
//
//
//    val notification = NotificationCompat.Builder(context, channelId)
//        .setOngoing(true)
//        .setContentTitle(title)
//        .setContentText(message)
//        .setPriority(NotificationCompat.PRIORITY_HIGH)
//        .setSmallIcon(R.mipmap.ic_launcher_round)
//        .build()
//
//    NotificationManagerCompat.from(context).notify(notificationId, notification)
//}