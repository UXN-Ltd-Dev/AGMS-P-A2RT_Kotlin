package kr.co.uxn.agms_p.ui.components.login

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@Composable
fun LoginPasswordScreen(email: String, navController: NavController) {
    Log.e("LoginPasswordScreen", "email: ${email}")
    val password = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(150.dp),
                painter = painterResource(R.drawable.uxn_logo),
                contentDescription = "로고"
            )
            Text(text = "비밀번호를 입력하세요.")
            Text(text = email)
            TextField(
                value = password.value,
                onValueChange = { password.value = it },
                placeholder = { Text("비밀번호") },
                visualTransformation = PasswordVisualTransformation(),  // 👈 비밀번호 감추기
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Text(
                text = "비밀번호 찾기"
            )
            Button(
                onClick = {
                    // 서버에 올려서 비번 맞는지 확인 또는, 맞으면 토큰 받아와서 메인화면 이동
                    navController.navigate("SettingScreen")
                },
            ) {
                Text(
                    text = "로그인"
                )
            }
        }
    }
}