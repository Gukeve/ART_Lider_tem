package com.artleader.mvp.ui.screens.main.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MainViewModel

@Composable
fun ProfileScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val user by vm.user.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF1B1B1B), Color.Black)))) {
        IconButton(onClick = { showSettings = true }, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Default.Settings, null) }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${user?.displayName ?: "Сотрудник"}")
            Text("${user?.position ?: "Должность"}")
            Spacer(Modifier.height(12.dp))
            Text("\"Создаю будущее\"")
            AnimatedVisibility(settings.birthday && user?.birthDate?.endsWith("-05-18") == true) { Text("🎉 С днем рождения") }
            Button(onClick = {}, modifier = Modifier.padding(top = 16.dp)) { Text("Открыть профиль") }
        }
    }
    if (showSettings) SettingsDialog(vm) { showSettings = false }
}

@Composable
private fun SettingsDialog(vm: MainViewModel, close: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    AlertDialog(onDismissRequest = close, confirmButton = { TextButton(close) { Text("Закрыть") } },
        title = { Text("Настройки") }, text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Анимации"); Switch(settings.animations, vm::setAnimations) }
                Row(verticalAlignment = Alignment.CenterVertically) { Text("День рождения"); Switch(settings.birthday, vm::setBirthday) }
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Темная тема"); Switch(settings.darkTheme, vm::setTheme) }
                TextButton(onClick = vm::clearCache) { Text("Очистить локальный кэш") }
            }
        })
}
