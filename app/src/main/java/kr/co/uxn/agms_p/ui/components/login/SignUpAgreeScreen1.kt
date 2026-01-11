package kr.co.uxn.agms_p.ui.components.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kr.co.uxn.agms_p.R

@Composable
fun SignUpAgreeScreen1(navController: NavController, type: Int, oAuthEmail: String) {
    val context = LocalContext.current
    val allChecked = remember { mutableStateOf(false) }
    val checked1 = remember { mutableStateOf(false) }
    val checked2 = remember { mutableStateOf(false) }
    val checked3 = remember { mutableStateOf(false) }
    val checked4 = remember { mutableStateOf(false) }

    val currentLanguage = Locale.current.language
    val isKorean = currentLanguage == "ko"

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(40.dp))

            // 백 버튼
            Image(
                painter = painterResource(R.drawable.back_icon),
                contentDescription = "백 버튼",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Start)
                    .clickable {
                    navController.popBackStack()
                }
            )
            Spacer(modifier = Modifier.size(50.dp))

            // 안녕하세요!
            Text(
                text = stringResource(R.string.sign_up_welcome),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )

            // 계속 진행하시려면..
            Text(
                text = stringResource(R.string.sign_up_agree_guide),
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 10.dp)
            )
            Spacer(modifier = Modifier.size(20.dp))

            // 모두 동의
            Surface(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .size(290.dp, 50.dp)
                    .padding(start = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.background(Color(0xFFEEEEEF))
                        .clickable {
                            allChecked.value = !allChecked.value
                            checked1.value = allChecked.value
                            checked2.value = allChecked.value
                            checked3.value = allChecked.value
                            checked4.value = allChecked.value
                        }
                ) {
                    Checkbox(
                        checked = allChecked.value,
                        onCheckedChange = {
                            allChecked.value = it
                            checked1.value = it
                            checked2.value = it
                            checked3.value = it
                            checked4.value = it
                        } // 바꼈을 때, 처리할 곳
                    )
                    Spacer(modifier = Modifier.size(3.dp))
                    Text(
                        text = stringResource(R.string.sign_up_agree_all)
                    )
                }
            }

            Spacer(modifier = Modifier.size(15.dp))

            // 1. 개별 동의  (만 14세)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(start = 50.dp)
                    .clickable {
                        checked1.value = !checked1.value
                    }
            ) {
                Checkbox(
                    checked = checked1.value,
                    onCheckedChange = { checked1.value = it }, // 바꼈을 때, 처리할 곳
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(R.string.term_item_age_check),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.size(15.dp))

            // 2. 개별 동의 (이용약관)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(start = 50.dp)
                    .clickable {
                        checked2.value = !checked2.value
                    }
            ) {
                Checkbox(
                    checked = checked2.value,
                    onCheckedChange = { checked2.value = it }, // 바꼈을 때, 처리할 곳
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(R.string.term_item_service),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.size(15.dp))

            // 3. 개별 동의 (개인정보 수집 및 이용)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(start = 50.dp)
                    .clickable {
                        checked3.value = !checked3.value
                    }
            ) {
                Checkbox(
                    checked = checked3.value,
                    onCheckedChange = { checked3.value = it }, // 바꼈을 때, 처리할 곳
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(R.string.term_item_privacy),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.size(15.dp))

            // 4. 개별 동의 (서비스 품질 향상)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(start = 50.dp)
                    .clickable {
                        checked4.value = !checked4.value
                    }
            ) {
                Checkbox(
                    checked = checked4.value,
                    onCheckedChange = { checked4.value = it }, // 바꼈을 때, 처리할 곳
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(R.string.term_item_quality),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.size(210.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = if (isKorean) R.drawable.btn_next else R.drawable.btn_eng_continue),
                    contentDescription = "다음 버튼",
                    modifier = Modifier.align(Alignment.Center)
                        .clickable {
                            if (checked1.value == true && checked2.value == true && checked3.value == true) {
                                navController.navigate("SignUpCheckScreen2/${type}/${oAuthEmail}")
                            } else {
                                Toast.makeText(context, R.string.toast_agree_required, Toast.LENGTH_SHORT).show()
                            }
                        }
                )
            }
        }
    }
}


