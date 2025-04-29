package kr.co.uxn.agms_p.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DummyValue(
//    @ColumnInfo(name = "uxn_user_id")
//    val userId: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,

    @ColumnInfo(name = "we_current")
    val weCurrent: Double,

    @ColumnInfo(name = "ae_current")
    val aeCurrent: Double,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

)
