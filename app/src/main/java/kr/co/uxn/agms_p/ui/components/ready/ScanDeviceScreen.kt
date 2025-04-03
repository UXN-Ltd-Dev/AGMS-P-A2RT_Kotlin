package kr.co.uxn.agms_p.ui.components.ready

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel

@Composable
fun ScanDeviceScreen(navController: NavController, bleViewModel: BleViewModel, mac: String) {
    val context = LocalContext.current
    val data by bleViewModel.isFindDevice.collectAsState()
    val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bleManager.adapter
    val scanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val deviceInfo = result?.device
            val rssi = result?.rssi
            Log.d("SCAN", "mac : ${deviceInfo?.address}, id : ${deviceInfo?.name}, rssi : ${rssi}")
            bleViewModel.updateIsFindDevice(true)
            // 성공시, 안정화 화면으로 이동
            navController.navigate("StabilizationScreen")
            // TODO : 스캔 종료 로직 필요
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d("SCAN", "onBatchScanResults")
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("SCAN", "onScanFailed..  errorCode : $errorCode")

            // 실패시, 실패 화면으로 이동
            navController.navigate("ScanFailScreen")
            // TODO : 스캔 종료 로직 필요
//                bluetoothAdapter.bluetoothLeScanner.stopScan(this)
        }
    }

    suspend fun teraRups() {
        delay(1000 * 60)
        Log.e("teraRups", "teraRups called!")
        if (!bleViewModel.isFindDevice.value) {
            navController.navigate("ScanFailScreen")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        }
    }


    LaunchedEffect(key1 = Unit) {
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

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO : 권한 요청 로직 필요
        }
        // 스캔 시작
        bluetoothAdapter.bluetoothLeScanner.startScan(mScanFilter, scanSettings, scanCallback)
        // 옵저빙하고 있다가 스캔 종료

        launch {
            bleViewModel.isFindDevice.collect { isFind ->
                if (isFind) {
                    bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
                }
            }
        }

        launch {
            teraRups()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(start = 30.dp, top = 38.dp),
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

            Spacer(modifier = Modifier.size(50.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "기기를 검색중입니다.",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )


            Spacer(modifier = Modifier.size(50.dp))

            Image(
                modifier = Modifier.size(250.dp),
                painter = painterResource(R.drawable.ble_scanning),
                contentDescription = "스캔 중"
            )

            Spacer(modifier = Modifier.size(50.dp))

            Text(
                text = "최소 3분 정도 소요됩니다",
                fontSize = 18.sp
            )

        }
    }
}
