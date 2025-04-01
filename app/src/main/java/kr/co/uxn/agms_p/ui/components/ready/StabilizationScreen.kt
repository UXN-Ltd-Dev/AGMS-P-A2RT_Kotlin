package kr.co.uxn.agms_p.ui.components.ready

import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ble.AlwaysService
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel

@Composable
fun StabilizationScreen(navController: NavController, bleViewModel: BleViewModel) {
    val context = LocalContext.current
//    val totalTime = 120 * 60 * 1000L // 120분을 밀리초로 변환
    val totalTime = 1 * 60 * 1000L // 테스트를 위해 1분 설정
    val remainingTime = remember { mutableStateOf(totalTime) }
    LaunchedEffect(Unit) {

        // 서비스 실행 이벤트 발행
        bleViewModel.emit("START_SERVICE")
        Log.e("StabilizationScreen", "START_SERVICE EMIT!")

        // 서비스 종료 이벤트 발행
        //  bleViewModel.emit("STOP_SERVICE")

        val countDownTimer = object : CountDownTimer(remainingTime.value, 60 * 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                // 타이머가 끝나면 다음 화면으로 이동
                navController.navigate("StabilizationCompleteScreen") // "nextScreen"을 다음 화면의 route로 변경
            }
        }
        countDownTimer.start()
    }


    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.size(200.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "남은 시간",
//                fontWeight = FontWeight.Bold,
                fontSize = 27.sp
            )

            Spacer(modifier = Modifier.size(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                val minutes = (remainingTime.value / 1000) / 60 + 1
                if (minutes > 0) {
                    Text(
//                    modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = minutes.toString(),
                        fontSize = 70.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF385DAB)
                    )
                }


                Text(
//                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "분",
                    fontSize = 70.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF385DAB)
                )
            }


            Spacer(modifier = Modifier.size(50.dp))

            Text(
                text = "조금만 기다려주세요.\n센서 안정화 중입니다.",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.size(30.dp))

            Image(
                painter = painterResource(R.drawable.one_hundred_twenty_min),
                contentDescription = "120분 소요",
                modifier = Modifier.size(width = 90.dp, 30.dp)
            )
        }
    }
}