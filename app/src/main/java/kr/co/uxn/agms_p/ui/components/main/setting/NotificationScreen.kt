package kr.co.uxn.agms_p.ui.components.main.setting

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.model.requestDTO.RequestEventData
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.components.main.event.ItemData
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val glucoseDataFromUser = remember { mutableStateOf("") }
    val time = remember { mutableStateOf<String>("") }
    val hint = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            modifier = Modifier.clickable {
                                navController.navigate("MainScreen/${2}")
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "알림 설정",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
        ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })  // 🔹 터치 시 키보드 숨기기
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF2F3F9)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm"))
                    time.value = now
                    Text(
                        modifier = Modifier.padding(start = 20.dp),
                        text = time.value
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Image(
                        modifier = Modifier.size(15.dp),
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "수정 아이콘"
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "자가측정혈당수치를\n입력해주세요.",
                    fontWeight = FontWeight.Medium,
                    fontSize = 23.sp,
                    color = Color(0xFF385DAB),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = glucoseDataFromUser.value,
                    onValueChange = { glucoseDataFromUser.value = it },
                    modifier = Modifier
                        .size(250.dp, 60.dp)
                        .onFocusChanged { focusState ->  // focusObserver 사용
                            if (focusState.isFocused) {
                                hint.value = "" // 포커스가 들어가면 힌트를 비웁니다
                            } else if (glucoseDataFromUser.value.isEmpty()) {
                                hint.value = "혈당을 입력해주세요." // 포커스를 잃고 입력값이 비어있다면 힌트를 다시 보여줍니다.
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


                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "정확한 데이터를 위해서\n최소 식후 2시간 후\n입력하는 것이 좋습니다.",
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(160.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(bottom = 20.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.btn_save),
                        contentDescription = "저장 버튼",
                        modifier = Modifier.align(Alignment.Center)
                            .clickable(
                            ) {
                                if (glucoseDataFromUser.value != "") {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val userId = DataStoreManager.getUserId().first() ?: -1
                                            val formatterOld = DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm")
                                            val formatterNew = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                            val oldParsedTime = LocalDateTime.parse(time.value, formatterOld)
                                            val newParsedTime = oldParsedTime.format(formatterNew)
                                            val eventTypeCode = 1403
                                            Log.e("TEST", "이벤트 전송하기 전 값 확인 userId : $userId eventCode : $eventTypeCode, createdAt : $newParsedTime content : ${glucoseDataFromUser.value}")
                                            // 서버에 이벤트 전송
                                            val upload = tokenRetrofit.uploadEvent(
                                                RequestEventData(
                                                    userId = userId,
                                                    createdAt = newParsedTime,
                                                    eventTypeCode = eventTypeCode,
                                                    content = glucoseDataFromUser.value
                                                )
                                            )

                                            Log.e("EVENT", "uploadBody : ${upload.body().toString()}")
                                            if (upload.isSuccessful) {
                                                val uploadBody = upload.body()
                                                if (uploadBody != null) {
                                                    if (uploadBody.isSuccess) {
                                                        Log.e("EVENT", "EVENT 업로드 성공, ${uploadBody.message}")
                                                        withContext(Dispatchers.Main) {
                                                            val image = R.drawable.event_calibration // 추후 혈당 이미지로 변경
                                                            eventScreenViewModel.addItem(ItemData(imageId = image, eventType = eventTypeCode, time = time.value, content = glucoseDataFromUser.value))
                                                            navController.navigate("MainScreen/${1}")
                                                            Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e("EVENT", "API 에러 : ${upload.errorBody()}")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("EVENT", "네트워크 또는 userId null 에러 : ${e.message}")
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "네트워크를 확인해주세요.", Toast.LENGTH_SHORT).show()
                                                Log.e("EVENT", "활동 이벤트 업로드 실패")
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "혈당을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                }
            }
        }
    }
}

