package kr.co.uxn.agms_p.ui.components.main

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, paddingValues: PaddingValues, homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    val day by homeViewModel.day.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 시연용 타이머
    LaunchedEffect(Unit) {
        homeViewModel.startTimerForTest()
        Log.e("TEST", "홈 화면에서 타이머 실행")
    }

    // 실제 타이머
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                // onResume 시점에만 실행!
//                homeViewModel.startTimer()
//                Log.e("TEST", "홈 화면에서 타이머 실행")
//            }
//        }
//
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }

    // 혈당 표시 카드
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF385DAB), // 카드 배경색 설정
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "현재 혈당",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 20.dp)
                )

                Image(
                    modifier = Modifier
                        .align(Alignment.CenterEnd) // ✅ 이미지는 오른쪽 끝
                        .padding(end = 20.dp)
                        .size(28.dp),
                    painter = painterResource(R.drawable.glucose_reset),
                    contentDescription = "glucoseReset"
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(modifier = Modifier.width(18.dp))
                Text(
                    text = "110",
                    color = Color.White,
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "mg/dL",
                    color = Color.White,
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.width(40.dp))
                Image(
                    modifier = Modifier
                        .size(70.dp)
                        .padding(top = 10.dp),
                    painter = painterResource(R.drawable.glucose_down),
                    contentDescription = "glucose_down"
                )
            }
        }


        // 그래프 표시 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White, // 카드 배경색 설정
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "일일 그래프",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(start = 20.dp)
            )

            Image(
                painter = painterResource(R.drawable.fraud),
                contentDescription = "그래프 샘플 이미지",
                modifier = Modifier.size(350.dp, 200.dp)
                    .padding(horizontal = 20.dp)
            )



        }

        // 센서 정보 표시 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White, // 카드 배경색 설정
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 15.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "센서 정보",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "더보기 아이콘",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                        Toast.makeText(context, "클릭됨", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 10) {
                    val painter = if (i < 10 - day + 1 ) {
                        R.drawable.sensor_progress_on
                    } else {
                        R.drawable.sensor_progress_off
                    }
                    Image(
                        painter = painterResource(painter),
                        contentDescription = "센서 진행률",
                        modifier = Modifier.size(25.dp, 10.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${10 - day + 1}/10일",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                if (day - 1 != 0) {
                    Text(
                        text = "${day - 1}일 남았어요",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "마지막 날이에요",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
