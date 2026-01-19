package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleBridge
import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.NotiDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionInfoScreen(navController: NavController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName

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

    // 다이얼로그
    // 1. Dialog : 일일 혈당 입력
    if (showCaliDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showCaliDialog(false) },
            onConfirm = {
                BleBridge.showCaliDialog(false)
                navController.navigate("GlucoseRegisterScreen")
            },
            title = stringResource(R.string.dialog_daily_enter_glucose_title),
            content = stringResource(R.string.dialog_daily_enter_glucose_content)
        )
    }

    // 2. Dialog : BLE 끊김
    if (showBleConnectDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBleConnectDialog(false) },
            onConfirm = {
                BleBridge.showBleConnectDialog(false)
            },
            title = stringResource(R.string.dialog_ble_disconnected_title),
            content = stringResource(R.string.dialog_ble_disconnected_content)
        )
    }

    // 4. Dialog : 블루투스 ON
    if (showBluetoothOnDialog.value) {
        NotiDialog(
            onDismiss = { BleBridge.showBluetoothOnDialog(false) },
            onConfirm = {
                BleBridge.showBluetoothOnDialog(false)
            },
            title = stringResource(R.string.dialog_bluetooth_off_title),
            content = stringResource(R.string.dialog_bluetooth_off_content),
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
                            Toast.makeText(context, context.getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            title = stringResource(R.string.dialog_end_measurement_title),
            content = stringResource(R.string.dialog_end_measurement_content)
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
            title = stringResource(R.string.dialog_low_glucose_title),
            content = stringResource(R.string.dialog_low_glucose_content),
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
            title = stringResource(R.string.dialog_high_glucose_title),
            content = stringResource(R.string.dialog_high_glucose_content)
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
                        text = stringResource(R.string.settings_version_info),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF2F3F9))
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider()

                Spacer(modifier = Modifier.height(screenHeight * 0.15f)) // 10% 여백

                Image(
                    painter = painterResource(id = R.drawable.agms_version_icon),
                    contentDescription = "앱 아이콘",
                    modifier = Modifier.size(250.dp)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Text(
                    text = stringResource(R.string.version) +" ${versionName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.1f))

                Text(
                    text = "Copyright 2025 UXN Co., Ltd.\nAll Rights Reserved",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Text(
                    text = "Powered by AGMS",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } // Scaffold
}
