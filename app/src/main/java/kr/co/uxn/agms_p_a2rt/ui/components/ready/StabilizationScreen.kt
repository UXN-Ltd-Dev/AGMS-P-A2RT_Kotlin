package kr.co.uxn.agms_p_a2rt.ui.components.ready

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.notification.AlertChannel
import kr.co.uxn.agms_p_a2rt.notification.AppNotificationManager
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.BleViewModel

@Composable
fun StabilizationScreen(navController: NavController, mac: String, bleViewModel: BleViewModel) {
    // 로티 애니메이션
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.reading_guy_lottie))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 0.8f
    )

    val context = LocalContext.current
    var isNotiStabilization = false

    /**
     * 센서 안정화 시간 설정 변수
     * totalTime
     */
//    val totalTime = 60 * 60 * 1000L // 60분
    val totalTime = 1 * 60 * 1000L // 테스트용 1분
//    val totalTime = 15 * 1000L // 시연용 15초

    val remainingTime = remember { mutableStateOf(totalTime) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutine = rememberCoroutineScope()

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    LaunchedEffect(Unit) {
        // 첫 실행 보장 플래그 불러오기
        val firstActivate = DataStoreManager.getFirstActivate().first() ?: false
        Log.d("TEST", "firstActivate 1 : ${firstActivate}")
        delay(1000)

        // 라우트 설정
        DataStoreManager.deleteRoute()
        DataStoreManager.saveRoute("StabilizationScreen/$mac")
        val route = DataStoreManager.getRoute().first()
        Log.e("TEST", "안정화 화면에서 Route : ${route}")

        if (!firstActivate) {

            Log.d("TEST", "firstActivate 2 : ${firstActivate}")
            DataStoreManager.setFirstActivateDB(true)
            val firstActivate2 = DataStoreManager.getFirstActivate().first() ?: false
            Log.d("TEST", "firstActivate 3 : ${firstActivate2}")

            // 서비스 실행 이벤트 발행
            if (mac != "999999") {
                DataStoreManager.saveIsSensorEnded(false)
                bleViewModel.emit("START_SERVICE")
                Log.e("StabilizationScreen", "START_SERVICE EMIT!")
            }

            Log.e(
                "TEST",
                "ble뷰모델로부터 갖고 온 device 테스트 : deviceMac :${bleViewModel.device.value.deviceMac}\ndevice객체 : ${bleViewModel.device.value.device}"
            )
        }


        val countDownTimer = object : CountDownTimer(remainingTime.value, 1000 * 60 * 1) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                // 타이머가 끝나면 다음 화면으로 이동
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {

                    navController.navigate("StabilizationCompleteScreen") {
                        popUpTo(0) { inclusive = true } // 백스택 전체 제거
                        launchSingleTop = true
                    }
                } else {
                    Log.d("NAVIGATION", "Navigation skipped - lifecycle not ready")
                }

                coroutine.launch(Dispatchers.IO) {
                    isNotiStabilization = DataStoreManager.getNotiStabilization().first() ?: true
                    Log.d("TEST", "안정화 화면에서 isNotiStabilization : ${isNotiStabilization}")

                    delay(500)

                    withContext(Dispatchers.Main) {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {

                            if (isNotiStabilization) {
                                sendNotification(context, context.getString(R.string.notification_sensor_ready), "", 90)
                            }
                        }
                    }
                }
                navController.navigate("StabilizationCompleteScreen") {
                    popUpTo(0) { inclusive = true } // 백스택 전체 제거
                    launchSingleTop = true
                }
            }
        }
        countDownTimer.start()
    }

    LaunchedEffect(Unit) {
        val userId = DataStoreManager.getUserId().first() ?: -1
        try {
            val detectorList = localDbRepository?.dataDao()?.getListAfterLastTime(userId, 0)
            if (detectorList.isNullOrEmpty()) {
                Log.d("StabilizationScreen, 시작,측정,종료 시간 설정", "detectorList is null or empty!")

                // 1. 시작 시간 설정
                val startTime = System.currentTimeMillis()

                DataStoreManager.saveIsSensorEnded(false)
                DataStoreManager.deleteStabilizationCompleted()
                DataStoreManager.deleteStartTime()
                DataStoreManager.saveStartTime(startTime)

                // check
                val startTimeFromDS = DataStoreManager.getStartTime().first() ?: 0
                Log.d("TEST", "startTimeFromDS : $startTimeFromDS")

                // 2. 측정 기간 설정
//                val measurementTime: Long = 1000 * 60 * 60 * 24 * 10 // 측정 기간 10일
                val measurementTime: Long = 1000 * 60 * 60 * 24 * 14 // 측정 기간 14일
//                val measurementTime: Long = 1000 * 60 * 60 * 24 * 20 // 측정 기간 20일
//                val measurementTime: Long = 1000 * 60 * 60 * 24 * 15 // 측정 기간 15일
//                val measurementTime: Long = 1000 * 60 * 60 * 24 * 3// 측정 기간 3일

                DataStoreManager.deleteMeasurementTime()
                DataStoreManager.saveMeasurementTime(measurementTime)

                // check
                val measurementTimeFromDS = DataStoreManager.getMeasurementTime().first() ?: 0
                Log.d("TEST", "measurementTimeForDs : ${measurementTimeFromDS}")

                // 3. 종료 시간 설정
                val endTime = startTime + measurementTime

                DataStoreManager.deleteEndTime()
                DataStoreManager.saveEndTime(endTime)

                // check
                val endTimeFromDS = DataStoreManager.getEndTime().first() ?: 0
                Log.d("TEST", "endTimeFromDS : $endTimeFromDS")


            } else {
                Log.d("TEST", "detectorList is exist : ${detectorList}")
            }
        } catch (e: Exception) {
            Log.d("TEST", "에러 : ${e.message}")
        }

    }


    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = colorResource(R.color.background_white)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.stabilization_content),
                fontSize =27.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.main)
            )


            Spacer(modifier = Modifier.weight(0.1f))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.stabilization_title),
//                fontWeight = FontWeight.Bold,
                fontSize = 27.sp
            )

            Spacer(modifier = Modifier.weight(0.1f))


            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                val minutes = (remainingTime.value / 1000) / 60 + 1
                if (minutes > 0) {
                    Text(
//                    modifier = Modifier.fillMaxWidth(),
//                        textAlign = TextAlign.Center,
                        text = minutes.toString() + stringResource(R.string.minute),
                        fontSize = 70.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.main)
                    )
                }
            }


            Spacer(modifier = Modifier.weight(0.2f))

            Text(
                text = stringResource(R.string.stabilization_sub_title),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.weight(0.1f))

//            if (isKorean) {
//                Image(
//                    painter = painterResource(id = R.drawable.one_hundre_twenty_rt), // 120분
//                    painter = painterResource(id = R.drawable.sixty_min_rt), // 60분
//                    contentDescription = "120분 소요",
//                    modifier = Modifier.size(width = 90.dp, 30.dp)
//                )
//            } else {
//                Image(
//                    painter = painterResource(id = R.drawable.test),
//                    contentDescription = "120분 소요",
//                    modifier = Modifier.size(width = 130.dp, 30.dp)
//                )
//            }

            Spacer(modifier = Modifier.weight(0.2f))

            // 로티 애니메이션
            Box(
                modifier = Modifier.size(100.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
suspend fun sendNotification(context: Context, title: String, message: String, notificationId: Int) {
    AppNotificationManager.notify(
        context = context,
        channel = AlertChannel.STABILIZATION,
        title = title,
        message = message,
        notificationId = notificationId,
        autoCancel = true
    )
}
