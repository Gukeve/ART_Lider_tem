package com.artleader.mvp.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.artleader.mvp.ui.screens.main.ai.AiScreen
import com.artleader.mvp.ui.screens.main.profile.ProfileScreen
import com.artleader.mvp.ui.screens.main.tools.ToolsScreen
import com.artleader.mvp.viewmodel.MainViewModel

@Composable
fun MainShell(vm: MainViewModel) {
    var idx by remember { mutableIntStateOf(0) }
    Scaffold(bottomBar = {
        NavigationBar {
            listOf("Профиль", "AI", "Инструменты").forEachIndexed { i, t ->
                NavigationBarItem(selected = idx == i, onClick = { idx = i }, label = { Text(t) }, icon = {
                    Icon(if (i == 0) Icons.Default.Face else if (i == 1) Icons.Default.Psychology else Icons.Default.Build, null)
                })
            }
        }
    }) { p ->
        when (idx) {
            0 -> ProfileScreen(vm, Modifier.padding(p))
            1 -> AiScreen(vm, Modifier.padding(p))
            else -> ToolsScreen(Modifier.padding(p))
        }
    }
}
