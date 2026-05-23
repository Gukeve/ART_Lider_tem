package com.artleader.mvp.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleRounded
import androidx.compose.material.icons.filled.HomeRounded
import androidx.compose.material.icons.filled.PersonRounded
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.artleader.mvp.ui.screens.main.ai.AiScreen
import com.artleader.mvp.ui.screens.main.messenger.MessengerScreen
import com.artleader.mvp.ui.screens.main.profile.ProfileScreen
import com.artleader.mvp.ui.screens.main.tools.ToolsScreen
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

@Composable
fun MainShell(vm: MainViewModel, messengerViewModel: MessengerViewModel) {
    var idx by remember { mutableIntStateOf(0) }
    val tabs = listOf("Home", "Messenger", "AI", "Profile")

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0x88131426)),
                containerColor = Color.Transparent
            ) {
                tabs.forEachIndexed { i, t ->
                    val icon = when (i) {
                        0 -> Icons.Default.HomeRounded
                        1 -> Icons.Default.ChatBubbleRounded
                        2 -> Icons.Default.AutoAwesome
                        else -> Icons.Default.PersonRounded
                    }
                    NavigationBarItem(
                        selected = idx == i,
                        onClick = { idx = i },
                        label = { Text(t) },
                        icon = { Icon(icon, null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFE44D),
                            selectedTextColor = Color.White,
                            indicatorColor = Color(0x66339CFF),
                            unselectedIconColor = Color(0xFF9FA8C3),
                            unselectedTextColor = Color(0xFF9FA8C3)
                        )
                    )
                }
            }
        }
    ) { p ->
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF090B14), Color(0xFF0D1022), Color(0xFF050608))
                    )
                )
                .padding(p)
        ) {
            Crossfade(targetState = idx, label = "main-tab") { selected ->
                when (selected) {
                    0 -> ToolsScreen(Modifier.fillMaxSize())
                    1 -> MessengerScreen(messengerViewModel, Modifier.fillMaxSize())
                    2 -> AiScreen(vm, Modifier.fillMaxSize())
                    else -> ProfileScreen(vm, Modifier.fillMaxSize())
                }
            }
        }
    }
}
