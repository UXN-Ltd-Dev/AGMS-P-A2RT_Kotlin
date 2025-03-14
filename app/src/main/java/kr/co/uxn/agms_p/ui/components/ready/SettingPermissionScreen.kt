package kr.co.uxn.agms_p.ui.components.ready

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@Composable
fun SettingPermissionScreen(navController: NavController) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                modifier = Modifier.size(150.dp),
                painter = painterResource(R.drawable.ble_icon),
                contentDescription = "ble 아이콘"
            )
            Text(
                text = "블루투스",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text("환영합니다.")
            Text("Agms의 다양한 서비스를 위해")
            Text("몇 가지 설정이 추가로 필요합니다.")
            Button(
                onClick = {navController.navigate("EnterInfoScreen")}
            ) {
                Text("다음")
            }
        }
    }
}