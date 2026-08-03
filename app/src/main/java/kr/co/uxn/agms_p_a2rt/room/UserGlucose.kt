package kr.co.uxn.agms_p_a2rt.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["created_at"])
data class UserGlucose(
    @ColumnInfo(name = "uxn_user_id")
    val userId: Int,
    @ColumnInfo(name = "glucose")
    val glucose: Double,
    @ColumnInfo(name = "w1_current")
    val weo1: Double,
    @ColumnInfo(name = "w2_current")
    val weo2: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "created_at_long")
    val createdAtLong: Long
)
