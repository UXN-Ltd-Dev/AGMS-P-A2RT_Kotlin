package kr.co.uxn.agms_p.ui.components.main.setting

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, paddingValues: PaddingValues, bleViewModel: BleViewModel) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showDialog.value) {
        LogoutDialog(
            onConfirm = {
                showDialog.value = false
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
            onDismiss = {
                showDialog.value = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
        .background(Color(0xF7F7FB))
        .padding(paddingValues)
    ) {
        Divider()
        Box(
            modifier = Modifier.clickable {
                navController.navigate("MyInfoScreen")
            }
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
                    text = "내 정보",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("SensorInfoScreen")
            }
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
                    text = "센서 정보",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Divider(color = Color.Transparent, thickness = 20.dp)

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("NotificationScreen")
            }
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
                    text = "알림 설정",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Divider(color = Color.Transparent, thickness = 20.dp)

        Divider()


        Box(
            modifier = Modifier.clickable {
                navController.navigate("TermsAndConditionsScreen")
            }
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
                    text = "이용 약관",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("PrivacyPolicyScreen")
            }
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
                    text = "개인정보 처리방침",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Divider(color = Color.Transparent, thickness = 20.dp)

        Divider()

        Box(
            modifier = Modifier.clickable {
                showDialog.value = true
            }
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
                    text = "로그아웃",
                    fontSize = 16.sp
                )
            }
        }

        Divider()

        Box(
            modifier = Modifier.clickable {
                navController.navigate("DeleteAccountScreen")
            }
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
                    text = "계정 삭제",
                    fontSize = 16.sp
                )
            }
        }

        Divider()
    }
}

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column {
                androidx.compose.material3.Text(
                    text = "로그아웃",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.Text(
                    text = "정말 로그아웃하시겠습니까?",
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color(0xFFD8D8D8))
                    ) {
                        androidx.compose.material3.Text(
                            text = "취소",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3451B2), // 파란색
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.Text(
                            text = "확인",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

