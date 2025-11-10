package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleBridge
import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.AlwaysDialog
import kr.co.uxn.agms_p.ui.components.main.NotiDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.ZoneId
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorInfoScreen(navController: NavController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    val showDialog = remember { mutableStateOf(false) }
    val startTime = remember { mutableStateOf("") }
    val remainingTime = remember { mutableStateOf(-1) }

    var serialNumber = remember { mutableStateOf("")}

    // 다이얼로그 변수 모음
    var showCaliDialog = bleViewModel.showCaliDialog.collectAsState()
    var showBleConnectDialog = bleViewModel.showBleConnectDialog.collectAsState()
    var showBluetoothOnDialog = bleViewModel.showBluetoothOnDialog.collectAsState()
    var showLowGlucoseDialog = bleViewModel.showLowGlucoseDialog.collectAsState()
    var showHighGlucoseDialog = bleViewModel.showHighGlucoseDialog.collectAsState()
    var showEndMeasurementDialog = bleViewModel.showEndMeasurementDialog.collectAsState()


    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    LaunchedEffect(Unit) {
        val startTimeMilli = DataStoreManager.getStartTime().first() ?: -1
        val endTimeMilli = DataStoreManager.getEndTime().first() ?: -1
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN)
        val userId = DataStoreManager.getUserId().first() ?: -1

        val formattedDate = if (startTimeMilli != -1L) {
            Instant.ofEpochMilli(startTimeMilli)
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(formatter)
        } else {
            ""
        }
        // 센서 시작 시간 설정
        startTime.value = formattedDate

        val remainingDays = if (startTimeMilli != -1L && endTimeMilli != -1L) {
            val diffMillis = endTimeMilli - System.currentTimeMillis()
            (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        } else {
            -1 // 오류 처리
        }

        // 남은 사용 기간 설정
        remainingTime.value = remainingDays

        // 시리얼번호 가져오기
        withContext(Dispatchers.IO) {
            serialNumber.value = DataStoreManager.getSerialNumber().first() ?: ""
        }
    }

//    if (showDialog.value) {
//        AlwaysDialog(
//            onDismiss = {
//                showDialog.value = false
//            },
//            onConfirm = {
//                showDialog.value = false
//                coroutineScope.launch(Dispatchers.IO) {
//                    // 0. 토큰 정리
//                    val userId = DataStoreManager.getUserId().first() ?: -1
//                    try {
//                        val sensorOff = tokenRetrofit.doSensorOff(userId)
//                        if (sensorOff.isSuccessful) {
//                            val sensorOffBody = sensorOff.body()
//                            if (sensorOffBody != null) {
//                                Log.w("TEST", "sensorOff responseBody : ${sensorOffBody}")
//                                if (sensorOffBody.isSuccess) {
//                                    // userId의 db삭제
//                                    localDbRepository?.dataDao()?.deleteUserValueTable(userId)
//                                    localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
//                                    localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)
//
//                                    Log.w("TEST", "sensorOff 성공")
//                                    DataStoreManager.saveIsMain(false)
//                                    DataStoreManager.deleteRoute()
//                                    DataStoreManager.saveRoute("Splash")
//                                    Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
//                                    DataStoreManager.deleteAccessToken()
//                                    DataStoreManager.deleteRefreshToken()
//                                    DataStoreManager.deleteUserId()
//                                    DataStoreManager.deleteDeviceMac()
//                                    DataStoreManager.deleteStartTime()
//                                    DataStoreManager.deleteEndTime()
//                                    DataStoreManager.deleteDailyCalibrationTime()
//                                    DataStoreManager.setLandScapeMode(false)
//                                    DataStoreManager.deleteTargetLowGlucose()
//                                    DataStoreManager.deleteTargetHighGlucose()
//                                    DataStoreManager.deleteEmail()
//                                    withContext(Dispatchers.Main) {
//                                        // 1. 서비스 종료
//                                        bleViewModel.emit("STOP_SERVICE")
//                                        // 앱 강제 종료
//                                        android.os.Process.killProcess(android.os.Process.myPid())
//                                        exitProcess(0)
//                                    }
//                                } else {
//                                    Log.w("TEST", "sensorOff 실패")
//                                }
//                            }
//                        } else {
//                            Log.w("TEST", "sensorOff API통신 실패 : ${sensorOff.errorBody()?.string()}")
//                        }
//                    } catch (e: Exception) {
//                        Log.d("TEST", "sensorOff API통신 실패 : ${e.message}")
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "네트워크를 확인해 주세요.",Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            },
//            title = "센서 종료",
//            content = "센서 연결을 종료하시겠습니까?"
//        )
//    }

    // 다이얼로그
    // 1. Dialog : 일일 혈당 입력
    if (showCaliDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showCaliDialog(false) },
            onConfirm = {
                BleBridge.showCaliDialog(false)
                navController.navigate("GlucoseRegisterScreen")
            },
            title = "혈당 입력 시간입니다.",
            content = "정확한 측정을 위해 공복 상태에서 자가 채혈한 혈당값을 입력해 주세요.",
        )
    }

    // 2. Dialog : BLE 끊김
    if (showBleConnectDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
            },
            title = "블루투스 연결이 끊어졌습니다.",
            content = "센서와의 연결이 일시적으로 끊어졌어요.\n스마트폰을 가까이 두고 연결 상태를 확인하세요.",
        )
    }

    // 4. Dialog : 블루투스 ON
    if (showBluetoothOnDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBluetoothOnDialog(false) },
            onConfirm = {
                BleBridge.showBluetoothOnDialog(false)
            },
            title = "블루투스가 꺼져있습니다.",
            content = "블루투스를 켜고 연결 상태를 확인하세요.",
        )
    }

    // 5. Dialog : 측정 종료
    if (showEndMeasurementDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
                coroutineScope.launch(Dispatchers.IO) {
                    val userId = DataStoreManager.getUserId().first() ?: -1
                    try {
                        val sensorOff = tokenRetrofit.doSensorOff(userId)
                        if (sensorOff.isSuccessful) {
                            val sensorOffBody = sensorOff.body()
                            if (sensorOffBody != null) {
                                Log.w("TEST", "sensorOff responseBody : ${sensorOffBody}")
                                if (sensorOffBody.isSuccess) {
                                    // userId의 db삭제
                                    localDbRepository?.dataDao()?.deleteUserValueTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
                                    localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)

                                    Log.w("TEST", "sensorOff 성공")
                                    DataStoreManager.saveIsMain(false)
                                    DataStoreManager.deleteRoute()
                                    DataStoreManager.saveRoute("Splash")
                                    Log.d("TEST", "${DataStoreManager.getIsMain().first()}")
                                    DataStoreManager.deleteAccessToken()
                                    DataStoreManager.deleteRefreshToken()
                                    DataStoreManager.deleteUserId()
                                    DataStoreManager.deleteDeviceMac()
                                    DataStoreManager.deleteStartTime()
                                    DataStoreManager.deleteEndTime()
                                    DataStoreManager.deleteDailyCalibrationTime()
                                    DataStoreManager.deleteDailyCalibrationLastTime()
                                    DataStoreManager.setLandScapeMode(false)
                                    DataStoreManager.deleteTargetLowGlucose()
                                    DataStoreManager.deleteTargetHighGlucose()
                                    DataStoreManager.deleteEmail()
                                    withContext(Dispatchers.Main) {
                                        // 1. 서비스 종료
                                        bleViewModel.emit("STOP_SERVICE")
                                        // 앱 강제 종료
                                        android.os.Process.killProcess(android.os.Process.myPid())
                                        exitProcess(0)
                                    }
                                } else {
                                    Log.w("TEST", "sensorOff 실패")
                                }
                            }
                        } else {
                            Log.w("TEST", "sensorOff API통신 실패 : ${sensorOff.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.d("TEST", "sensorOff API통신 실패 : ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "네트워크를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            title = "센서의 사용 기간이 종료되었습니다.",
            content = "센서의 사용 기간이 만료되어 더 이상 측정이 불가합니다. 새 센서를 연결해 주세요.",
        )
    }

    // 6.1 Dialog : 저혈당
    if (showLowGlucoseDialog.value) {
        NotiDialog(
            onDismiss = { showLowGlucoseDialog(false) },
            onConfirm = {
                showLowGlucoseDialog(false)
                NotificationManagerCompat.from(context).cancel(95)
            },
            title = "혈당수치가 낮습니다.",
            content = "저혈당 위험이 있어요. 필요시 조치를 취하고, 안정 후 수치를 다시 확인하세요.",
        )
    }

    // 6.2 Dialog : 고혈당
    if (showHighGlucoseDialog.value) {
        NotiDialog(
            onDismiss = { showHighGlucoseDialog(false) },
            onConfirm = {
                showHighGlucoseDialog(false)
                NotificationManagerCompat.from(context).cancel(96)
            },
            title = "혈당수치가 높습니다.",
            content = "현재 혈당이 혈당 범위를 초과했어요. 식사나 활동 내용을 확인하세요.",
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            modifier = Modifier.clickable {
                                navController.navigate("MainScreen/${2}") {
                                    popUpTo("MainScreen/{startIndex}") { inclusive = true }
                                }
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "센서 정보",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF2F3F9)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .height(50.dp)
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "센서 시작일",
                        fontSize = 16.sp
                    )
                    Text(
                        text = startTime.value,
                        fontSize = 16.sp
                    )
                }

                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .height(50.dp)
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "남은 사용 기간",
                        fontSize = 16.sp
                    )
                    if (remainingTime.value > 0) {
                        Text(
                            text = "${remainingTime.value}일",
                            fontSize = 16.sp
                        )
                    } else if (remainingTime.value == 0) {
                        Text(
                            text = "오늘이에요",
                            fontSize = 16.sp
                        )
                    } else {
                        Text(
                            text = "기간이 지났어요",
                            fontSize = 16.sp
                        )
                    }
                }

                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .height(50.dp)
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "시리얼 번호",
                        fontSize = 16.sp
                    )

                    Text(
                        text = serialNumber.value,
                        fontSize = 16.sp
                    )
                }


                Divider()

//                Box(
//                    modifier = Modifier.clickable {
//                        showDialog.value = true
//                    }
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(Color.White)
//                            .height(50.dp)
//                            .padding(horizontal = 30.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = "센서 종료",
//                            fontSize = 16.sp
//                        )
//                    }
//                }
//                Divider()
            } // Column
        } // Surface
    } // Scaffold
}
