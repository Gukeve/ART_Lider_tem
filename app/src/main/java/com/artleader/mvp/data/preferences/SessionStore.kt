package com.artleader.mvp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore("session")

data class UserSession(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val rememberMe: Boolean = false,
    val biometricEnabled: Boolean = false
)

class SessionStore(private val context: Context) {
    private val loggedInKey = booleanPreferencesKey("logged_in")
    private val usernameKey = stringPreferencesKey("username")
    private val rememberKey = booleanPreferencesKey("remember_me")
    private val biometricKey = booleanPreferencesKey("biometric_enabled")

    val session = context.sessionDataStore.data.map {
        UserSession(
            isLoggedIn = it[loggedInKey] ?: false,
            username = it[usernameKey] ?: "",
            rememberMe = it[rememberKey] ?: false,
            biometricEnabled = it[biometricKey] ?: false
        )
    }

    suspend fun saveLoggedIn(username: String, rememberMe: Boolean) {
        context.sessionDataStore.edit {
            it[loggedInKey] = rememberMe
            it[usernameKey] = username
            it[rememberKey] = rememberMe
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) = context.sessionDataStore.edit { it[biometricKey] = enabled }
    suspend fun logout() = context.sessionDataStore.edit { it.clear() }
}
