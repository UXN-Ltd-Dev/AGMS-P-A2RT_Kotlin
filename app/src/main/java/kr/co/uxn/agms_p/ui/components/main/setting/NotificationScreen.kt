package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
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
import kr.co.uxn.agms_p.ui.components.main.CustomTimePicker
import kr.co.uxn.agms_p.ui.components.main.NotiDialog
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val time = remember { mutableStateOf<String>("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    // notification swtich
    val checkedForHighGlucose = remember { mutableStateOf(false) }
    val checkedForLowGlucose = remember { mutableStateOf(false) }
    val checkedForLostSignal = remember { mutableStateOf(false) }
    val checkedForExpiredSensor = remember { mutableStateOf(false) }
    val checkedForStabilization = remember { mutableStateOf(false) }
    val checkedForCalibration = remember { mutableStateOf(false) }

    val showGlucoseDialog = remember { mutableStateOf(false) }
    val showSetLowGlucoseDialog = remember { mutableStateOf(false) }
    val showSetHighGlucoseDialog = remember { mutableStateOf(false) }
    val targetLowGlucose = remember { mutableStateOf("") }
    val targetHighGlucose = remember { mutableStateOf("") }

    val showSetDailyCalibrationDialog = remember { mutableStateOf(false) }

    var dailyCalibrationTime = remember { mutableStateOf("") }
    var hour = remember { mutableStateOf("") }
    var minute = remember { mutableStateOf("") }
    var isAfternoon = remember { mutableStateOf(false) }

    val formatter = remember { SimpleDateFormat("a hh:mm", Locale.KOREAN) }

    // 다이얼로그 변수 모음
    var showCaliDialog = bleViewModel.showCaliDialog.collectAsState()
    var showBleConnectDialog = bleViewModel.showBleConnectDialog.collectAsState()
    var showBluetoothOnDialog = bleViewModel.showBluetoothOnDialog.collectAsState()
    var showLowGlucoseDialog = bleViewModel.showLowGlucoseDialog.collectAsState()
    var showHighGlucoseDialog = bleViewModel.showHighGlucoseDialog.collectAsState()
    var showEndMeasurementDialog = bleViewModel.showEndMeasurementDialog.collectAsState()


    val currentLanguage = androidx.compose.ui.text.intl.Locale.current.language
    val isKorean = currentLanguage == "ko"

    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

//    var calHour: Int? = null
//    var calMinute: Int? = null

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // DS로부터 값 불러오기
            val verifiedDSHigh = DataStoreManager.getNotiHighGlucose().first() ?: false
            val verifiedDSLow = DataStoreManager.getNotiLowGlucose().first() ?: false
            val verifiedDSLostSignal = DataStoreManager.getNotiLostSignal().first() ?: true
            val verifiedDSExpiredSensor = DataStoreManager.getNotiExpiredSensor().first() ?: true
            val verifiedDSStabilization = DataStoreManager.getNotiStabilization().first() ?: true
            val verifiedDSCalibration = DataStoreManager.getNotiCalibration().first() ?: true
            val verifiedDSTargetLowGlucose = DataStoreManager.getTargetLowGlucose().first() ?: 70
            val verifiedDSTargetHighGlucose = DataStoreManager.getTargetHighGlucose().first() ?: 170
            val verifiedDSDailyCalibrationTime = DataStoreManager.getDailyCalibrationTime().first() ?: "오전 11:00"

            // 화면에 값 설정
            checkedForHighGlucose.value = verifiedDSHigh
            checkedForLowGlucose.value = verifiedDSLow
            checkedForLostSignal.value = verifiedDSLostSignal
            checkedForExpiredSensor.value = verifiedDSExpiredSensor
            checkedForStabilization.value = verifiedDSStabilization
            checkedForCalibration.value = verifiedDSCalibration
            targetLowGlucose.value = verifiedDSTargetLowGlucose.toString()
            targetHighGlucose.value = verifiedDSTargetHighGlucose.toString()
            dailyCalibrationTime.value = verifiedDSDailyCalibrationTime

            // 로그 띄우기
//            Log.e("NOTI", "After High : ${verifiedDSHigh}, Low : ${verifiedDSLow} " +
//                    "\n Lost : ${verifiedDSLostSignal} ExpiredSensor : ${verifiedDSExpiredSensor}" +
//                    "\n Stabilization : ${verifiedDSStabilization} Calibration : ${verifiedDSCalibration}" +
//                    "\n Target High Glucose : ${verifiedDSTargetHighGlucose} Target Low Glucose : ${verifiedDSTargetLowGlucose}")
        }
    }

    if (showSetDailyCalibrationDialog.value) {
        Dialog(
            onDismissRequest = { showSetDailyCalibrationDialog.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            androidx.compose.material3.Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .height(IntrinsicSize.Min)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.background
                    ),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        text = stringResource(R.string.glucose_reminder_glucose_entry_time),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium
                    )

//                    WheelTimePicker(
//                        timeFormat = TimeFormat.AM_PM,
//                        size = DpSize(200.dp, 100.dp),
//                        textStyle =
//                            TextStyle(
//                                color = Color.Black,
//                                fontSize = 25.sp,
//                                fontWeight = FontWeight.Medium
//                            ),
//                        selectorProperties = WheelPickerDefaults.selectorProperties(
//                            enabled = true,
//                            shape = RoundedCornerShape(0.dp),
//                            color = Color(0xFF8ACBF1).copy(alpha = 0.2f),
//                            border = BorderStroke(2.dp, Color(0xFFf1faee))
//                        )
//                    ) { snappedDateTime ->
//                        calHour = snappedDateTime.hour
//                        calMinute = snappedDateTime.minute
//
//                        Log.d("TIME" , "calHour : ${calHour}, snappedDateTime.hour : ${snappedDateTime.hour} \ncalMinute : ${calMinute}, snappedDateTime.minute : ${snappedDateTime.minute}")
//                    }

                    CustomTimePicker(hour = hour, minute = minute, isAfternoon = isAfternoon)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                    ) {
                        OutlinedButton(
                            onClick = { showSetDailyCalibrationDialog.value = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color(0xFFD8D8D8)),
                        ) {
                            androidx.compose.material3.Text(
                                text = stringResource(R.string.glucose_reminder_cancel),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = {
                                if (hour.value != "" && minute.value != "") {
                                    val cal = Calendar.getInstance().apply {
                                        val hourInt = hour.value.toInt()
                                        val hour24 = when {
                                            isAfternoon.value && hourInt != 12 -> hourInt + 12  // 오후 1-11시
                                            !isAfternoon.value && hourInt == 12 -> 0           // 오전 12시 (자정)
                                            else -> hourInt                                     // 오전 1-11시, 오후 12시
                                        }
                                        set(Calendar.HOUR_OF_DAY, hour24)
                                        set(Calendar.MINUTE, minute.value.toInt())
                                    }
                                    val formattedTime = formatter.format(cal.time)
                                    Log.d("TIME", "입력된 시간: $formattedTime")
                                    dailyCalibrationTime.value = formattedTime
                                    showSetDailyCalibrationDialog.value = false

                                    coroutineScope.launch(Dispatchers.IO) {
                                        DataStoreManager.setDailyCalibrationTime(dailyCalibrationTime.value)
                                    }
                                    Log.d("TIME", "finalTime : $dailyCalibrationTime")
                                } else {
                                    Toast.makeText(context, context.getString(R.string.toast_req_time), Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3451B2), // 파란색
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Text(
                                text = stringResource(R.string.glucose_reminder_enter),
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                }
            }
        }

    }


    // 저혈당, 고혈당 한번에 입력하는 다이얼로그 현재 사용안함.
    if (showGlucoseDialog.value) {
        Dialog(onDismissRequest = { showGlucoseDialog.value = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.low_glucose),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    OutlinedTextField(
                        value = targetLowGlucose.value,
                        onValueChange = { targetLowGlucose.value = it},
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(15 .dp))

                    Text(
                        text = stringResource(R.string.high_glucose),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    OutlinedTextField(
                        value = targetHighGlucose.value,
                        onValueChange = { targetHighGlucose.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (targetHighGlucose.value.contains(".") || targetHighGlucose.value.contains("-") || targetHighGlucose.value.contains(",")
                                    || targetLowGlucose.value.contains(".") || targetLowGlucose.value.contains("-") || targetLowGlucose.value.contains(",")
                                ) {
                                    Toast.makeText(context, context.getString(R.string.toast_enter_only_number), Toast.LENGTH_SHORT).show()
                                } else if (targetHighGlucose.value != "" && targetLowGlucose.value != "") {
                                    showGlucoseDialog.value = false
                                    coroutineScope.launch(Dispatchers.Main) {
                                        DataStoreManager.setTargetHighGlucose(targetHighGlucose.value.toInt())
                                        DataStoreManager.setTargetLowGlucose(targetLowGlucose.value.toInt())
                                        Log.d("TEST", "저장된 고혈당 : ${DataStoreManager.getTargetHighGlucose().first()}")
                                        Log.d("TEST", "저장된 저혈당 : ${DataStoreManager.getTargetLowGlucose().first()}")
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.toast_enter_glucose), Toast.LENGTH_SHORT).show()
                                }},
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3451B2), // 파란색
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.glucose_reminder_enter),
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSetLowGlucoseDialog.value) {
        Dialog(onDismissRequest = { showSetLowGlucoseDialog.value = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.low_glucose),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    OutlinedTextField(
                        value = targetLowGlucose.value,
                        onValueChange = { targetLowGlucose.value = it},
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(15 .dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (targetLowGlucose.value.contains(".") || targetLowGlucose.value.contains("-") || targetLowGlucose.value.contains(",")
                                ) {
                                    Toast.makeText(context, context.getString(R.string.toast_enter_only_number), Toast.LENGTH_SHORT).show()
                                } else if (targetLowGlucose.value != "") {
                                    showSetLowGlucoseDialog.value = false
                                    coroutineScope.launch(Dispatchers.Main) {
                                        DataStoreManager.setTargetLowGlucose(targetLowGlucose.value.toInt())
                                        Log.d("TEST", "저장된 저혈당 : ${DataStoreManager.getTargetLowGlucose().first()}")
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.toast_enter_glucose), Toast.LENGTH_SHORT).show()
                                }},
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3451B2), // 파란색
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.glucose_reminder_enter),
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSetHighGlucoseDialog.value) {
        Dialog(onDismissRequest = { showSetHighGlucoseDialog.value = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.high_glucose),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    OutlinedTextField(
                        value = targetHighGlucose.value,
                        onValueChange = { targetHighGlucose.value = it},
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    Spacer(modifier = Modifier.height(15 .dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (targetHighGlucose.value.contains(".") || targetHighGlucose.value.contains("-") || targetHighGlucose.value.contains(",")
                                ) {
                                    Toast.makeText(context, context.getString(R.string.toast_enter_only_number), Toast.LENGTH_SHORT).show()
                                } else if (targetHighGlucose.value != "") {
                                    showSetHighGlucoseDialog.value = false
                                    coroutineScope.launch(Dispatchers.Main) {
                                        DataStoreManager.setTargetHighGlucose(targetHighGlucose.value.toInt())
                                        Log.d("TEST", "저장된 고혈당 : ${DataStoreManager.getTargetHighGlucose().first()}")
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.toast_enter_glucose), Toast.LENGTH_SHORT).show()
                                }},
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3451B2), // 파란색
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.glucose_reminder_enter),
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
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



    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            },
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
                        text = stringResource(R.string.settings_notifications),
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

                Box(
                    modifier = Modifier.clickable {
                        showSetLowGlucoseDialog.value = true
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .height(80.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
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
                                text = stringResource(R.string.low_glucose_notification),
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = checkedForLowGlucose.value,
                                onCheckedChange = {
                                    checkedForLowGlucose.value = it
                                    coroutineScope.launch(Dispatchers.IO) {
                                        DataStoreManager.setNotiLowGlucose(it)
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .height(30.dp)
                                .padding(horizontal = 30.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "${targetLowGlucose.value} mg/dL",
                                color = Color(0xFF828282),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Surface(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit_icon),
                                    modifier = Modifier
                                        .size(16.dp),
                                    contentDescription = "저혈당 알림 입력값 수정"
                                )
                            }
                        }
                    }
                }

                Divider()

                Box(
                    modifier = Modifier.clickable {
                        showSetHighGlucoseDialog.value = true
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .height(80.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
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
                                text = stringResource(R.string.high_glucose_notification),
                                fontSize = 16.sp
                            )

                            Switch(
                                checked = checkedForHighGlucose.value,
                                onCheckedChange = {
                                    checkedForHighGlucose.value = it
                                    coroutineScope.launch(Dispatchers.IO) {
                                        DataStoreManager.setNotiHighGlucose(it)
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .height(30.dp)
                                .padding(horizontal = 30.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "${targetHighGlucose.value} mg/dL",
                                color = Color(0xFF828282),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Surface(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit_icon),
                                    modifier = Modifier
                                        .size(16.dp),
                                    contentDescription = "고혈당 알림 입력값 수정"
                                )
                            }
                        }
                    }
                }


                Divider()

                Divider(color = Color.Transparent, thickness = 20.dp)

                Divider()

                Box() {
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
                            text = stringResource(R.string.lost_signal),
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = checkedForLostSignal.value,
                            onCheckedChange = {
                                checkedForLostSignal.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiLostSignal(it)
                                    Log.e("NOTI", "신호소실 DS 값 : ${DataStoreManager.getNotiLostSignal().first()!!}")
                                }
                            }
                        )
                    }
                }

                Divider()

                Box() {
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
                            text = stringResource(R.string.sensor_expired),
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = checkedForExpiredSensor.value,
                            onCheckedChange = {
                                checkedForExpiredSensor.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiExpiredSensor(it)
                                }
                            }
                        )
                    }
                }

                Divider()

                Box() {
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
                            text = stringResource(R.string.sensor_stabilization),
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = checkedForStabilization.value,
                            onCheckedChange = {
                                checkedForStabilization.value = it
                                coroutineScope.launch(Dispatchers.IO) {
                                    DataStoreManager.setNotiStabilization(it)
                                }
                            }
                        )
                    }
                }

                Divider()

                Divider(color = Color.Transparent, thickness = 20.dp)

                Divider()

                Box(
                    modifier = Modifier.clickable {
                        showSetDailyCalibrationDialog.value = true
                        hour.value = Calendar.getInstance().get(Calendar.HOUR).toString()
                        minute.value = Calendar.getInstance().get(Calendar.MINUTE).toString()
                        isAfternoon.value = if(Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) false else true
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .height(80.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
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
                                text = stringResource(R.string.glucose_entry_time_notificaition),
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = checkedForCalibration.value,
                                onCheckedChange = {
                                    checkedForCalibration.value = it
                                    coroutineScope.launch(Dispatchers.IO) {
                                        DataStoreManager.setNotiCalibration(it)
                                    }
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .height(30.dp)
                                .padding(horizontal = 30.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
//                                text = convertToDisplayTime(dailyCalibrationTime.value, isKorean),
                                text = convertToDisplayTime(dailyCalibrationTime.value, true),
                                color = Color(0xFF828282),
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.width(15.dp))
                            Surface(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit_icon),
                                    modifier = Modifier
                                        .size(16.dp),
                                    contentDescription = "혈당 입력 시간 수정"
                                )
                            }
                        }
                    }
                }

                Divider()

            }
        }
    }
}

fun convertToDisplayTime(savedTime: String, isKorean: Boolean): String {
    if (savedTime.isEmpty()) return ""

    return try {
        // 1. 저장된 데이터가 한국어 포맷("오전 11:00")이라고 가정하고 파싱
        // (저장할 때 Locale.KOREAN으로 저장했으므로 읽을 때도 KOREAN으로 읽어야 함)
        val parseFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
        val time = LocalTime.parse(savedTime, parseFormatter)

        // 2. 현재 폰의 언어 설정(Locale.getDefault())에 맞춰서 다시 포맷팅
        // Locale이 US면 "11:00 AM", KOREA면 "오전 11:00"으로 자동 변환됨
        val displayFormatter = if (isKorean) {
            DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN)
        } else {
            DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        }

        time.format(displayFormatter)

    } catch (e: Exception) {
        // 3. 만약 파싱에 실패하면(데이터가 깨졌거나 형식이 다르면)
        // 억지로 바꾸지 말고 원본 그대로 보여줌 (안전장치)
        savedTime
    }
}

