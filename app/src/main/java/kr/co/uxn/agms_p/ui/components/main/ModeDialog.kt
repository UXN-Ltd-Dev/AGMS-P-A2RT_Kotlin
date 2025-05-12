package kr.co.uxn.agms_p.ui.components.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ModeDialog(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedOption) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 24.dp),
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentSelection = option }
                    ) {
                        RadioButton(
                            selected = (option == currentSelection),
                            onClick = { currentSelection = option }
                        )
                        Text(
                            text = option,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Text(
                text = "확인",
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onOptionSelected(currentSelection)
                        onDismissRequest()
                    }
            )
        },
        dismissButton = {
            Text(
                text = "취소",
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onDismissRequest()
                    }
            )
        }
    )
}