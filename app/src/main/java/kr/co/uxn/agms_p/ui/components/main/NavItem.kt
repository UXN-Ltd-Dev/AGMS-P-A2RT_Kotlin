package kr.co.uxn.agms_p.ui.components.main

import androidx.compose.ui.graphics.painter.Painter

data class NavItem(
    val icon: Painter,
    val selectedIcon: Painter,
    val label: String
)
