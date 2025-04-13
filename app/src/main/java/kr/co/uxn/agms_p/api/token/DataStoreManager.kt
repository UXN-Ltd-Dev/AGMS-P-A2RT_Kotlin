package kr.co.uxn.agms_p.api.token

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.uxn.agms_p.ble.Device

object DataStoreManager {
    private lateinit var dataStore: DataStore<Preferences>
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val EMAIL = stringPreferencesKey("email")
    private val USER_ID = intPreferencesKey("user_id")
    private val START_TIME = longPreferencesKey("start_time")
    private val MEASUREMENT_TIME = longPreferencesKey("measurement_time")
    private val END_TIME = longPreferencesKey("end_time")
    private val DEVICE_MAC = stringPreferencesKey("device_mac")

    fun init(context: Context) {
        dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("token_prefs")
        }
    }

    fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN_KEY]
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }
    }

    fun getEmail(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[EMAIL]
        }
    }

    fun getUserId(): Flow<Int?> {
        return dataStore.data.map { prefs ->
            prefs[USER_ID]
        }
    }

    fun getDeviceMac(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[DEVICE_MAC]
        }
    }

    fun getStartTime(): Flow<Long?> {
        return dataStore.data.map { prefs ->
            prefs[START_TIME]
        }
    }

    fun getMeasurementTime(): Flow<Long?> {
        return dataStore.data.map { prefs ->
            prefs[MEASUREMENT_TIME]
        }
    }

    fun getEndTime(): Flow<Long?> {
        return dataStore.data.map { prefs ->
            prefs[END_TIME]
        }
    }

    suspend fun saveEmail(email: String) {
        dataStore.edit { prefs ->
            prefs[EMAIL] = email
        }
    }

    suspend fun saveUserId(userId: Int) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    suspend fun saveDeviceMac(deviceMac: String) {
        dataStore.edit { prefs ->
            prefs[DEVICE_MAC] = deviceMac
        }
    }

    suspend fun saveAccessToken(token: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveStartTime(StartTime: Long) {
        dataStore.edit { prefs ->
            prefs[START_TIME] = StartTime
        }
    }

    suspend fun saveMeasurementTime(MeasurementTime: Long) {
        dataStore.edit { prefs ->
            prefs[MEASUREMENT_TIME] = MeasurementTime
        }
    }

    suspend fun saveEndTime(EndTime: Long) {
        dataStore.edit { prefs ->
            prefs[END_TIME] = EndTime
        }
    }

    suspend fun deleteAccessToken() {
        dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
        }
    }

    suspend fun deleteUserId() {
        dataStore.edit { prefs ->
            prefs.remove(USER_ID)
        }
    }

    suspend fun deleteDeviceMac() {
        dataStore.edit { prefs ->
            prefs.remove(DEVICE_MAC)
        }
    }

    suspend fun deleteStartTime() {
        dataStore.edit { prefs ->
            prefs.remove(START_TIME)
        }
    }

    suspend fun deleteMeasurementTime() {
        dataStore.edit { prefs ->
            prefs.remove(MEASUREMENT_TIME)
        }
    }

    suspend fun deleteEndTime() {
        dataStore.edit { prefs ->
            prefs.remove(END_TIME)
        }
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = token
        }
    }

}