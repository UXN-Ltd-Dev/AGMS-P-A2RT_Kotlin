package kr.co.uxn.agms_p_a2rt.ui.components.main

import EventListScreen
import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kr.co.uxn.agms_p_a2rt.AuthEvent
import kr.co.uxn.agms_p_a2rt.AuthEventNotifier
import kr.co.uxn.agms_p_a2rt.BleConnectionState
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.room.AppDatabase
import kr.co.uxn.agms_p_a2rt.ui.components.main.home.HomeScreen
import kr.co.uxn.agms_p_a2rt.ui.components.main.notification.AlertHistoryScreen
import kr.co.uxn.agms_p_a2rt.ui.components.main.setting.SettingListScreen
import kr.co.uxn.agms_p_a2rt.ui.components.main.statistics.AnalysisScreen
import kr.co.uxn.agms_p_a2rt.ui.model.NavItem
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.EventScreenViewModel
import kr.co.uxn.agms_p_a2rt.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    bleViewModel: BleViewModel,
    startIndex: Int = 0,
    eventScreenViewModel: EventScreenViewModel,
    eventStartCategory: String? = null,
    eventRecordTimeMillis: Long? = null
) {
    val context = LocalContext.current

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    val navItemList = listOf(
        NavItem(icon = painterResource(id = R.drawable.home_unselected), selectedIcon = painterResource(id = R.drawable.home_selected), label = stringResource(R.string.label_home)),
        NavItem(icon = painterResource(id = R.drawable.statistics_unselected), selectedIcon = painterResource(id = R.drawable.statistics_selected), label = stringResource(R.string.label_past)),
        NavItem(icon = painterResource(id = R.drawable.event_unselected), selectedIcon = painterResource(id = R.drawable.event_selected), label = stringResource(R.string.label_event)),
        NavItem(icon = painterResource(id = R.drawable.notification_unselected), selectedIcon = painterResource(id = R.drawable.notification_selected), label = stringResource(R.string.label_notification)),
        NavItem(icon = painterResource(id = R.drawable.setting_unselected), selectedIcon = painterResource(id = R.drawable.settings_selected), label = stringResource(R.string.label_setting))
    )

    var selectedIndex by remember { mutableStateOf(startIndex) }

    val bleState by bleViewModel.bleState.collectAsState()

    val showDuplicateLoginSessionOffDialog = remember { mutableStateOf(false) }
    val localDbRepository by lazy {
        AppDatabase.getInstance(context)
    }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val iconSize = when {
        screenHeightDp == 783 -> 22.dp // a시리즈
        else -> 21.dp
    }

    val navHeight = when {
        screenHeightDp == 783 -> 70.dp // a시리즈
        else -> 65.dp
    }

    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        // 앱이 포그라운드(화면에 보임) 상태일 때만 블록 실행, 백그라운드 가면 자동 중지
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            AuthEventNotifier.eventFlow.collect { event ->
                when (event) {
                    AuthEvent.DUPLICATE_LOGIN -> {
                        showDuplicateLoginSessionOffDialog.value = true
                    }
                }
            }
        }
    }

