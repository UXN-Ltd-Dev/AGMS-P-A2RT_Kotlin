package kr.co.uxn.agms_p.ui.components.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
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
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.Start
        ) {


            Spacer(modifier = Modifier.size(50.dp))

            // UXN 로고
            Image(
                modifier = Modifier.size(150.dp),
                painter = painterResource(R.drawable.uxn_logo),
                contentDescription = "로고"
            )
            Spacer(modifier = Modifier.size(40.dp))
            Text(
                text = "비밀번호를 입력하세요.",
                fontSize = 20.sp,
//                fontWeight = Bold
            )

            Spacer(modifier = Modifier.size(5.dp))
            Row() {
                Text(
                    text = email,
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = "변경",
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("Login")
                    }
                )
            }

            Spacer(modifier = Modifier.size(5.dp))
            // 비밀번호
            BasicTextField(
                value = password.value,
                onValueChange = { password.value = it },
                visualTransformation = PasswordVisualTransformation(),  // 👈 비밀번호 감추기
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                modifier = Modifier.padding(end = 30.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .border(
                                color = Color.Gray,
                                width = 0.5.dp,
                                shape = RoundedCornerShape(7.dp)
                            )
                            .padding(start = 5.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (password.value.isEmpty()) {
                            Text(
                                text = "비밀번호",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = "비밀번호 찾기",
                color = Color.Gray,
                textDecoration = TextDecoration.Underline,
                fontSize = 14.sp,
            )


            Spacer(modifier = Modifier.size(200.dp))
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    // 서버에 올려서 비번 맞는지 확인 또는, 맞으면 토큰 받아와서 메인화면 이동
                    if (!password.value.isNullOrEmpty()) {
                        navController.navigate("SettingScreen")
                    } else {
                        Toast.makeText(navController.context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                )
            ) {
                Text(text = "로그인")
            }
        }
    }
}