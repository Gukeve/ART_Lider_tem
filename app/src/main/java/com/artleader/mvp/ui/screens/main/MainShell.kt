package com.artleader.mvp.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.artleader.mvp.ui.screens.main.ai.AiScreen
import com.artleader.mvp.ui.screens.main.messenger.MessengerScreen
import com.artleader.mvp.ui.screens.main.profile.ProfileScreen
import com.artleader.mvp.ui.screens.main.tools.ToolsScreen
import com.artleader.mvp.viewmodel.AttendanceViewModel
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

private data class NavTab(val icon: ImageVector, val label: String)

@Composable
fun MainShell(vm: MainViewModel, messengerViewModel: MessengerViewModel, attendanceViewModel: AttendanceViewModel) {
    var idx by remember { mutableIntStateOf(2) }
    val tabs = remember {
        listOf(
            NavTab(Icons.Default.Person, "Profile"),
            NavTab(Icons.Default.Chat, "Messenger"),
            NavTab(Icons.Default.AutoAwesome, "AI"),
            NavTab(Icons.Default.Construction, "Tools"),
            NavTab(Icons.Default.Code, "Dev")
        )
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF07080F))) {
        Box(Modifier.fillMaxSize().padding(bottom = 96.dp)) {
            AnimatedContent(targetState = idx, transitionSpec = {
                (fadeIn(tween(220, easing = FastOutSlowInEasing)) + scaleIn(tween(220), initialScale = 0.985f))
                    .togetherWith(fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.985f))
            }, label = "main-tabs") { selected ->
                when (selected) {
                    0 -> ProfileScreen(vm, Modifier.fillMaxSize())
                    1 -> MessengerScreen(messengerViewModel, Modifier.fillMaxSize())
                    2 -> AiScreen(vm, Modifier.fillMaxSize())
                    3 -> ToolsScreen(attendanceViewModel, Modifier.fillMaxSize())
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Dev module placeholder", color = Color(0xFF8EA2C8)) }
                }
            }
        }
        BottomNavGlass(tabs = tabs, selected = idx, onSelected = { idx = it })
    }
}

@Composable
private fun BottomNavGlass(tabs: List<NavTab>, selected: Int, onSelected: (Int) -> Unit) {
    val aura by rememberInfiniteTransition(label = "ai-aura").animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "aa")
    Box(Modifier.navigationBarsPadding().padding(16.dp).fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.padding(bottom = 2.dp)) {
            Box(Modifier.clip(RoundedCornerShape(32.dp)).background(Color(0xCC0C0E1C)).padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    tabs.forEachIndexed { i, tab ->
                        if (i == 2) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 2.dp)) {
                                Box(Modifier.size(66.dp).blur(18.dp).background(Color(0xFF4EF7FF).copy(aura), CircleShape))
                                IconButton(onClick = { onSelected(i) }, modifier = Modifier.size(56.dp).clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF5F42FF), Color(0xFF2C9BFF))))) {
                                    Icon(tab.icon, tab.label, tint = Color.White)
                                }
                            }
                        } else {
                            IconButton(onClick = { onSelected(i) }) {
                                Icon(tab.icon, tab.label, tint = if (selected == i) Color(0xFF4EF7FF) else Color.White.copy(0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}
