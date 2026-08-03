package kr.co.uxn.agms_p_a2rt.ui.components.splash

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kr.co.uxn.agms_p_a2rt.R

@Composable
fun SplashScreen(navController: NavHostController, activity: Activity) {
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(key1 = Unit) {
        systemUiController.setSystemBarsColor(
            color = Color(0xFFFFFFFF),
            darkIcons = true // 상태바 아이콘을 밝게 (흰색)
        )
//        delay(500) // 테스트용
        delay(2000) // 실제
        // 애니메이션 후 네비게이션
        navController.navigate("Login") {
            popUpTo(0) { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
//            painter = painterResource(R.drawable.splash_background), // PNG 파일 리소스,
            painter = painterResource(R.drawable.splash_background_rt), // PNG 파일 리소스,
            contentScale = ContentScale.Crop,
            contentDescription = "배경 이미지"
        )
    }
}
