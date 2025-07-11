package kr.co.uxn.agms_p.ui.components.main.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionInfoScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            modifier = Modifier.clickable {
                                navController.navigate("MainScreen/${2}") {
                                    popUpTo("MainScreen/{startIndex}") { inclusive = true }
                                }
                            }
                        )
                    }
                },
                title = {
                    Text(
                        text = "버전 정보",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF2F3F9))
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider()

                Spacer(modifier = Modifier.height(screenHeight * 0.15f)) // 10% 여백

                Image(
                    painter = painterResource(id = R.drawable.agms_version_icon),
                    contentDescription = "앱 아이콘",
                    modifier = Modifier.size(250.dp)
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Text(
                    text = "버전 ${versionName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.1f))

                Text(
                    text = "Copyright 2025 UXN Co., Ltd.\nAll Rights Reserved",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                Text(
                    text = "Powered by AGMS",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } // Scaffold
}
