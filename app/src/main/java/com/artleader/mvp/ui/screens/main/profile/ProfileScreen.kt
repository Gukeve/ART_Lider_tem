package com.artleader.mvp.ui.screens.main.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MainViewModel

@Composable
fun ProfileScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF17122F), Color(0xFF0A0C18), Color.Black)))) {
        IconButton(onClick = { showSettings = true }, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            Icon(Icons.Default.Settings, null, tint = Color(0xFFFFE44D))
        }

        Column(
            Modifier.align(Alignment.Center).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Виталик", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Text("Мастер наружной рекламы", color = Color(0xFFBFC8EA))
            Card(shape = RoundedCornerShape(16.dp)) { Text("2 категория", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color(0xFF1A1B24)) }
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(Modifier.background(Brush.verticalGradient(listOf(Color(0x66F94892), Color(0x66339CFF), Color(0x22171A26)))).padding(18.dp)) {
                    Text("ART LEADER CREATOR", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text("Neon profile hero ready. Добавьте image resource в следующий шаг для полноэкранного hero-баннера.", color = Color(0xFFD7E2FF))
                }
            }
            AnimatedVisibility(settings.birthday) { Text("🎉 Персональные достижения и бейджи скоро здесь", color = Color(0xFFFFE44D)) }
            Button(onClick = {}, shape = RoundedCornerShape(18.dp)) { Text("Редактировать профиль") }
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
