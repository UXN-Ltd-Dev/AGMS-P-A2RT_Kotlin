package kr.co.uxn.agms_p.ui.components.main

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(navController: NavController, paddingValues: PaddingValues) {
    var interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        // 1. 생활 등록 카드
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.size(70.dp, 26.dp)
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.size(70.dp, 26.dp)
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
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "최근 활동",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            LazyColumn { // Lazy : 필요할때만 보여주겠다.
                // 스텝 2 : items(itemList)를 이용해 Item을 반복해서 컬럼에 추가하자.
                // item : 한개씩,
                // items : 여러걔 삽입


                item {
                    Item(
                        ItemData(imageId = R.drawable.event_activity, eventType = 1402, time = "2025.04.11 09:04", content = "컨텐트")
                    )
                }
                item {
                    Item(
                        ItemData(imageId = R.drawable.event_meal, eventType = 1401, time = "2025.04.11 09:03", content = "컨텐트")
                    )
                }
                item {
                    Item(
                        ItemData(imageId = R.drawable.event_activity, eventType = 1402, time = "2025.04.11 10:04", content = "컨텐트")
                    )
                }

                item {
                    Item(
                        ItemData(imageId = R.drawable.event_meal, eventType = 1401, time = "2025.04.11 09:03", content = "컨텐트")
                    )
                }
                item {
                    Item(
                        ItemData(imageId = R.drawable.event_activity, eventType = 1402, time = "2025.04.11 10:04", content = "컨텐트")
                    )
                }

                item {
                    Item(
                        ItemData(imageId = R.drawable.event_meal, eventType = 1401, time = "2025.04.11 09:03", content = "컨텐트")
                    )
                }
                item {
                    Item(
                        ItemData(imageId = R.drawable.event_activity, eventType = 1402, time = "2025.04.11 10:04", content = "컨텐트")
                    )
                }
                item {
                    Item(
                        ItemData(imageId = R.drawable.event_meal, eventType = 1401, time = "2025.04.11 09:03", content = "컨텐트")
                    )
                }
                item {
                    Item(
                        ItemData(imageId = R.drawable.event_activity, eventType = 1402, time = "2025.04.11 10:04", content = "컨텐트")
                    )
                }
//                item {
//                    Item(itemList[1])
//                }


//                items(itemList) { item ->
//                    Item(item)
//                }
            }


        }
    }
}


@Composable
fun Item(itemData: ItemData) {
    androidx.compose.material.Card(
        elevation = 8.dp,
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = itemData.content)
            Text(
                text = itemData.time,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF828282),
                )
            Image(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = itemData.imageId),
                contentDescription = "활동 아이콘"
            )
        }
    }
}

// eventCode
// 식사 : 1401, 활동 : 1402, 혈당 : 1403, 기타 : 1404
data class ItemData(
    @DrawableRes val imageId: Int,
    val eventType: Int,
    val time: String,
    val content: String
)
