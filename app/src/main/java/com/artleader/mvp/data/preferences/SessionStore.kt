package com.artleader.mvp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.util.Base64

private val Context.sessionDataStore by preferencesDataStore("session")

data class UserSession(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val biometricEnabled: Boolean = false,
    val hasEncryptedSession: Boolean = false
)

class SessionStore(private val context: Context) {
    private val activeKey = booleanPreferencesKey("active")
    private val usernameKey = stringPreferencesKey("username")
    private val biometricKey = booleanPreferencesKey("biometric_enabled")
    private val encryptedTokenKey = stringPreferencesKey("encrypted_token")

    val session = context.sessionDataStore.data.map {
        UserSession(
            isLoggedIn = it[activeKey] ?: false,
            username = it[usernameKey] ?: "",
            biometricEnabled = it[biometricKey] ?: false,
            hasEncryptedSession = !(it[encryptedTokenKey].isNullOrBlank())
        )
    }

    suspend fun saveAuthenticated(username: String, biometricEnabled: Boolean) {
        context.sessionDataStore.edit {
            it[activeKey] = true
            it[usernameKey] = username
            it[biometricKey] = biometricEnabled
            it[encryptedTokenKey] = Base64.getEncoder().encodeToString("$username:${System.currentTimeMillis()}".toByteArray())
        }
    }

    suspend fun unlockWithBiometric() = context.sessionDataStore.edit { if (!(it[encryptedTokenKey].isNullOrBlank())) it[activeKey] = true }
    suspend fun lockOnAppLaunch() = context.sessionDataStore.edit { it[activeKey] = false }
    suspend fun logout() = context.sessionDataStore.edit { it.clear() }
}
