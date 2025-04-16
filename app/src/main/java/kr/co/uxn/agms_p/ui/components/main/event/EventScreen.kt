package kr.co.uxn.agms_p.ui.components.main.event

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    eventScreenViewModel: EventScreenViewModel
) {
    var interactionSource = remember { MutableInteractionSource() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val eventList by eventScreenViewModel.eventItemList.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // onResume 시점에만 실행!
                // 서버로부터 이벤트 목록 받아와서 화면 갱신해주기
                Log.e("TEST", "이벤트 화면에서 onResume일때 DisposableEffect 실행")

                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val userId = DataStoreManager.getUserId().first() ?: -1
                        val eventList = tokenRetrofit.getEventList(userId)
                        if (eventList.isSuccessful) {
                            val eventListBody = eventList.body()

                            if (eventListBody != null) {
                                Log.e("TEST", "불러온 eventListBody : ${eventListBody}")
                                val items = eventListBody.map { it ->
                                    ItemData(eventType = it.eventTypeCode, time = it.createdAt, content = it.content)
                                }
                                eventScreenViewModel.setItems(items)
                            }
                        } else {
                            Log.e("TEST", "API 에러 : ${eventList.errorBody()}")
                        }
                    } catch (e: Exception) {
                        Log.e("TEST", "네트워크 에러 : ${e.message}")
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        // 1. 생활 등록 카드
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "생활 등록",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "식사와 운동, 인슐린 주입",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = " 등",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF828282),
                    fontSize = 15.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "일상 활동을 기록하세요",
                    color = Color(0xFF828282),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Image(
                    modifier = Modifier
                        .size(70.dp, 26.dp)
                        .padding(bottom = 3.dp)
                        .clickable(
                            // 클릭시 리플효과 제거
//                            interactionSource = interactionSource,
//                            indication = null
                        ) {
                            navController.navigate("ActivityRegisterScreen")
                        },
                    painter = painterResource(R.drawable.enter_icon),
                    contentDescription = "입력 아이콘"
                )
            }
        }

        // 혈당값 입력 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "혈당값 입력",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "최소 ",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF828282),
                )
                Text(
                    text = "1일 1회",
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 15.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "공복혈당을 입력하세요",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF828282),
                    fontSize = 15.sp
                )
                Image(
                    modifier = Modifier
                        .size(70.dp, 26.dp)
                        .padding(bottom = 3.dp)
                        .clickable {
                            navController.navigate("GlucoseRegisterScreen")
                        },
                    painter = painterResource(R.drawable.enter_icon),
                    contentDescription = "입력 아이콘"
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 3. 최근 활동 카드
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "최근 활동",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (eventList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center // 중앙 정렬
                ) {
                    Text(
                        text = "가볍게, 오늘 하루를 남겨보세요",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn {
                    // items : 여러개 삽입
                    items(eventList) { item ->
                        Item(itemData = item)
                    }
                }
            }
        }
    }
}


@Composable
fun Item(itemData: ItemData) {
    Column(
        modifier = Modifier.fillMaxWidth()
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 25.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = itemData.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(4f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = itemData.time,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF828282),
                modifier = Modifier.weight(4f)
            )
            val image = when (itemData.eventType) {
                1401 -> R.drawable.event_meal
                1402 -> R.drawable.event_activity
                1403 -> R.drawable.event_calibration
                else -> R.drawable.event_insulin
            }
            Image(
                modifier = Modifier
                    .size(40.dp)
                    .weight(2f),
                painter = painterResource(image),
                contentDescription = "활동 아이콘"
            )
        }
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}


