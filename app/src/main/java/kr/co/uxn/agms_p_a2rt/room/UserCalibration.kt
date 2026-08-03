package kr.co.uxn.agms_p_a2rt.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["created_at", "user_id"])
data class UserCalibration(
    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo("glucose_value")
    val glucoseValue: Double,

    @ColumnInfo("created_at")
    val createdAt: String,

    @ColumnInfo("created_at_long")
    val createdAtLong: Long
)
