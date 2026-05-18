package com.artleader.mvp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artleader.mvp.data.local.entity.UserEntity
import com.artleader.mvp.data.preferences.AppSettings
import com.artleader.mvp.data.preferences.SettingsStore
import com.artleader.mvp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {
    val settings = settingsStore.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init { viewModelScope.launch { authRepository.ensureSeed() } }

    fun login(login: String, password: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        _user.value = authRepository.login(login, password)
        onResult(_user.value != null)
    }

    fun setAnimations(v: Boolean) = viewModelScope.launch { settingsStore.setAnimations(v) }
    fun setBirthday(v: Boolean) = viewModelScope.launch { settingsStore.setBirthday(v) }
    fun setTheme(v: Boolean) = viewModelScope.launch { settingsStore.setDarkTheme(v) }
    fun setApiKey(v: String) = viewModelScope.launch { settingsStore.setApiKey(v) }
    fun clearCache() = viewModelScope.launch { settingsStore.clear() }
}
