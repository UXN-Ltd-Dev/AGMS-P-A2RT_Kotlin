package kr.co.uxn.agms_p

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
// import kr.co.uxn.agms_p.databinding.ActivitySplashBinding

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val spalshScreen = installSplashScreen()
        spalshScreen.setKeepOnScreenCondition { false }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        // 흰화면 깜빡임 없애기 위한 코드 실험
        overridePendingTransition(0, 0)
        finish()
    }
}