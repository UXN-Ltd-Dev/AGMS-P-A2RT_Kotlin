package kr.co.uxn.agms_p.ui.components.ready

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@Composable
fun GuideScreen6(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.Start)
                    .padding(start = 30.dp)
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
            // Always 로고
            Image(
                modifier = Modifier.size(130.dp, 50.dp),
                painter = painterResource(R.drawable.always_icon),
                contentDescription = "로고"
            )

            // 사용 설명
            Text(
                text = "사용 설명",
                fontSize = 33.sp,
                color = Color(0xFF385DAB)
            )

            Spacer(modifier = Modifier.size(20.dp))

            // 이미지
            Image(
                painter = painterResource(R.drawable.guide_illustration6),
                contentDescription = "가이드 일러스트6",
                modifier = Modifier.size(230.dp, 300.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "손가락으로 트랜스미터 주위에\n" +
                        "있는 점착패드 주변을 눌러 점착패드가\n" +
                        "피부에 잘 붙도록 합니다.",
                fontSize = 20.sp,
            )

            Spacer(modifier = Modifier.size(32.dp))

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