//    if (showDuplicateLoginSessionOffDialog.value) {
//        NotiDialog(
//            onDismiss = {
//                showDuplicateLoginSessionOffDialog.value = false
//            },
//            onConfirm = {
//                showDuplicateLoginSessionOffDialog.value = false
//
//                coroutineScope.launch(Dispatchers.IO) {
//                    val userId = DataStoreManager.getUserId().first() ?: -1
//                    try {
//                        // userId의 db삭제
//                        localDbRepository?.dataDao()?.deleteUserValueTable(userId)
//                        localDbRepository?.dataDao()?.deleteUserGlucoseTable(userId)
//                        localDbRepository?.dataDao()?.deleteUserCalibrationTable(userId)
//
//                        DataStoreManager.saveIsMain(false)
//                        DataStoreManager.deleteRoute()
//                        DataStoreManager.saveRoute("Splash")
//                        Log.e("TEST", "${DataStoreManager.getIsMain().first()}")
//                        DataStoreManager.deleteAccessToken()
//                        DataStoreManager.deleteRefreshToken()
//                        DataStoreManager.deleteUserId()
//                        DataStoreManager.deleteDeviceMac()
////                                    DataStoreManager.deleteDeviceMac()
//                        DataStoreManager.setNotiHighGlucose(false)
//                        DataStoreManager.setNotiLowGlucose(false)
//                        DataStoreManager.deleteStartTime()
//                        DataStoreManager.deleteEndTime()
//                        DataStoreManager.deleteDailyCalibrationTime()
//                        DataStoreManager.deleteDailyCalibrationLastTime()
//                        DataStoreManager.setLandScapeMode(false)
//                        DataStoreManager.deleteTargetLowGlucose()
//                        DataStoreManager.deleteTargetHighGlucose()
//                        DataStoreManager.deleteEmail()
//                        withContext(Dispatchers.Main) {
//                            // 1. 서비스 종료
//                            bleViewModel.emit("STOP_SERVICE")
//                            // 앱 강제 종료
//                            android.os.Process.killProcess(android.os.Process.myPid())
//                            exitProcess(0)
//                        }
//                    } catch (e: Exception) {
//                        Log.e("TEST", "중복로그인 다이얼로그 confirm 에러 : ${e.message}")
//                    }
//                }
//            },
//            title = context.getString(R.string.dialog_detect_other_login_title),
//            content = context.getString(R.string.dialog_detect_other_login_content)
//        )
//    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.a2rt_topbar),
                        contentDescription = "Always TopAppBar",
                        modifier = Modifier.size(130.dp, 35.dp)
                            .padding(start = 10.dp)
                    )
                },
                actions = {
//                    if (isKorean) {

                    if (bleState == BleConnectionState.CONNECTED) {
                        BleStatusBadge(BleStatus.CONNECTED)
                    } else if (bleState == BleConnectionState.CONNECTING) {
                        BleStatusBadge(BleStatus.CONNECTING)
                    } else {
                        BleStatusBadge(BleStatus.DISCONNECTED)
                    }
//                        Image(
//                            painter = painterResource(blePainter),
//                            contentDescription = "ble 연결상태 아이콘",
//                            modifier = Modifier.size(80.dp, 40.dp)
////                            .padding(end = 10.dp)
//                        )




//                    } else {
//                        Image(
//                            painter = painterResource(blePainter),
//                            contentDescription = "ble 연결상태 아이콘",
//                            modifier = Modifier.size(110.dp, 40.dp)
////                            .padding(end = 10.dp)
//                        )
//                    }

                    Spacer(modifier = Modifier.width(10.dp))
                },
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(navHeight),
                containerColor = Color.White
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
//                            selectedIndex = index
                            navController.navigate("MainScreen/$index") {
//                                launchSingleTop = true
                                popUpTo("MainScreen/{startIndex}") { inclusive = true }
                            }
                        },
                        icon = {
                            if(selectedIndex == index) {
                                // 설정 아이콘이 작게나와서 설정일경우에만 25로 크기 키움
                                if (index == navItemList.lastIndex) {
                                    Image(
                                        modifier = Modifier.size(25.dp),
                                        painter = navItem.selectedIcon,
                                        contentDescription = "Navigation Selected Icon",
                                    )
                                } else {
                                    Image(
                                        modifier = Modifier.size(iconSize),
                                        painter = navItem.selectedIcon,
                                        contentDescription = "Navigation Selected Icon",
                                    )
                                }
                            } else {
                                // 설정 아이콘이 작게나와서 설정일경우에만 25로 크기 키움
                                if (index == navItemList.lastIndex) {
                                    Image(
                                        modifier = Modifier.size(25.dp),
                                        painter = navItem.icon,
                                        contentDescription = "Navigation Icon",
                                    )
                                } else {
                                    Image(
                                        modifier = Modifier.size(iconSize),
                                        painter = navItem.icon,
                                        contentDescription = "Navigation Icon",
                                    )
                                }
                            }

                        },
                        label = {
                            Text(
                                text = navItem.label,
                                fontSize = 13.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        ContentScreen(
            paddingValues = paddingValues,
            selectedIndex = selectedIndex,
            navController = navController,
            homeViewModel = homeViewModel,
            bleViewModel = bleViewModel,
            eventScreenViewModel = eventScreenViewModel,
            eventStartCategory = eventStartCategory,
            eventRecordTimeMillis = eventRecordTimeMillis
        )
    }
}

@Composable
fun ContentScreen(
    paddingValues: PaddingValues,
    selectedIndex: Int,
    navController: NavController,
    homeViewModel: HomeViewModel,
    bleViewModel: BleViewModel,
    eventScreenViewModel: EventScreenViewModel,
    eventStartCategory: String? = null,
    eventRecordTimeMillis: Long? = null
) {
    when(selectedIndex) {
        0 -> {
            HomeScreen(navController, paddingValues, homeViewModel, bleViewModel)
        }
        1 -> {
//            PastGlucoseScreen(navController, paddingValues, homeViewModel, bleViewModel)
            AnalysisScreen(
                paddingValues = paddingValues,
                onBloodSugarRecordClick = { recordTimeMillis ->
                    navController.navigate("MainScreen/2/select/$recordTimeMillis") {
                        popUpTo("MainScreen/{startIndex}") { inclusive = true }
                    }
                }
            )
        }
        2 -> {
            EventListScreen(
                modifier = Modifier.padding(paddingValues),
                startDestination = eventStartCategory ?: "list",
                eventScreenViewModel = eventScreenViewModel,
                initialRecordTimeMillis = eventRecordTimeMillis
            )
        }
        3 -> {
            AlertHistoryScreen(modifier = Modifier.padding(paddingValues))
        }
        4 -> {
//            SettingScreen(navController, paddingValues, bleViewModel)
            SettingListScreen(
                modifier = Modifier.padding(paddingValues),
                bleViewModel = bleViewModel,
                onSensorEnded = {
                    navController.navigate("MainScreen/0") {
                        popUpTo("MainScreen/{startIndex}") { inclusive = true }
                    }
                }
            )
        }
    }
}


enum class BleStatus(@StringRes val textResId: Int, val dotColor: Color) {
    CONNECTED(R.string.ble_connection_state_connected, Color(0xFF6FCF97)),      // 초록색 도트
    CONNECTING(R.string.ble_connection_state_connecting, Color(0xFFF2994A)),  // 주황색 도트
    DISCONNECTED(R.string.ble_connection_state_disconnected, Color(0xFFEB5757))   // 빨간색 도트
}

// 2. 뱃지 UI 컴포저블
@Composable
fun BleStatusBadge(status: BleStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "bleStatusBlink")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_250),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bleStatusDotAlpha"
    )

    Row(
        modifier = Modifier
//            .width(120.dp)
            .background(
                color = Color(0xFF8C8C8C), // 동일한 회색 배경
                shape = RoundedCornerShape(50) // 알약 형태의 둥근 모서리
            )
            .padding(horizontal = 15.dp, vertical = 5.dp), // 내부 여백 (가로세로 크기 조정)
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // 앞부분 색상 도트 (동그라미)
        Box(
            modifier = Modifier
                .size(15.dp)
                .alpha(dotAlpha)
                .background(color = status.dotColor, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(10.dp)) // 도트와 텍스트 사이 간격

        // 상태 텍스트
        Text(
            text = stringResource(status.textResId),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
