package kr.co.uxn.agms_p.ui.components.login

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpInfoScreen3(navController: NavController, email: String, pwd: String) {
    val context = LocalContext.current

    var expandedForSex by remember { mutableStateOf(false) }
    var expandedForDiabetesType by remember { mutableStateOf(false) }

    val sex = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val height = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val diabetesType = remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = Unit) {
        Log.e("SignUpInfoScreen3", "email: $email, pwd: $pwd")
    }

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
//            Image(
//                painter = painterResource(R.drawable.back_icon),
//                contentDescription = "백 버튼",
//                modifier = Modifier
//                    .size(40.dp)
//                    .align(Alignment.Start)
//                    .clickable {
//                    navController.popBackStack()
//                }
//            )
            Spacer(modifier = Modifier.size(50.dp))

            // 내 정보를 입력하세요.
            Text(
                text = "내 정보를 입력하세요.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            // 혈당, 혈당 추세 ..
            Text(
                text = "혈당, 혈당 추세 등 더욱 정확한 결과를 제공할려면 이 정보가 필요합니다. Always가 제공하는 알고리즘을 적용하기 위해서는 나이 외에도 이러한 추가 정보를 입력해야 여러분에게 꼭 맞는 데이터를 제공할 수 있습니다. 자세히 보기",
                fontSize = 15.sp,
                color = Color(0xFF828282),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))


            // 1. 성별
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "성별")
                Spacer(modifier = Modifier.size(60.dp, 30.dp))
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable {
                            expanded = !expanded
                        }
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray, // 원하는 색상
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sex.value,
                        modifier = Modifier.clickable {
                            expanded = !expanded
                        }
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = DpOffset(100.dp, -200.dp)
                ) {
                    // First section
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "남성",
                            )
                        },
                        onClick = {
                            sex.value = "남성"
                            expanded = !expanded
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("여성") },
                        onClick = {
                            sex.value = "여성"
                            expanded = !expanded
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("선택 안함") },
                        onClick = {
                            sex.value = "선택 안함"
                            expanded = !expanded
                        }
                    )
                }
            }



            // 2. 연령
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "연령")
                Spacer(modifier = Modifier.size(60.dp, 30.dp))

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray, // 원하는 색상
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    BasicTextField(
                        value = age.value,
                        onValueChange = { age.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(30.dp)
                                    .drawBehind {
                                        val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    .padding(start = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        },
                    )
                }
            }

            // 3. 신장
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "신장")
                Spacer(modifier = Modifier.size(60.dp, 30.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    BasicTextField(
                        value = height.value,
                        onValueChange = { height.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(30.dp)
                                    .drawBehind {
                                        val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    .padding(start = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        },
                    )
                }

            }

            // 4. 체중
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "체중")
                Spacer(modifier = Modifier.size(60.dp, 30.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {

                    BasicTextField(
                        value = weight.value,
                        onValueChange = { weight.value = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        }),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(30.dp)
                                    .drawBehind {
                                        val strokeWidth = 0.dp.toPx() // 선 두께 설정
                                        val y = size.height - strokeWidth / 2 // 선을 하단에 위치
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                    .padding(start = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                innerTextField()
                            }
                        },
                    )
                }
            }

            // 5. 당뇨 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 50.dp)
            ) {
                Text(text = "당뇨 정보")
                // 제1형 당뇨병
                // 제2형 당뇨병
                // 임신성 당뇨병
                // 당뇨 전단계
                // LADA(Latent Autoimmune Diabetes in Adults)
                // 정상
                Spacer(modifier = Modifier.size(30.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .clickable {
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx()  // 선 굵기
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = Color.Gray, // 원하는 색상
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = diabetesType.value,
                        modifier = Modifier.clickable {
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                }
                DropdownMenu(
                    expanded = expandedForDiabetesType,
                    onDismissRequest = { expandedForDiabetesType = false },
                    offset = DpOffset(100.dp, -10.dp)
                ) {
                    // First section
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "정상",
                            )
                        },
                        onClick = {
                            diabetesType.value = "정상"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("당뇨 전단계") },
                        onClick = {
                            diabetesType.value = "당뇨 전단계"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("제1형 당뇨병") },
                        onClick = {
                            diabetesType.value = "제1형 당뇨병"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("제2형 당뇨병") },
                        onClick = {
                            diabetesType.value = "제2형 당뇨병"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("임신성 당뇨병") },
                        onClick = {
                            diabetesType.value = "임신성 당뇨병"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("LADA") },
                        onClick = {
                            diabetesType.value = "LADA"
                            expandedForDiabetesType = !expandedForDiabetesType
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.size(100.dp))

            // 개인정보 처리방침 및..
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable {
                        checked.value = !checked.value
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Checkbox(
                    checked = checked.value,
                    onCheckedChange = { checked.value = it }, // 바꼈을 때, 처리할 곳
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "개인정보 처리방침 및 이용약관에 동의합니다.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.size(30.dp))

            // 확인 버튼
            Button(
                onClick = {
                    if (sex.value == "") {
                        Toast.makeText(context, "성별을 선택해 주세요.", Toast.LENGTH_SHORT).show()
                    } else if (age.value == "") {
                        Toast.makeText(context, "나이를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    } else if (height.value == "") {
                        Toast.makeText(context, "신장을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    } else if (weight.value == "") {
                        Toast.makeText(context, "체중을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    } else if (diabetesType.value == "") {
                        Toast.makeText(context, "당뇨 정보를 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    } else if (checked.value == false) {
                        Toast.makeText(context, "개인정보 처리방침 및 이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate("SettingPermissionScreen")
                        // TODO : 서버에 가입정보 전달
                        // TODO : 가입에 성공하면 로그인 시도
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