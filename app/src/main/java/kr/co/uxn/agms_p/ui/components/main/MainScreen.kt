package kr.co.uxn.agms_p.ui.components.main

import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p.AuthEvent
import kr.co.uxn.agms_p.AuthEventNotifier
import kr.co.uxn.agms_p.BleConnectionState
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p.api.token.DataStoreManager
import kr.co.uxn.agms_p.room.AppDatabase
import kr.co.uxn.agms_p.ui.components.main.event.EventScreen
import kr.co.uxn.agms_p.ui.components.main.home.HomeScreen
import kr.co.uxn.agms_p.ui.components.main.past.PastGlucoseScreen
import kr.co.uxn.agms_p.ui.components.main.setting.SettingScreen
import kr.co.uxn.agms_p.ui.model.NavItem
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import kr.co.uxn.agms_p.ui.viewmodel.HomeViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, homeViewModel: HomeViewModel, bleViewModel: BleViewModel, startIndex: Int= 0, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    val navItemList = listOf(
        NavItem(icon = painterResource(id = R.drawable.home_icon), selectedIcon = painterResource(id = R.drawable.home_selected), label = stringResource(R.string.label_home)),
        NavItem(icon = painterResource(id = R.drawable.round_access_time_clicked_24), selectedIcon = painterResource(id = R.drawable.round_access_time_24), label = stringResource(R.string.label_past)),
        NavItem(icon = painterResource(id = R.drawable.event_icon), selectedIcon = painterResource(id = R.drawable.event_selected), label = stringResource(R.string.label_event)),
        NavItem(icon = painterResource(id = R.drawable.setting_icon), selectedIcon = painterResource(id = R.drawable.settings_selected), label = stringResource(R.string.label_setting))
    )

    var selectedIndex by remember { mutableStateOf(startIndex) }

    val bleState by bleViewModel.bleState.collectAsState()

    val blePainter = when (bleState) {
//        BleConnectionState.CONNECTED -> if(isKorean) R.drawable.ble_connected else R.drawable.eng_connected
        BleConnectionState.CONNECTED -> R.drawable.ble_connected
//        BleConnectionState.DISCONNECTED -> if(isKorean) R.drawable.ble_disconnected else R.drawable.eng_disconnected
        BleConnectionState.DISCONNECTED -> R.drawable.ble_disconnected
//        BleConnectionState.CONNECTING -> if(isKorean) R.drawable.ble_connecting else R.drawable.eng_connecting
        BleConnectionState.CONNECTING -> R.drawable.ble_connecting
    }


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
            // 스텝 1 : 'topBar'를 'TopAppBar'로 채워보자.`
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.always_topbar),
                        contentDescription = "Always TopAppBar",
                        modifier = Modifier.size(130.dp, 35.dp)
                            .padding(start = 10.dp)
                    )
                },
                actions = {
//                    if (isKorean) {
                        Image(
                            painter = painterResource(blePainter),
                            contentDescription = "ble 연결상태 아이콘",
                            modifier = Modifier.size(80.dp, 40.dp)
//                            .padding(end = 10.dp)
                        )
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
                                Image(
                                    modifier = Modifier.size(iconSize),
                                    painter = navItem.selectedIcon,
                                    contentDescription = "Navigation Selected Icon",
                                )
                            } else {
                                Image(
                                    modifier = Modifier.size(iconSize),
                                    painter = navItem.icon,
                                    contentDescription = "Navigation Icon",
                                )
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
        ContentScreen(paddingValues = paddingValues, selectedIndex, navController, homeViewModel, bleViewModel, eventScreenViewModel)
    }
}

@Composable
fun ContentScreen(paddingValues: PaddingValues, selectedIndex: Int, navController: NavController, homeViewModel: HomeViewModel, bleViewModel: BleViewModel, eventScreenViewModel: EventScreenViewModel) {
    when(selectedIndex) {
        0 -> {
            HomeScreen(navController, paddingValues, homeViewModel, bleViewModel)
        }
        1 -> {
            PastGlucoseScreen(navController, paddingValues, homeViewModel, bleViewModel)
        }
        2 -> {
            EventScreen(navController, paddingValues, eventScreenViewModel, bleViewModel)
        }
        3 -> {
            SettingScreen(navController, paddingValues, bleViewModel)
        }
    }
}
