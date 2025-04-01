package kr.co.uxn.agms_p.ui.components.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            // 스텝 1 : 'topBar'를 'TopAppBar'로 채워보자.`
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.always_topbar),
                        contentDescription = "Always TopAppBar",
                        modifier = Modifier.size(150.dp, 40.dp)
                    )
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.ble_connected),
                        contentDescription = "ble 연결완료 아이콘",
                        modifier = Modifier.size(80.dp, 40.dp)
                    )
                }
            )
        },

        bottomBar = {



        }

    ) { paddingValues ->
        Column (
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
        ) {

        }
    }
}