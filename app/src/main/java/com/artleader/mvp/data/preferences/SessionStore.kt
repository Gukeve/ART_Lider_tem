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
    val username: String    = "",           // login key, e.g. "vitalik"
    val displayName: String = "",           // e.g. "Виталик"
    val biometricEnabled: Boolean = false,
    val hasEncryptedSession: Boolean = false
)

class SessionStore(private val context: Context) {

    private val activeKey         = booleanPreferencesKey("active")
    private val usernameKey       = stringPreferencesKey("username")
    private val displayNameKey    = stringPreferencesKey("display_name")
    private val biometricKey      = booleanPreferencesKey("biometric_enabled")
    // Stores the actual username for biometric re-auth (no fake hardcoded user)
    private val bioUsernameKey    = stringPreferencesKey("bio_username")

    val session = context.sessionDataStore.data.map { prefs ->
        UserSession(
            isLoggedIn          = prefs[activeKey]      ?: false,
            username            = prefs[usernameKey]    ?: "",
            displayName         = prefs[displayNameKey] ?: "",
            biometricEnabled    = prefs[biometricKey]   ?: false,
            hasEncryptedSession = !(prefs[bioUsernameKey].isNullOrBlank())
        )
    }

    /** Save session after successful credential login. */
    suspend fun saveAuthenticated(
        username: String,
        displayName: String,
        biometricEnabled: Boolean
    ) {
        context.sessionDataStore.edit { prefs ->
            prefs[activeKey]      = true
            prefs[usernameKey]    = username
            prefs[displayNameKey] = displayName
            prefs[biometricKey]   = biometricEnabled
            if (biometricEnabled) {
                // Store the actual username so biometric re-auth restores THIS user,
                // not a hardcoded one.
                prefs[bioUsernameKey] = username
            }
        }
    }

    /**
     * Re-authenticate via biometric.
     * Returns the stored username so the ViewModel can load the correct profile,
     * or null if no biometric session exists.
     */
    suspend fun unlockWithBiometric(): String? {
        var storedUsername: String? = null
        context.sessionDataStore.edit { prefs ->
            val bio = prefs[bioUsernameKey]
            if (!bio.isNullOrBlank()) {
                prefs[activeKey]   = true
                prefs[usernameKey] = bio
                storedUsername = bio
            }
        }
        return storedUsername
    }

    /** Lock on cold launch — requires re-auth. Biometric token is preserved. */
    suspend fun lockOnAppLaunch() {
        context.sessionDataStore.edit { prefs ->
            prefs[activeKey] = false
            // username / bioUsername preserved so biometric button is shown
        }
    }

    /** Full logout — wipes everything including biometric token. */
    suspend fun logout() {
        context.sessionDataStore.edit { it.clear() }
    }
}
