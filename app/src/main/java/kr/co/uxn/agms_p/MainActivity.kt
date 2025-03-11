package kr.co.uxn.agms_p

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kr.co.uxn.agms_p.ui.theme.AGMSPTheme
import kr.co.uxn.agms_p.ui.theme.components.login.LoginScreen
import kr.co.uxn.agms_p.ui.theme.components.splash.SplashScreen
import java.security.MessageDigest
import kotlin.text.toByteArray

class MainActivity : ComponentActivity() {
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
//        val spalshScreen = installSplashScreen()
//        spalshScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
//        setTheme(R.style.Theme_AGMSP)
        setContent {
            AGMSPTheme {
                Navigation()
            }
        }
    }


    @Composable
    fun Navigation(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController()
    ) {
        // 단계 3: `NavHost`를 만듭니다.
        // `navController`, `"Home"`, `modifier`를 전달합시다.
        NavHost(navController, "Splash", modifier = modifier) {
            composable("Splash") {
                SplashScreen(navController)
            }
            composable("Login") {
                LoginScreen(kakaoAuthViewModel, navController)
            }

//            navController.navigate("Office") {
//                popUpTo("Home") {
//                    inclusive = true
//                }
//            }

//            composable("Argument/{userId}") { backStackEntry ->
//                val userId = backStackEntry.arguments?.get("userId")
//                Text("userID는 $userId")
//            }
        }
    }
}
