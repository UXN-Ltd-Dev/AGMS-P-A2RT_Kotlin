//package kr.co.uxn.agms_p.ui.components.main.event
//
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.ui.res.stringResource
//import androidx.core.app.NotificationManagerCompat
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kr.co.uxn.agms_p.R
//import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
//import kr.co.uxn.agms_p.api.token.DataStoreManager
//import kr.co.uxn.agms_p.ble.BleBridge
//import kr.co.uxn.agms_p.ble.BleBridge.showHighGlucoseDialog
//import kr.co.uxn.agms_p.ble.BleBridge.showLowGlucoseDialog
//import kr.co.uxn.agms_p.ui.components.main.ModeDialog
//import kr.co.uxn.agms_p.ui.components.main.NotiDialog
//import kotlin.system.exitProcess
//
//class Test {
//    // 1. Dialog : 일일 혈당 입력
//    if (showCaliDialog.value) {
//        NotiDialog(
//            onDismiss = { BleBridge.showCaliDialog(false) },
//            onConfirm = {
//                BleBridge.showCaliDialog(false)
//                navController.navigate("GlucoseRegisterScreen")
//            },
//            title = stringResource(R.string.dialog_daily_enter_glucose_title),
//            content = stringResource(R.string.dialog_daily_enter_glucose_content)
//        )
//    }
//
//    // 2. Dialog : BLE 끊김
//    if (showBleConnectDialog.value) {
//        NotiDialog(
//            onDismiss = { BleBridge.showBleConnectDialog(false) },
//            onConfirm = {
//                BleBridge.showBleConnectDialog(false)
//            },
//            title = stringResource(R.string.dialog_ble_disconnected_title),
//            content = stringResource(R.string.dialog_ble_disconnected_content)
//        )
//    }
//
//    // 3. 그래프 모드 다이얼로그
//    if (showModeDialog.value) {
//        ModeDialog(
//            options = listOf(
//                stringResource(R.string.chart_option_glucose), stringResource(R.string.chart_option_weo1), stringResource(
//                    R.string.chart_option_weo2)
//            ),
//            selectedOption = selectedChartOption,
//            onOptionSelected =
//                {
//                    showModeDialog.value = false
//                    selectedChartOption = it
//                },
//            onDismissRequest = { showModeDialog.value = false }
//        )
//    }
//
//    // 4. Dialog : 블루투스 ON
//    if (showBluetoothOnDialog.value) {
//        NotiDialog(
//            onDismiss = { BleBridge.showBluetoothOnDialog(false) },
//            onConfirm = {
//                BleBridge.showBluetoothOnDialog(false)
//            },
//            title = stringResource(R.string.dialog_bluetooth_off_title),
//            content = stringResource(R.string.dialog_bluetooth_off_content),
//        )
//    }
//
//    // 5. Dialog : 측정 종료
//    if (showEndMeasurementDialog.value) {
//        NotiDialog(
//            onDismiss = { BleBridge.showBleConnectDialog(false) },
//            onConfirm = {
//                BleBridge.showBleConnectDialog(false)
//                coroutineScope.launch(Dispatchers.IO) {
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
//                                    Log.d("TEST", "${DataStoreManager.getIsMain().first()}")
//                                    DataStoreManager.deleteAccessToken()
//                                    DataStoreManager.deleteRefreshToken()
//                                    DataStoreManager.deleteUserId()
//                                    DataStoreManager.deleteDeviceMac()
//                                    DataStoreManager.deleteStartTime()
//                                    DataStoreManager.deleteEndTime()
//                                    DataStoreManager.deleteDailyCalibrationTime()
//                                    DataStoreManager.deleteDailyCalibrationLastTime()
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
//                            Toast.makeText(context, context.getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            },
//            title = stringResource(R.string.dialog_end_measurement_title),
//            content = stringResource(R.string.dialog_end_measurement_content),
//        )
//    }
//
//    // 6.1 Dialog : 저혈당
//    if (showLowGlucoseDialog.value) {
//        NotiDialog(
//            onDismiss = { showLowGlucoseDialog(false) },
//            onConfirm = {
//                showLowGlucoseDialog(false)
//                NotificationManagerCompat.from(context).cancel(95)
//            },
//            title = stringResource(R.string.dialog_low_glucose_title),
//            content = stringResource(R.string.dialog_low_glucose_content),
//        )
//    }
//
//    // 6.2 Dialog : 고혈당
//    if (showHighGlucoseDialog.value) {
//        NotiDialog(
//            onDismiss = { showHighGlucoseDialog(false) },
//            onConfirm = {
//                showHighGlucoseDialog(false)
//                NotificationManagerCompat.from(context).cancel(96)
//            },
//            title = stringResource(R.string.dialog_high_glucose_title),
//            content = stringResource(R.string.dialog_high_glucose_content)
//        )
//    }
//}