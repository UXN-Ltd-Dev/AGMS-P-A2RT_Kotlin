package kr.co.uxn.agms_p.ui.theme.components.splash

import androidx.browser.trusted.splashscreens.SplashScreenParamKey
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kr.co.uxn.agms_p.R
import kr.co.uxn.agms_p.ui.theme.AGMSPTheme

@Composable
fun SplashScreen(navController: NavHostController) {

    val alpha = remember {
        Animatable(0f)
    }

    LaunchedEffect(key1 = Unit) {
//        alpha.animateTo(
//            targetValue = 1f,
//            animationSpec = tween(1500)
//        )
        delay(2500L)
        navController.navigate("Login")
    }


    Surface(
        modifier = Modifier.fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(160.dp),
                painter = painterResource(R.drawable.uxn_logo),
                contentDescription = "로고"
            )
        }
    }
}

//@Preview
//@Composable
//fun SplashScreenPreview() {
//    AGMSPTheme{
//        SplashScreen()
//    }
//}