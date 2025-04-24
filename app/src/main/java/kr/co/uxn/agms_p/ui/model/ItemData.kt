package kr.co.uxn.agms_p.ui.model

import java.util.UUID

// eventCode
// 식사 : 1401, 활동 : 1402, 혈당 : 1403, 기타 : 1404
data class ItemData(
    val id: String = UUID.randomUUID().toString(),
    val eventType: Int,
    val time: String,
    val content: String
)
