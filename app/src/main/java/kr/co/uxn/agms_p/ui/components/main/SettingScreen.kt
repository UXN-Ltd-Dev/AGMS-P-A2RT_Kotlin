package kr.co.uxn.agms_p.ui.components.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, paddingValues: PaddingValues) {
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = Color.Red
//    ) {}

    val items = listOf(
        "내 정보",
        "알림 설정",
        "이용 약관",
        "개인 정보 처리 방침",
        "로그아웃",
        "계정 삭제"
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xF7F7FB))) {
        Divider()
        items.forEachIndexed { index, item ->
            SettingItem(item)

//            Divider(color = Color.Transparent, thickness = 1.dp)
            if (index == 0 || index == 1 || index == 3) {
                Divider(color = Color.Transparent, thickness = 1.dp)
            }
            else {
                Divider(color = Color.Transparent, thickness = 20.dp)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }

}

@Composable
fun SettingItem(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = Color.LightGray)
            .clickable { /* TODO */ }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = title, fontSize = 16.sp)
    }
}