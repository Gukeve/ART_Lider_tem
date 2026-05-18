package com.artleader.mvp.ui.screens.main.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ToolsScreen(modifier: Modifier = Modifier) {
    val tools = remember { mutableStateListOf("Обычный калькулятор", "Калькулятор люверсов", "Калькулятор баннеров", "Модульные решения") }
    var newTool by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Инструменты")
        LazyColumn(Modifier.weight(1f)) { items(tools) { Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Text(it, Modifier.padding(16.dp)) } } }
        OutlinedTextField(newTool, { newTool = it }, label = { Text("Динамический инструмент") }, modifier = Modifier.fillMaxWidth())
        Row {
            Button(onClick = { if (newTool.isNotBlank()) tools.add(newTool) }) { Text("Добавить") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { if (tools.isNotEmpty()) tools.removeLast() }) { Text("Удалить") }
        }
    }
}
