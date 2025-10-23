package kr.co.uxn.agms_p.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["created_at"])
data class UserValue(
    @ColumnInfo(name = "uxn_user_id")
    val userId: Int,
    @ColumnInfo(name = "we_current")
    val weCurrent: Double,
    @ColumnInfo(name = "ae_current")
    val aeCurrent: Double,
    @ColumnInfo(name = "temperature")
    val temperature: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "created_at_long")
    val createdAtLong: Long
)
