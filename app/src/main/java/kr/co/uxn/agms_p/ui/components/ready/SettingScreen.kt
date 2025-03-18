package kr.co.uxn.agms_p.ui.components.ready

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@Composable
fun SettingScreen(navController: NavController) {

    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(100.dp))
            Image(
                modifier = Modifier.size(150.dp),
                painter = painterResource(R.drawable.iphone),
                contentDescription = "휴대폰"
            )
            Spacer(modifier = Modifier.size(40.dp))

            Text("환영합니다.")
            Text("Agms의 다양한 서비스를 위해")
            Text("몇 가지 설정이 추가로 필요합니다.")

            Spacer(modifier = Modifier.size(200.dp))
            Button(
                onClick = {
                    navController.navigate("SettingPermissionScreen")
                },
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                )
            ) {
                Text(text = "시작하기")
            }
        }
    }
}
