package kr.co.uxn.agms_p.ui.components.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.retrofitMachine
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEmailVerificationCode
import kr.co.uxn.agms_p.api.model.requestDTO.RequestKakaoAccessCode

@Composable
fun SignUpCheckScreen2(navController: NavController) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val verificationCode = remember { mutableStateOf("") }
    val pwd1 = remember { mutableStateOf("") }
    val pwd2 = remember { mutableStateOf("") }
    val isEmailVerified = remember { mutableStateOf(false) }

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
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Spacer(Modifier.size(10.dp))

            // 인증번호 전송 버튼
            Button(
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    // TODO : 서버에 인증번호 요청
                    // 만약 이메일이 중복되면, 중복되었다는 토스트 메시지

                    // 인증완료 테스트를 위한 코드
                    isEmailVerified.value = !isEmailVerified.value
                    Log.e("TAG", "isEmailVerified : ${isEmailVerified.value}")
                    // 인증완료 테스트를 위한 코드 끝

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val result = retrofitMachine.sendVerificationCode(
                                RequestEmailVerificationCode(email = email.value)
                            )
                            if (result.isSuccessful) {
                                Log.d("TAG", "서버 응답: ${result.body()}")
                                val resultBody = result.body()
                                if (resultBody != null) {
                                    if(resultBody.isDuplicated) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "이미 가입된 이메일입니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    // 중복되지 않았다면 진행바 실행 및 인증번호 전송
                                    else { // isDuplicated = false
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show()
                                            verificationCode.value = resultBody.authenticationCode.toString()
                                        }
                                    }
                                } else {
                                    Log.e("TAG", "서버 응답이 null 입니다.")
                                }
                            } else {
                                Log.e("TAG", "API 실패: ${result.errorBody()?.string()}")
                            }
                        } catch (e: Exception) {
                            Log.e("TAG", "네트워크 오류 발생: ${e.message}")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF385DAB) // 배경색 설정
                ),
                modifier = Modifier
                    .align(Alignment.End)
                    .size(120.dp, 35.dp)
            ) {
                Text(
                    text = "인증번호 전송",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 이메일 인증번호
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "이메일 인증번호",
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(10.dp))
                if (isEmailVerified.value) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color(0xFF009900), // 색상을 Green으로 변경,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    // 한번 생긴 아이콘이 정말 사라지는지 확인 필요

//                            Icon(
//                                imageVector = Icons.Default.Check,
//                                contentDescription = "Verified",
//                                tint = Color.White, // 색상을 Green으로 변경,
//                                modifier = Modifier.size(0.dp)
//                            )
                }
            }
            BasicTextField(
                value = verificationCode.value,
                onValueChange = { verificationCode.value = it },
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
                        if (verificationCode.value.isEmpty()) {
                            Text(
                                text = "인증번호를 입력해 주세요.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Spacer(Modifier.size(20.dp))



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
                                fontSize = 13.sp
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
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp)
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
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                },
            )

            // 다음 버튼
            Spacer(modifier = Modifier.size(110.dp))
            Button(
                onClick = {
                    if (email.value.isEmpty()) {
                        Toast.makeText(context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (pwd1.value.isEmpty()) {
                        Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (pwd2.value.isEmpty()) {
                        Toast.makeText(context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    } else if (pwd1.value != pwd2.value) {
                        Toast.makeText(context, "비밀번호가 일치하지 않습니다.\n다시 시도해보세요.", Toast.LENGTH_SHORT).show()
                    } else if (!isEmailVerified.value) {
                        Toast.makeText(context, "이메일 인증을 해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 인증 성공시
                        navController.navigate("SignUpInfoScreen3/${email.value}/${pwd1.value}")
                    }
                },
                modifier = Modifier
                    .size(280.dp, 50.dp)
                    .background(
                        color = Color(0xFF385DAB),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .align(Alignment.CenterHorizontally),
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


