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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artleader.mvp.ui.screens.main.ai.AiScreen
import com.artleader.mvp.ui.screens.main.messenger.MessengerScreen
import com.artleader.mvp.ui.screens.main.profile.ProfileScreen
import com.artleader.mvp.ui.screens.main.tools.ToolsScreen
import com.artleader.mvp.viewmodel.AttendanceViewModel
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

private data class NavTab(val icon: ImageVector, val label: String)

@Composable
fun MainShell(
    vm: MainViewModel,
    messengerViewModel: MessengerViewModel,
    attendanceViewModel: AttendanceViewModel
) {
    var idx by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            NavTab(Icons.Default.Person,       "Профиль"),
            NavTab(Icons.Default.Chat,         "Чаты"),
            NavTab(Icons.Default.AutoAwesome,  "AI"),
            NavTab(Icons.Default.Construction, "Инструменты"),
            NavTab(Icons.Default.Code,         "Dev")
        )
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF07080F))) {

        // ── Content area ─────────────────────────────────────────────────────
        // Bottom padding accounts for nav bar height (80dp) + system nav bars
        Box(Modifier.fillMaxSize().padding(bottom = 80.dp)) {
            AnimatedContent(
                targetState  = idx,
                transitionSpec = {
                    (fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                            scaleIn(tween(200), initialScale = 0.97f))
                        .togetherWith(fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.97f))
                },
                label = "main-tabs"
            ) { tab ->
                when (tab) {
                    0    -> ProfileScreen(vm,                Modifier.fillMaxSize())
                    1    -> MessengerScreen(messengerViewModel, Modifier.fillMaxSize())
                    2    -> AiScreen(vm,                     Modifier.fillMaxSize())
                    3    -> ToolsScreen(attendanceViewModel, Modifier.fillMaxSize())
                    else -> DevPlaceholder()
                }
            }
        }

        // ── Floating bottom nav ───────────────────────────────────────────────
        PremiumBottomNav(
            modifier  = Modifier.align(Alignment.BottomCenter),
            tabs      = tabs,
            selected  = idx,
            onSelect  = { idx = it }
        )
    }
}

@Composable
private fun PremiumBottomNav(
    modifier: Modifier,
    tabs: List<NavTab>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val aiAura by rememberInfiniteTransition(label = "ai-aura").animateFloat(
        0.25f, 0.70f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "aa"
    )

    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Glass pill background
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(32.dp), clip = false)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xEE0C0E1C))
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { i, tab ->
                if (i == 2) {
                    // ── Floating AI button ──────────────────────────────────
                    Box(
                        Modifier.offset(y = (-14).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Aura glow
                        Box(
                            Modifier.size(64.dp).blur(16.dp)
                                .background(Color(0xFF4EF7FF).copy(aiAura), CircleShape)
                        )
                        // Button
                        Box(
                            Modifier
                                .size(56.dp)
                                .shadow(12.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF5F42FF), Color(0xFF2C9BFF)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { onSelect(i) }, modifier = Modifier.fillMaxSize()) {
                                Icon(tab.icon, tab.label, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                } else {
                    // ── Regular tab ────────────────────────────────────────
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected == i) Color.White.copy(0.06f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        IconButton(onClick = { onSelect(i) }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                tab.icon, tab.label,
                                tint     = if (selected == i) Color(0xFF4EF7FF) else Color.White.copy(0.40f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (selected == i) {
                            Spacer(Modifier.height(2.dp))
                            Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF4EF7FF)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DevPlaceholder() {
    Box(Modifier.fillMaxSize().background(Color(0xFF07080F)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚙", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text("Dev module", color = Color(0xFF3A4A6A), fontWeight = FontWeight.Bold)
            Text("Coming soon", color = Color(0xFF2A3050), fontSize = 12.sp)
        }
    }
}
