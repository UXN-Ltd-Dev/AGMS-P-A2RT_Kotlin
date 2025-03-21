package kr.co.uxn.agms_p.ui.components.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@Composable
fun SignUpCheckScreen2(navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val pwd1 = remember { mutableStateOf("") }
    val pwd2 = remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                .padding(horizontal = 30.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(40.dp))

            // 백 버튼
            Image(
                painter = painterResource(R.drawable.back_icon),
                contentDescription = "백 버튼",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Start)
                    .clickable {
                        navController.popBackStack()
                    }
            )
            Spacer(modifier = Modifier.size(50.dp))

            // 이메일
            Text(
                text = "이메일",
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            BasicTextField(
                value = email.value,
                onValueChange = { email.value = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .drawBehind {
                                val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                drawLine(
                                    color = Color(0xFFEEEEEF),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(start = 5.dp, end = 40.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (email.value.isEmpty()) {
                            Text(
                                text = "이메일 주소를 입력해 주세요.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Spacer(Modifier.size(10.dp))

            // 중복확인 버튼
            Button(
                shape = RoundedCornerShape(20.dp),
                onClick = {
                    // TODO : 서버에 이메일 중복 확인 요청
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF385DAB) // 배경색 설정
                ),
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Text(
                    text = "중복확인"
                )
            }

            // 비밀번호
            Text(
                text = "비밀번호",
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            BasicTextField(
                value = pwd1.value,
                onValueChange = { pwd1.value = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .drawBehind {
                                val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                drawLine(
                                    color = Color(0xFFEEEEEF),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(start = 5.dp, end = 40.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (pwd1.value.isEmpty()) {
                            Text(
                                text = "비밀번호를 입력해 주세요.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                },
            )

//            Spacer(modifier= Modifier.size(10.dp))

            // TODO 입력된 값이 조건에 맞는지 체크하는 로직 필요

            Text(
                text = "8~16자의 영문, 숫자, 특수문자를 조합해 사용해주세요.",
                color = Color.Gray,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.size(20.dp))

            // 비밀번호 확인
            Text(
                text = "비밀번호 확인",
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            BasicTextField(
                value = pwd2.value,
                onValueChange = { pwd2.value = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .drawBehind {
                                val strokeWidth = 3.dp.toPx() // 선 두께 설정
                                val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                drawLine(
                                    color = Color(0xFFEEEEEF),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .padding(start = 5.dp, end = 40.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (pwd2.value.isEmpty()) {
                            Text(
                                text = "비밀번호를 확인 해주세요.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                },
            )

            // 다음 버튼
            Spacer(modifier = Modifier.size(250.dp))
            Button(
                onClick = {
                    if (email.value.isEmpty()) {
                        Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (pwd1.value.isEmpty()) {
                        Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (pwd2.value.isEmpty()) {
                        Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (pwd1.value != pwd2.value) {
                        Toast.makeText(context, "비밀번호가 일치하지 않습니다.\n다시 시도해보세요.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        navController.navigate("SignUpInfoScreen3/${email.value}/${pwd1.value}")
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
                    text = "다음",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }
    }
}


