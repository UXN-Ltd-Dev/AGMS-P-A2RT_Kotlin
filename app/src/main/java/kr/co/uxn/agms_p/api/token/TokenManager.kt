package kr.co.uxn.agms_p.api.token

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object TokenManager {
    private lateinit var dataStore: DataStore<Preferences>
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val EMAIL = stringPreferencesKey("email")
    private val USER_ID = intPreferencesKey("user_id")

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

    suspend fun saveAccessToken(token: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
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

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { prefs ->
            prefs[REFRESH_TOKEN_KEY] = token
        }
    }

}