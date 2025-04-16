package kr.co.uxn.agms_p.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {
    // DB에 데이터 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userValeList: List<UserValue>)

    // userId의 모든 데이터 가져오기
//    @Query("SELECT * FROM user_value WHERE user_id = :userId")
//    fun getListAll(userId: Int): List<UserValue>

    // userId의 lastTime 이후의 데이터 가져오기
    @Query("SELECT * FROM UserValue WHERE uxn_user_id = :userId AND created_at_long > :lastTime")
    fun getListAfterLastTime(userId: Int, lastTime: Long): List<UserValue>

    @Query("DELETE FROM UserValue")
    fun deleteUserValueTable()
}