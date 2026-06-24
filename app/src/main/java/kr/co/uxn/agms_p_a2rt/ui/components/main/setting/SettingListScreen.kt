package kr.co.uxn.agms_p_a2rt.ui.components.main.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// 테마 컬러
val BackgroundGray = Color(0xFFF2F2F2)
val TextGray = Color(0xFF888888)
val PrimaryOrange = Color(0xFFFCA937)
val AlertRed = Color(0xFFFF5252)

// 1. 네비게이션 호스트
@Composable
fun SettingListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "settings_main",
        modifier = modifier.fillMaxSize().background(BackgroundGray)
    ) {
        composable("settings_main") { SettingsMainScreen(navController) }
        composable("user_info") { UserInfoScreen(navController) }
        composable("sensor_info") { SensorInfoScreen(navController) }
        composable("alarm_settings") { AlarmSettingsScreen(navController) }
        composable("app_info") { AppInfoScreen(navController) }
    }
}

// ==========================================
// [첫 번째 화면] 설정 메인 화면
// ==========================================
@Composable
fun SettingsMainScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 사용자 프로필 영역 (흰색 배경)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text("김민준", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("계정: mjkim@naver.com", fontSize = 14.sp, color = TextGray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 메뉴 리스트 영역 (회색 배경)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 사용자 정보 & 센서 정보 (하나의 카드로 묶음)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    SettingsMenuRow("사용자 정보") { navController.navigate("user_info") }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = BackgroundGray
                    )
                    SettingsMenuRow("센서 정보") { navController.navigate("sensor_info") }
                }
            }

            // 2. 단일 메뉴 카드들
            SettingsSingleCard("알림 설정") { navController.navigate("alarm_settings") }
            SettingsSingleCard("사용 설명서") { /* 설명서 화면 이동 로직 */ }
            SettingsSingleCard("앱 정보") { navController.navigate("app_info") }

            // 3. 로그아웃 (빨간색 텍스트)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* 로그아웃 로직 */ }
            ) {
                Text(
                    text = "로그아웃",
                    color = AlertRed,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// 메뉴 공통 컴포넌트
@Composable
fun SettingsSingleCard(title: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsMenuRow(title = title, onClick = onClick)
    }
}

@Composable
fun SettingsMenuRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp)
    }
}

// ==========================================
// 공통 서브 화면 헤더 (뒤로가기 + 타이틀)
// ==========================================
@Composable
fun SubScreenHeader(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(8.dp)
        )
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(40.dp)) // 타이틀 중앙 정렬을 위한 여백
    }
}

// ==========================================
// [두 번째 화면] 사용자 정보
// ==========================================
@Composable
fun UserInfoScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SubScreenHeader("사용자 정보", onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("기본정보", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            InfoRow("이름", "김민준")
            InfoRow("이메일", "mjkim@naver.com")
            Text("비밀번호 변경하기", fontSize = 16.sp, modifier = Modifier.padding(vertical = 12.dp).clickable { })

            Spacer(modifier = Modifier.height(24.dp))

            Text("부가정보", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            InfoRow("성별", "")
            InfoRow("연령", "")
            InfoRow("신장", "")
            InfoRow("체중", "")
            InfoRow("당뇨 유형", "")
            InfoRow("목표 혈당 범위", "")
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("일반적인 목표 혈당범위는 80~130 mg/dL이며, 식후 최대 혈당은 180 mg/dL미만입니다.", fontSize = 12.sp, color = TextGray)

            Spacer(modifier = Modifier.height(32.dp))
            Text("회원탈퇴", color = AlertRed, fontSize = 14.sp, modifier = Modifier.clickable { })
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// [세 번째 화면] 센서 정보
// ==========================================
@Composable
fun SensorInfoScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("센서 정보", onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            InfoRow("센서 시작일", "2025년 03월 18일")
            InfoRow("남은 사용 기간", "13일")
            InfoRow("시리얼 번호", "342")
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("센서종료", color = AlertRed, fontSize = 16.sp, modifier = Modifier.clickable { })
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp)
        Text(value, fontSize = 16.sp, color = TextGray)
    }
}

// ==========================================
// [네 번째 화면] 알림 설정
// ==========================================
@Composable
fun AlarmSettingsScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SubScreenHeader("알림 설정", onBackClick = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            AlarmSwitchRow("무음 모드", true)
            AlarmSwitchRow("저혈당 알림", false, "70 mg/dL")
            AlarmSwitchRow("고혈당 알림", false, "170 mg/dL")
            AlarmSwitchRow("신호 소실", true)
            AlarmSwitchRow("센서 만료", true)
            AlarmSwitchRow("센서 안정화", true)
        }
    }
}

@Composable
fun AlarmSwitchRow(title: String, defaultChecked: Boolean, subValue: String? = null) {
    var checked by remember { mutableStateOf(defaultChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontSize = 16.sp)
            if (subValue != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(subValue, fontSize = 14.sp, color = TextGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextGray, modifier = Modifier.size(14.dp))
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// ==========================================
// [다섯 번째 화면] 앱 정보
// ==========================================
@Composable
fun AppInfoScreen(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        SubScreenHeader("앱 정보", onBackClick = { navController.popBackStack() })

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // 로고 대체 텍스트 (실제 앱에서는 Image 컴포저블 사용)
            Text("Always RT", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("버전 0.3.5", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("현재 최신버전입니다.", fontSize = 12.sp, color = TextGray)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("의료기기 허가정보", fontSize = 14.sp, modifier = Modifier.clickable { })
                Text("서비스 이용약관", fontSize = 14.sp, modifier = Modifier.clickable { })
                Text("개인정보 처리방침", fontSize = 14.sp, modifier = Modifier.clickable { })
                Text("앱 접근권한 안내", fontSize = 14.sp, modifier = Modifier.clickable { })
            }
        }
    }
}