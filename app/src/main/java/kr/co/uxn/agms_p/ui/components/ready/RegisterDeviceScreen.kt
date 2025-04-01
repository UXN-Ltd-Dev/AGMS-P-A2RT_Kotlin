package kr.co.uxn.agms_p.ui.components.ready

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@Composable
fun RegisterDeviceScreen(navController: NavController) {
    val context = LocalContext.current
    val deviceNumber = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 터치 시, 힌트를 지우기 위한 용도
    val interactionSource = remember { MutableInteractionSource() }
    val hint = remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(start = 30.dp, top = 38.dp),
            ) {
                // 백 버튼
                Image(
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = "백 버튼",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.popBackStack()
                        }
                )
            }

            Spacer(modifier = Modifier.size(50.dp))

            Text(
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        navController.navigate("StabilizationScreen")
                    },
                textAlign = TextAlign.Center,
                text = "블루투스 연결을 위해\n기기 번호를 입력해주세요.",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(15.dp))

            Image(
                modifier = Modifier.size(300.dp, 250.dp),
                painter = painterResource(R.drawable.serial_number_pic),
                contentDescription = "시리얼넘버 예시 사진"
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                text = "제품패키지 라벨의 시리얼 넘버",
                color = Color(0xFF385DAB),
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.size(20.dp))

            OutlinedTextField(
                value = deviceNumber.value,
                onValueChange = { deviceNumber.value = it },
                modifier = Modifier
                    .size(230.dp, 55.dp)
                    .onFocusChanged { focusState ->  // focusObserver 사용
                        if (focusState.isFocused) {
                            hint.value = "" // 포커스가 들어가면 힌트를 비웁니다
                        } else if (deviceNumber.value.isEmpty()) {
                            hint.value = "기기 번호를 입력해주세요." // 포커스를 잃고 입력값이 비어있다면 힌트를 다시 보여줍니다.
                        }
                    },
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                ),
                singleLine = true,
                placeholder = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = hint.value,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                },
                interactionSource = interactionSource, // 터치 이벤트 감지
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.size(50.dp))

            // 다음 버튼
            Button(
                onClick = {
                    if (deviceNumber.value != "") {
                        navController.navigate("ScanDeviceScreen")
                    } else {
                        Toast.makeText(context, "기기 번호를 입력해 주세요.",Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .size(280.dp, 50.dp)
                    .background(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF385DAB)
                    )
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
