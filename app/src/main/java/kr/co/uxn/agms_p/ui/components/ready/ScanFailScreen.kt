package kr.co.uxn.agms_p.ui.components.ready

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
import androidx.compose.runtime.getValue
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kr.co.uxn.agms_p.R

@Composable
fun ScanFailScreen(navController: NavController) {
    val context = LocalContext.current

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan_fail_lottie))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 0.3f
    )


    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(start = 30.dp, top = 30.dp),
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

            Spacer(modifier = Modifier.size(30.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "블루투스 연결에\n실패하였습니다.",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.size(50.dp))

            Box(
                modifier = Modifier.size(200.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }


            Spacer(modifier = Modifier.size(50.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "다시 연결하기 전 블루투스 신호와\n기기 번호를 확인해 주세요.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(102.dp))

            // 다음 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_next),
                    contentDescription = "다음 버튼",
                    modifier = Modifier.align(Alignment.Center)
                        .clickable {
                            navController.navigate("RegisterDeviceScreen")
                        }
                )
            }
        }
    }
}
