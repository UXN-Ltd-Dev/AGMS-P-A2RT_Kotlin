package kr.co.uxn.agms_p.ui.components.main.setting

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.NetworkUtil
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestDeleteOauthUserInfo
import kr.co.uxn.agms_p.api.model.requestDTO.RequestDeleteUserInfo
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ble.BleBridge
import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.AlwaysDialog
import kr.co.uxn.agms_p.ui.components.main.NotiDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import kotlin.system.exitProcess

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
                                val deleteUser = tokenRetrofit.deleteUser(RequestDeleteUserInfo(userId))
                                val deleteUserBody = deleteUser.body()
                                Log.e("TEST", "deleteUserBody : $deleteUserBody")
                                if (deleteUser.isSuccessful) {
                                    if (deleteUserBody != null) {
                                        if (deleteUserBody.isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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
                                val deleteOauthUser = tokenRetrofit.deleteOauthUser(RequestDeleteOauthUserInfo(userId, type))
                                val deleteOauthUserBody = deleteOauthUser.body()
                                Log.e("TEST", "deleteOauthUserBody : $deleteOauthUserBody")
                                if (deleteOauthUser.isSuccessful) {
                                    if (deleteOauthUserBody != null) {
                                        if (deleteOauthUserBody.isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
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
            title = "계정 삭제",
            content = "정말 삭제하시겠습니까?"
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
                        text = "계정 삭제",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
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
                        text = "계정을 삭제하시겠습니까?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "계정을 삭제할 경우",
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
                                text = "동일한 이메일 주소를 사용하는 관련 Always"
                            )
                            Text(
                                text = "앱 계정의 모든 데이터를 잃게 됩니다."
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
                                text = "현재 사용하고 계신 Always 제품의 연결이 끊"
                            )
                            Text(
                                text = "기게 됩니다."
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "계정을 삭제하면 이 앱에서 로그아웃됩니다."
                    )
                    Text(
                        text = "세션이 만료되기까지 최대 1시간이 소요 될 수 있습니다."
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "계정 삭제 후 다시 돌아오셔서 새로운 계정을 만들 수"
                    )
                    Text(
                        text = "있습니다."
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "계정을 삭제하시겠습니까?",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(실행 취소는 불가능 합니다.)",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(30.dp))


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
                            onClick = null // 접근성 위해 null
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "예, 계정을 삭제하겠습니다.",
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
                            painter = painterResource(R.drawable.btn_delete_account),
                            contentDescription = "계정 삭제 버튼",
                            modifier = Modifier.align(Alignment.Center)
                                .clickable {
                                    if (!isCheck) {
                                        Toast.makeText(context, "삭제를 체크해 주세요.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (NetworkUtil.isNetworkAvailable(context)) {
                                            showDialog.value = true

                                        } else {
                                            Toast.makeText(context, "네트워크를 확인해 주세요.", Toast.LENGTH_SHORT).show()
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
                            painter = painterResource(R.drawable.btn_cancel),
                            contentDescription = "취소 버튼",
                            modifier = Modifier.align(Alignment.Center)
                                .clickable {
                                    navController.navigate("MainScreen/${2}")
                                }
                        )
                    }
                }
            }
        }
    }
}

