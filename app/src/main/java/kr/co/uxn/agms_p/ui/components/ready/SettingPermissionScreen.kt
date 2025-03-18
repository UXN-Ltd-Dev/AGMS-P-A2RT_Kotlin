package kr.co.uxn.agms_p.ui.components.ready

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.viewmodel.RegisterViewModel
import java.util.Timer
import java.util.TimerTask

@Composable
fun SettingPermissionScreen(navController: NavController, viewModel: RegisterViewModel, activity: Activity) {
//    var timerWhenScan: Timer? = null
    val context = LocalContext.current
    val activityContext = context as Activity
    val isGrant by viewModel.isGrant.collectAsState()

    // SDK 버전에 따른 권한 배열 구성
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.USE_EXACT_ALARM
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        Log.d("PERMISSION", "permissionLauncher 호출")

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activityContext.startActivity(intent)
    }


    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            Log.e("TEST", "TEST : ${activity.packageName}")
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activityContext.startActivity(intent)
    }

    LaunchedEffect(Unit) {
        while (!isGrant) {
            permissionLauncher.launch(permissions)
            delay(5000)
        }
    }

    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(100.dp))
            Image(
                modifier = Modifier.size(150.dp),
                painter = painterResource(R.drawable.ble_icon),
                contentDescription = "ble 아이콘"
            )
            Text(
                text = "블루투스",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text("환영합니다.")
            Text("Agms의 다양한 서비스를 위해")
            Text("몇 가지 설정이 추가로 필요합니다.")

            Spacer(modifier = Modifier.size(200.dp))
            Button(
                onClick = {
                    Log.e("PERMISSION", "isGrant : ${isGrant}")
                    if (isGrant) {
                        navController.navigate("EnterInfoScreen")
                    } else {
                        openAppSettings(activity)
                        viewModel.changeGrantState(true)
                    }
                },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                )
            ) {
                Text(text = "다음")
            }
        }
    }
}
