package kr.co.uxn.agms_p_a2rt.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.NetworkUtil
import kr.co.uxn.agms_p_a2rt.ble.BleBridge
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.ui.components.main.NotiDialog
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.BleViewModel
import kotlin.system.exitProcess
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestDeleteOauthUserInfo
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestDeleteUserInfo
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p_a2rt.ble.BleBridge.showLowGlucoseDialog
import kr.co.uxn.agms_p_a2rt.ui.components.main.AlwaysDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(navController: NavController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current
    var isCheck by remember { mutableStateOf<Boolean>(false) }
    val showDialog = remember { mutableStateOf(false) }

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

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

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
            content = stringResource(R.string.dialog_end_measurement_content),
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


    if (showDialog.value) {
        AlwaysDialog(
            onConfirm = {
                showDialog.value = false

                coroutineScope.launch(Dispatchers.IO) {
                    val type = DataStoreManager.getType().first() ?: -1
                    val userId = DataStoreManager.getUserId().first() ?: -1

                    if (type != -1) {
                        Log.e("TEST", "type : $type")
                        if (type == 1803) {
                            try {
                                val deleteUser = tokenRetrofit.deleteUser(
                                    RequestDeleteUserInfo(
                                        userId
                                    )
                                )
                                val deleteUserBody = deleteUser.body()
                                Log.e("TEST", "deleteUserBody : $deleteUserBody")
                                if (deleteUser.isSuccessful) {
                                    if (deleteUserBody != null) {
                                        if (deleteUserBody.isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, context.getString(R.string.toast_delete_account), Toast.LENGTH_SHORT).show()
                                            }

                                            // 토큰정리 및 앱 종료

                                            localDbRepository?.dataDao()?.deleteUserValueTable(userId)
                                            localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
                                            localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)

                                            Log.w("TEST", "sensorOff 성공")
                                            DataStoreManager.saveIsMain(false)
                                            DataStoreManager.deleteRoute()
                                            DataStoreManager.saveRoute("Splash")
                                            Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                                            Log.e("TEST", "DS에 저장된 Route : ${DataStoreManager.getRoute().first()}")
                                            DataStoreManager.deleteAccessToken()
                                            DataStoreManager.deleteRefreshToken()
                                            DataStoreManager.deleteUserId()
                                            DataStoreManager.deleteDeviceMac()
                                            DataStoreManager.deleteStartTime()
                                            DataStoreManager.deleteEndTime()
                                            withContext(Dispatchers.Main) {
                                                // 1. 서비스 종료
//                                                bleViewModel.emit("STOP_SERVICE")
                                                // 앱 강제종료
                                                android.os.Process.killProcess(android.os.Process.myPid())
                                                exitProcess(0)
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("TEST", "API 에러 : ${deleteUser.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                Log.e("TEST", "네트워크 에러 : $e")

                            }
                        } else {
                            try {
                                val deleteOauthUser = tokenRetrofit.deleteOauthUser(
                                    RequestDeleteOauthUserInfo(userId, type)
                                )
                                val deleteOauthUserBody = deleteOauthUser.body()
                                Log.e("TEST", "deleteOauthUserBody : $deleteOauthUserBody")
                                if (deleteOauthUser.isSuccessful) {
                                    if (deleteOauthUserBody != null) {
                                        if (deleteOauthUserBody.isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, context.getString(R.string.toast_delete_account), Toast.LENGTH_SHORT).show()
                                            }

                                            // 토큰 정리 및 앱 종료

                                            localDbRepository?.dataDao()?.deleteUserValueTable(userId)
                                            localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
                                            localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)

                                            Log.w("TEST", "sensorOff 성공")
                                            DataStoreManager.saveIsMain(false)
                                            DataStoreManager.deleteRoute()
                                            DataStoreManager.saveRoute("Splash")
                                            Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
                                            Log.e("TEST", "DS에 저장된 Route : ${DataStoreManager.getRoute().first()}")
                                            DataStoreManager.deleteAccessToken()
                                            DataStoreManager.deleteRefreshToken()
                                            DataStoreManager.deleteUserId()
                                            DataStoreManager.deleteDeviceMac()
                                            DataStoreManager.deleteStartTime()
                                            DataStoreManager.deleteEndTime()
                                            withContext(Dispatchers.Main) {
                                                // 1. 서비스 종료
//                                                bleViewModel.emit("STOP_SERVICE")
                                                // 앱 강제종료
                                                android.os.Process.killProcess(android.os.Process.myPid())
                                                exitProcess(0)
                                            }
                                        }
                                    }
                                } else {
                                    Log.e("TEST", "API 에러 : ${deleteOauthUser.errorBody()?.string()}")
                                }
                            } catch (e: Exception) {
                                Log.e("TEST", "네트워크 에러 : $e")
                            }
                        }
                    }
                }
            },
            onDismiss = {
                showDialog.value = false
            },
            title = context.getString(R.string.dialog_delete_account),
            content = context.getString(R.string.dialog_delete_ask_again)
        )
    }



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize()
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp)
                        .padding(top = 20.dp),
                ) {
                    Text(
                        text = stringResource(R.string.delete_account_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.delete_account_sub_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Image(
                            modifier = Modifier.size(35.dp),
                            painter = painterResource(R.drawable.link_icon),
                            contentDescription = "링크 아이콘"
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column(

                        ){
                            Text(
                                text = stringResource(R.string.delete_account_content_1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Image(
                            modifier = Modifier.size(27.dp),
                            painter = painterResource(R.drawable.device_icon),
                            contentDescription = "장치 아이콘"
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Column(

                        ){
                            Text(
                                text = stringResource(R.string.delete_account_content_2)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = stringResource(R.string.delete_account_content_3)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = stringResource(R.string.delete_account_content_4)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = stringResource(R.string.delete_account_alert_1),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.delete_account_alert_2),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))


                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = (isCheck == true),
                                onClick = { isCheck = !isCheck }, // 눌렀을 때만 선택
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (isCheck == true),
                            onClick = null, // 접근성 위해 null
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(R.color.main)
                            )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.delete_check_description),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Image(
                            painter = painterResource(id = if (isKorean) R.drawable.btn_delete_account else R.drawable.btn_eng_delete),
                            contentDescription = "계정 삭제 버튼",
                            modifier = Modifier.align(Alignment.Center)
                                .clickable {
                                    if (!isCheck) {
                                        Toast.makeText(context, context.getString(R.string.toast_check_delete), Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (NetworkUtil.isNetworkAvailable(context)) {
                                            showDialog.value = true

                                        } else {
                                            Toast.makeText(context, context.getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Image(
                            painter = painterResource(id = if (isKorean) R.drawable.btn_cancel else R.drawable.btn_eng_cancel),
                            contentDescription = "취소 버튼",
                            modifier = Modifier.align(Alignment.Center)
                                .clickable {
                                    navController.popBackStack()
                                }
                        )
                    }
                }
            }
        }
    }
}
