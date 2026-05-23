package com.artleader.mvp.ui.screens.main.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MainViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var show by remember { mutableStateOf(false) }
    var key by remember(settings.apiKey) { mutableStateOf(settings.apiKey) }
    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text("AI") }, actions = {
        IconButton(onClick = { show = true }) { Icon(Icons.Default.MoreVert, null) }
    }) }) { p ->
        Column(Modifier.padding(p).fillMaxSize().padding(16.dp)) {
            Text("AI чат (mockup)")
            Spacer(Modifier.height(10.dp))
            Text("AI: Готов помочь с задачами Art Leader")
            Text("Вы: Подключу API позже")
        }
    }
    if (show) AlertDialog(onDismissRequest = { show = false }, confirmButton = {
        TextButton(onClick = { vm.setApiKey(key); show = false }) { Text("Сохранить") }
    }, title = { Text("Добавить API") }, text = {
        OutlinedTextField(value = key, onValueChange = { key = it }, label = { Text("API key") })
    })
}
