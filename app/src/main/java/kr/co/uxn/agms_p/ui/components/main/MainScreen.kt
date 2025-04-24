package kr.co.uxn.agms_p.ui.components.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.BleConnectionState
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.components.main.event.EventScreen
import kr.co.uxn.agms_p.ui.components.main.home.HomeScreen
import kr.co.uxn.agms_p.ui.components.main.setting.SettingScreen
import kr.co.uxn.agms_p.ui.model.NavItem
import kr.co.uxn.agms_p.ui.viewmodel.BleViewModel
import kr.co.uxn.agms_p.ui.viewmodel.EventScreenViewModel
import kr.co.uxn.agms_p.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, homeViewModel: HomeViewModel, bleViewModel: BleViewModel, startIndex: Int= 0, eventScreenViewModel: EventScreenViewModel) {
    val context = LocalContext.current

    val navItemList = listOf(
        NavItem(icon = painterResource(id = R.drawable.home_icon), selectedIcon = painterResource(id = R.drawable.home_selected), label = "홈"),
        NavItem(icon = painterResource(id = R.drawable.event_icon), selectedIcon = painterResource(id = R.drawable.event_selected), label = "이벤트"),
        NavItem(icon = painterResource(id = R.drawable.setting_icon), selectedIcon = painterResource(id = R.drawable.settings_selected), label = "설정")
    )

    var selectedIndex by remember { mutableStateOf(startIndex) }

    val bleState by bleViewModel.bleState.collectAsState()

    val blePainter = when (bleState) {
        BleConnectionState.CONNECTED -> R.drawable.ble_connected
        BleConnectionState.DISCONNECTED -> R.drawable.ble_disconnected
        BleConnectionState.CONNECTING -> R.drawable.ble_connecting
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                    Image(
                        painter = painterResource(blePainter),
                        contentDescription = "ble 연결상태 아이콘",
                        modifier = Modifier.size(80.dp, 40.dp)
//                            .padding(end = 10.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(60.dp),
                containerColor = Color.White
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            if(selectedIndex == index) {
                                Image(
                                    modifier = Modifier.size(20.dp),
                                    painter = navItem.selectedIcon,
                                    contentDescription = "Navigation Selected Icon",
                                )
                            } else {
                                Image(
                                    modifier = Modifier.size(20.dp),
                                    painter = navItem.icon,
                                    contentDescription = "Navigation Icon",
                                )
                            }

                        },
                        label = {
                            Text(
                                text = navItem.label,
                                fontSize = 12.sp
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
        0 -> HomeScreen(navController, paddingValues, homeViewModel, bleViewModel)
        1 -> EventScreen(navController, paddingValues, eventScreenViewModel)
        2 -> SettingScreen(navController, paddingValues, bleViewModel)
    }
}
