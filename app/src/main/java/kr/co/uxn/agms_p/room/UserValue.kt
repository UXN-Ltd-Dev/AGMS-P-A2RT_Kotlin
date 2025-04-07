package kr.co.uxn.agms_p.room

import androidx.room.Entity

@Entity(primaryKeys = ["createdAt", "userValueId"])
data class UserValue(
    val userId: Int,
    val userValueId: Int,
    val value: Double,
    val valueType: Int,
    val createdAt: String
)
