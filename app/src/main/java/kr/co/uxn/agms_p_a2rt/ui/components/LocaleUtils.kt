package kr.co.uxn.agms_p_a2rt.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun isKorea(): Boolean {
    val locale = LocalConfiguration.current.locales[0]
    return locale.language == "ko"
}
