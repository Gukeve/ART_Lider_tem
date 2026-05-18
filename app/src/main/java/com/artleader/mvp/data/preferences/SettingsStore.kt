package com.artleader.mvp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class AppSettings(
    val animations: Boolean = true,
    val birthday: Boolean = true,
    val darkTheme: Boolean = true,
    val apiKey: String = ""
)

class SettingsStore(private val context: Context) {
    private val animationsKey = booleanPreferencesKey("animations")
    private val birthdayKey = booleanPreferencesKey("birthday")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val apiKeyKey = stringPreferencesKey("api_key")

    val settings = context.dataStore.data.map {
        AppSettings(
            animations = it[animationsKey] ?: true,
            birthday = it[birthdayKey] ?: true,
            darkTheme = it[darkThemeKey] ?: true,
            apiKey = it[apiKeyKey] ?: ""
        )
    }

    suspend fun setAnimations(v: Boolean) = context.dataStore.edit { it[animationsKey] = v }
    suspend fun setBirthday(v: Boolean) = context.dataStore.edit { it[birthdayKey] = v }
    suspend fun setDarkTheme(v: Boolean) = context.dataStore.edit { it[darkThemeKey] = v }
    suspend fun setApiKey(v: String) = context.dataStore.edit { it[apiKeyKey] = v }
    suspend fun clear() = context.dataStore.edit { it.clear() }
}
