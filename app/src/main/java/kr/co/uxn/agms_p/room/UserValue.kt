package kr.co.uxn.agms_p.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["created_at", "user_value_id"])
data class UserValue(
    @ColumnInfo(name = "uxn_user_id")
    val userId: Int,
    @ColumnInfo(name = "user_value_id")
    val userValueId: Int,
    @ColumnInfo(name = "value")
    val value: Double,
    @ColumnInfo(name = "value_type")
    val valueType: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "created_at_long")
    val createdAtLong: Long
)
