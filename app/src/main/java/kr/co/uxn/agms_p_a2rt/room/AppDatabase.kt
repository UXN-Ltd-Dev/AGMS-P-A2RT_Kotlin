package kr.co.uxn.agms_p_a2rt.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserValue::class,
        UserGlucose::class,
        UserCalibration::class,
        DummyValue::class,
        SensorParameter::class,
        A2RTData::class,
        GlucoseAlert::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataDao(): RoomDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "roomdb.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}
