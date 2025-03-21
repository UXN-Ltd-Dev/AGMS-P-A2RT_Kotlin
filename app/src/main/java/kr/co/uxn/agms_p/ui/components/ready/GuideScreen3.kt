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
fun GuideScreen3(navController: NavController) {
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
                painter = painterResource(R.drawable.guide_illustration3),
                contentDescription = "가이드 일러스트3",
                modifier = Modifier.size(230.dp, 300.dp)
            )

            Spacer(modifier = Modifier.size(20.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "어플리케이터를 삽입하는 피부에\n수평하게 위치시킵니다.\n ",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.size(30.dp))

            // 다음 버튼
            Button(
                onClick = {
                    navController.navigate("GuideScreen4")
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