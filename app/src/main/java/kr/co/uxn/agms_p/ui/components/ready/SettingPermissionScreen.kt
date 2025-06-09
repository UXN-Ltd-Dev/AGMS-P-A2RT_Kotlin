package kr.co.uxn.agms_p.ui.components.ready

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.emptyRetrofit
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignInNormal
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignUpNormal
import kr.co.uxn.agms_p.api.model.requestDTO.RequestSignUpOauthDetail
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.viewmodel.PermissionViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingPermissionScreen(
    navController: NavController,
    viewModel: PermissionViewModel,
    activity: Activity,
    type : Int
) {
//    var timerWhenScan: Timer? = null
    val context = LocalContext.current
    val activityContext = context as Activity
    val isGrant by viewModel.isGrant.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    val permissionStatuses = remember { mutableStateMapOf<String, Boolean>() }
    var isPermissionRequestInProgress by remember { mutableStateOf(false) }

    // SDK 버전에 따른 권한 배열 구성
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.USE_EXACT_ALARM
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val test = rememberMultiplePermissionsState(
        permissions = permissions
    )

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d("PERMISSION", "permissionLauncher 호출")

//        isPermissionRequestInProgress = false
        result.forEach { (permission, isGranted) ->
            permissionStatuses[permission] = isGranted
        }

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activityContext.startActivity(intent)

        if (permissions.any { !permissionStatuses[it]!! && !ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, it) }) {
            showSettingsDialog = true
        }

    }

    fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            Log.e("TEST", "TEST : ${activity.packageName}")
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activityContext.startActivity(intent)
    }

    LaunchedEffect(Unit) {
        permissions.forEach { permission ->
            permissionStatuses[permission] = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

    }

    LaunchedEffect(Unit) {
        while(true) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activityContext.startActivity(intent)
            test.launchMultiplePermissionRequest()
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        DataStoreManager.deleteType()
        DataStoreManager.saveType(type)
    }

    // Show dialog if "Don't Ask Again" was selected
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permissions Required") },
            text = { Text("Some permissions are permanently denied. Please enable them from app settings.") },
            confirmButton = {
                Button(onClick = {
                    showSettingsDialog = false
                    openAppSettings(context)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showSettingsDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = "모든 권한을 허용하지 않아도 앱을 사용할 수 있으나 일부 기능이 제한될 수 있어요",
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
                Column() {
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
                Column() {
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
                Column() {
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

            Spacer(modifier = Modifier.size(140.dp))

            // 확인 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_confirm),
                    contentDescription = "확인 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
//                            test.launchMultiplePermissionRequest()
                            when {
                                test.allPermissionsGranted -> {
                                    // 권한이 허용됨
                                    navController.navigate("GuideScreen1")
                                    Log.d("TEST", "1")
                                }
                                test.shouldShowRationale -> {
                                    // 권한이 거부됨
                                    openAppSettings(activity)
                                    Log.d("TEST", "2")
                                }
                                else -> {
                                    // 권한이 요청됨
                                    navController.navigate("GuideScreen1")
//                                    test.launchMultiplePermissionRequest()
                                    Log.d("TEST", "3")

                                    test.revokedPermissions.forEach {
                                        Log.d("TEST", "Revoked permission : ${it.permission}")
                                    }

                                    test.permissions.forEach {
                                        Log.d("TEST", "granted Permissions : ${it.permission}")
                                    }
                                }
                            }
                        }
                )
            }
        }
    }
}
