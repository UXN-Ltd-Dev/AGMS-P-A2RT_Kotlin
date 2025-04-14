package kr.co.uxn.agms_p.ui.components.main.setting

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, paddingValues: PaddingValues, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val items = listOf(
        "내 정보",
        "센서 정보",
        "알림 설정",
        "이용 약관",
        "개인정보 처리방침",
        "로그아웃",
        "계정 삭제"
    )

    Column(modifier = Modifier.fillMaxSize()
        .background(Color(0xF7F7FB))
        .padding(paddingValues)
    ) {
        Divider()
        items.forEachIndexed { index, item ->
            SettingItem(item, index, context, navController, bleViewModel)
            if (index == 0 || index == 3 || index == 5) {
                Divider(color = Color.Transparent, thickness = 1.dp)
            }
            else {
                Divider(color = Color.Transparent, thickness = 20.dp)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SettingItem(title: String, index: Int, context: Context, navController: NavController, bleViewModel: BleViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "로그아웃") },
            text = { Text(text = "진행 하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        // 확인 동작


                        coroutineScope.launch {
                            // 0. 토큰 정리
                            DataStoreManager.deleteAccessToken()
                            DataStoreManager.deleteRefreshToken()
                            DataStoreManager.deleteUserId()
                            DataStoreManager.deleteDeviceMac()
                            DataStoreManager.deleteStartTime()
                            DataStoreManager.deleteEndTime()
                            // 1. 서비스 종료
                            bleViewModel.emit("STOP_SERVICE")
                            // 앱 강제종료
                            android.os.Process.killProcess(android.os.Process.myPid())
                            exitProcess(0)
                        }
                        // 2. 데이터 전송
                        // 3. 로그인 화면으로 이동
                        navController.navigate("Login") {
                            popUpTo(0)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,   // 버튼 배경색
                        contentColor = Color.White          // 텍스트 색
                    )
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        // 취소 동작
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,   // 버튼 배경색
                        contentColor = Color.White          // 텍스트 색
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = Color.LightGray)
            .clickable {
                when(index) {
                    0 -> {
                        navController.navigate("MyInfoScreen")
                    }
                    1 -> {
                        navController.navigate("SensorInfoScreen")
                    }
                    2 -> {
                        navController.navigate("NotificationScreen")
                    }
                    3 -> {
                        navController.navigate("TermsAndConditionsScreen")
                    }
                    4 -> {
                        navController.navigate("PrivacyPolicyScreen")
                    }
                    5 -> {
                        showDialog.value = true
                    }
                    6 -> {
                        navController.navigate("DeleteAccountScreen")
                    }
                }

            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = title, fontSize = 16.sp)
    }
}