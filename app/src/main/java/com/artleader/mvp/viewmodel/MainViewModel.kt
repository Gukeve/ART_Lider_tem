package com.artleader.mvp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artleader.mvp.data.local.entity.UserEntity
import com.artleader.mvp.data.preferences.AppSettings
import com.artleader.mvp.data.preferences.SessionStore
import com.artleader.mvp.data.preferences.SettingsStore
import com.artleader.mvp.data.preferences.UserSession
import com.artleader.mvp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val settingsStore: SettingsStore,
    private val sessionStore: SessionStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val session: StateFlow<UserSession> = sessionStore.session
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSession())

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.ensureSeed()
            sessionStore.lockOnAppLaunch()
        }
    }

    fun login(
        login: String,
        password: String,
        biometricEnabled: Boolean,
        onResult: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val found = authRepository.login(login.trim().lowercase(), password)
        _user.value = found
        if (found != null) {
            sessionStore.saveAuthenticated(
                username         = found.login,
                displayName      = found.displayName,
                biometricEnabled = biometricEnabled
            )
        }
        onResult(found != null)
    }

    /**
     * Biometric unlock — restores the LAST authenticated user, not a hardcoded one.
     * Calls onResult(true, username) on success so the ViewModel can load the profile.
     */
    fun unlockWithBiometric(onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val username = sessionStore.unlockWithBiometric()
        if (username != null) {
            _user.value = authRepository.getUser(username)
            onResult(true)
        } else {
            onResult(false)
        }
    }

    fun logout() = viewModelScope.launch {
        _user.value = null
        sessionStore.logout()
    }

    fun updateAvatar(uri: String) = viewModelScope.launch {
        val login = _user.value?.login ?: return@launch
        authRepository.updateAvatar(login, uri)
        _user.value = authRepository.getUser(login)
    }

    fun setAnimations(v: Boolean)  = viewModelScope.launch { settingsStore.setAnimations(v) }
    fun setBirthday(v: Boolean)    = viewModelScope.launch { settingsStore.setBirthday(v) }
    fun setTheme(v: Boolean)       = viewModelScope.launch { settingsStore.setDarkTheme(v) }
    fun setApiKey(v: String)       = viewModelScope.launch { settingsStore.setApiKey(v) }
    fun clearCache()               = viewModelScope.launch { settingsStore.clear() }
}
