package kr.co.uxn.agms_p.ui.components.ready

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.api.token.DataStoreManager

@Composable
fun StabilizationCompleteScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        DataStoreManager.deleteRoute()
        DataStoreManager.saveRoute("StabilizationCompleteScreen")
        val route = DataStoreManager.getRoute().first()
        Log.e("TEST", "안정화 완료 화면에서 Route : ${route}")
    }
    val coroutine = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.size(280.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "센서 안정화가",
                fontSize = 30.sp,
//                fontWeight = FontWeight.Bold,
                color = Color(0xFF385DAB)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "완료되었습니다.",
                fontSize = 30.sp,
//                fontWeight = FontWeight.Bold,
                color = Color(0xFF385DAB)
            )


            Spacer(modifier = Modifier.height(260.dp))

            // 다음 버튼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.btn_next),
                    contentDescription = "다음 버튼",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            navController.navigate("EnterFirstGlucose")

                            // 메인화면으로 고정 isMain = true
                            coroutine.launch(Dispatchers.IO) {
                                DataStoreManager.deleteRoute()
                                DataStoreManager.saveRoute("MainScreen/0")
                                DataStoreManager.saveIsMain(true)
                                val isMain = DataStoreManager.getIsMain().first()
                                val route = DataStoreManager.getRoute().first()
                                Log.e("TEST", "안정화 화면에서 버튼 눌럿을시isMain : ${isMain}")
                                Log.e("TEST", "안정화 화면에서 버튼 눌럿을시 Route : ${route}")
                            }
                        }
                )
            }
        }
    }
}