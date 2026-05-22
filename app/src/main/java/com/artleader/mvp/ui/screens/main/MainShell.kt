package com.artleader.mvp.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.artleader.mvp.ui.screens.main.ai.AiScreen
import com.artleader.mvp.ui.screens.main.messenger.MessengerScreen
import com.artleader.mvp.ui.screens.main.profile.ProfileScreen
import com.artleader.mvp.ui.screens.main.tools.ToolsScreen
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

@Composable
fun MainShell(vm: MainViewModel, messengerViewModel: MessengerViewModel) {
    var idx by remember { mutableIntStateOf(0) }
    Scaffold(bottomBar = {
        NavigationBar {
            listOf("Профиль", "AI", "Инструменты", "Messenger").forEachIndexed { i, t ->
                NavigationBarItem(selected = idx == i, onClick = { idx = i }, label = { Text(t) }, icon = {
                    val icon = when (i) {
                        0 -> Icons.Default.Face
                        1 -> Icons.Default.Psychology
                        2 -> Icons.Default.Build
                        else -> Icons.Default.Forum
                    }
                    Icon(icon, null)
                })
            }
        }
    }) { p ->
        when (idx) {
            0 -> ProfileScreen(vm, Modifier.padding(p))
            1 -> AiScreen(vm, Modifier.padding(p))
            2 -> ToolsScreen(Modifier.padding(p))
            else -> MessengerScreen(messengerViewModel, Modifier.padding(p))
        }
    }
}
