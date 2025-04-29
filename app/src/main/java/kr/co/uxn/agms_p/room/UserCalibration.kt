package kr.co.uxn.agms_p.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = ["created_at"])
data class UserCalibration(
    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo("glucose_value")
    val glucoseValue: Double,

    @ColumnInfo("created_at")
    val createdAt: String
)
