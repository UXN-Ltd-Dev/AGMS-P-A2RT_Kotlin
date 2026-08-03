package kr.co.uxn.agms_p_a2rt.ui.components.ready

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.model.requestDTO.RequestLinkDevice
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ble.Device
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.BleViewModel

@Composable
fun ScanDeviceScreen(navController: NavController, bleViewModel: BleViewModel, mac: String, serialNumber: String) {
    // 로티 애니메이션
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan_rt_test))
    val coroutineScope = rememberCoroutineScope()
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 0.2f
    )


    val context = LocalContext.current
    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }
    val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bleManager.adapter
    val scanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (bleViewModel.isFindDevice.value) return

            val deviceInfo = result?.device
            val rssi = result?.rssi
            Log.d("SCAN", "mac : ${deviceInfo?.address}, id : ${deviceInfo?.name}, rssi : ${rssi}")
            bleViewModel.updateIsFindDevice(true)
            bluetoothAdapter.bluetoothLeScanner?.stopScan(this)
            DataStoreManager.getUserId()
            coroutineScope.launch(Dispatchers.IO) {
                DataStoreManager.deleteDeviceMac()
                DataStoreManager.saveDeviceMac(mac)
                val verifiedDeviceMac = DataStoreManager.getDeviceMac().first()
                Log.d("TEST", "스캔화면에서 저장한 datastore 맥 주소 : ${verifiedDeviceMac}")


                val userId = DataStoreManager.getUserId().first() ?: -1

                // 서버에 유저와 디바이스 링크
                try {
                    val detectorList = localDbRepository?.dataDao()?.getListAfterLastTime(userId, 0)
                    Log.d("TEST", "스캔화면에서 저장한 datastore 맥 주소 : ${verifiedDeviceMac}")
                    if (detectorList.isNullOrEmpty()) {
                        Log.d("ScanDeviceScreen, 링크 결정", "detectorList is null or empty!")
                        val linkDevice = tokenRetrofit.linkDevice(RequestLinkDevice(userId = userId, serialNumber = serialNumber))
                        if (linkDevice.isSuccessful) {
                            val linkDeviceBody = linkDevice.body()
                            if (linkDeviceBody != null) {
                                Log.d("TEST", "linkDeviceBody : ${linkDeviceBody}")
                                if (linkDeviceBody.isSuccess) {
                                    Log.d("TEST", "링크 성공!")
                                    DataStoreManager.deleteUserDeviceId()
                                    DataStoreManager.saveUserDeviceId(linkDeviceBody.userDeviceId)
                                    Log.d("TEST", "저장된 user_device_id : ${DataStoreManager.getUserDeviceId().first()}")
                                } else {
                                    Log.d("TEST", "링크 실패 : ${linkDeviceBody.message}")
                                }
                            }
                        } else {
                            Log.d("TEST", "API 에러 : ${linkDevice.errorBody()?.string()}")
                        }
                    } else {
                        Log.d("TEST", "detectorList is exist : ${detectorList}")
                    }

                    DataStoreManager.deleteSerialNumber()
                    DataStoreManager.saveSerialNumber(serialNumber)


                } catch (e: Exception) {
                    Log.d("TEST","네트워크 에러 : ${e.message}")
                }
            }

            // 성공시, 안정화 화면으로 이동
            val mac = "test"
            navController.navigate("StabilizationScreen/$mac")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d("SCAN", "onBatchScanResults")
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("SCAN", "onScanFailed..  errorCode : $errorCode")
            bleViewModel.updateIsFindDevice(false)
            navController.navigate("ScanFailScreen")
        }
    }

    LaunchedEffect(mac, serialNumber) {
        // BleViewModel은 화면 이동 후에도 유지되므로 새 검색마다 상태를 초기화한다.
        bleViewModel.updateIsFindDevice(false)
        DataStoreManager.deleteActivateFirst()
        DataStoreManager.setFirstActivateDB(false)
        if (mac == "999999") {
            navController.navigate("StabilizationScreen/$mac")
        } else {
            var mScanFilter = mutableListOf<ScanFilter>()
            Log.e("TAG", "스캔에 쓰일 Mac : $mac")

            val scanFilter = ScanFilter
                .Builder()
                .setDeviceAddress(mac)
                .build()

            mScanFilter.add(scanFilter)

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // 빠른 스캔 모드
                .build()

            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (scanner == null) {
                Log.e("SCAN", "BluetoothLeScanner를 가져오지 못했습니다.")
                navController.navigate("ScanFailScreen")
                return@LaunchedEffect
            }

            try {
                Log.d("SCAN", "BLE 스캔 시작: $mac")
                scanner.startScan(mScanFilter, scanSettings, scanCallback)
                delay(20_000L)

                if (!bleViewModel.isFindDevice.value) {
                    Log.e("SCAN", "20초 동안 기기를 찾지 못해 실패 화면으로 이동합니다.")
                    navController.navigate("ScanFailScreen")
                }
            } finally {
                scanner.stopScan(scanCallback)
                Log.d("SCAN", "BLE 스캔 종료: $mac")
            }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(start = 30.dp, top = 30.dp),
            ) {
                // 백 버튼
                Image(
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = "백 버튼",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.popBackStack()
                        }
                )
            }

            Spacer(modifier = Modifier.size(80.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.scanning_title),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(modifier = Modifier.size(50.dp))

            // 로티 애니메이션
            Box(
                modifier = Modifier.size(210.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.size(100.dp))

            Text(
                text = stringResource(R.string.scanning_sub_title),
                fontSize = 18.sp
            )
        }
    }
}
