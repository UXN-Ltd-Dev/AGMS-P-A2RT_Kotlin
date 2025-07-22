package kr.co.uxn.agms_p.ui.components.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.TextStyle

@Composable
fun CustomTimePicker(
    hour: MutableState<String>,
    minute: MutableState<String>,
    isAfternoon: MutableState<Boolean>
) {
    val border = BorderStroke(1.dp, Color.Gray)
    val shape = RoundedCornerShape(size = 4.dp)
    val textStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Row(verticalAlignment = Alignment.Top) {
        // 시간
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = hour.value,
                onValueChange = {
                    if(it.isEmpty()) {
                        hour.value  = it
                    } else if (it.length <= 2 && it.all { ch -> ch.isDigit() } ) {
                        val intValue = it.toIntOrNull() ?: 0
                        if(intValue in 1 .. 12) {
                            hour.value = it
                        }
                    }
                },
                modifier = Modifier
                    .width(70.dp)
                    .height(80.dp),
                textStyle = textStyle,
                singleLine = true,
                shape = shape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Black
                )
            )
            Text("시", fontSize = 12.sp)
        }

        Text(
            text = " : ",
            fontSize = 32.sp,
            modifier = Modifier
                .padding(top = 20.dp)
                .padding(horizontal = 8.dp))

        // 분
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = minute.value,
                onValueChange = {
                    if (it.length <= 2 && it.all { ch -> ch.isDigit() }) {
                        minute.value = it
                    }
                },
                modifier = Modifier
                    .width(70.dp)
                    .height(80.dp),
                textStyle = textStyle,
                singleLine = true,
                shape = shape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Black
                )
            )
            Text("분", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 오전/오후 토글
        Column(
            modifier = Modifier
                .width(60.dp)
                .height(80.dp)
                .border(border, shape),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (!isAfternoon.value) Color.LightGray else Color.Transparent)
                    .clickable { isAfternoon.value = false },
                contentAlignment = Alignment.Center
            ) {
                Text("오전", fontSize = 14.sp)
            }
            Divider(color = Color.Gray, thickness = 1.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (isAfternoon.value) Color.LightGray else Color.Transparent)
                    .clickable { isAfternoon.value = true },
                contentAlignment = Alignment.Center
            ) {
                Text("오후", fontSize = 14.sp)
            }
        }
    }
}