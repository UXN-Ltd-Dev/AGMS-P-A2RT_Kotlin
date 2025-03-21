package kr.co.uxn.agms_p.ui.components.ready

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.viewmodel.RegisterViewModel

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
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.size(100.dp))
            Text(
                text = "Always앱에서 사용하는\n권한을 알려드려요.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(20.dp))
            Text(
                text = "모든 권한을 허용하지 않아도 앱을 사용할 수 있으나 일부 기능이 제한될 수 있어요.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.size(80.dp))

            Row(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Image(
                    painterResource(R.drawable.notifications_permission),
                    modifier = Modifier.size(35.dp),
                    contentDescription = "알림 권한"
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(){
                    Text(
                        text = "알림(필수)"
                    )
                    Text(
                        text = "알림 메세지 발송",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.size(15.dp))

            Row(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Image(
                    painterResource(R.drawable.nearby_permission),
                    modifier = Modifier.size(35.dp),
                    contentDescription = "근처 기기 권한"
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(){
                    Text(
                        text = "근처 기기(필수)"
                    )
                    Text(
                        text = "근처기기 연결, 상대적 위치 파악",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.size(15.dp))

            Row(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Image(
                    painterResource(R.drawable.battery_permission),
                    modifier = Modifier.size(35.dp),
                    contentDescription = "배터리 권한"
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(){
                    Text(
                        text = "배터리 사용화 최적화 중지(필수)"
                    )
                    Text(
                        text = "백그라운드 배터리 사용량 제한",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.size(170.dp))

            // 확인 버튼
            Button(
                onClick = {
                    Log.e("PERMISSION", "isGrant : ${isGrant}")
                    if (isGrant) {
                        navController.navigate("GuideScreen1")
                    } else {
                        openAppSettings(activity)
                        viewModel.changeGrantState(true)
                    }
                },
                modifier = Modifier
                    .size(280.dp, 50.dp)
                    .background(
                        color = Color(0xFF385DAB),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "확인",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }
    }
}
