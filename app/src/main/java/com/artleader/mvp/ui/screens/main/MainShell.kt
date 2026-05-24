package com.artleader.mvp.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artleader.mvp.ui.screens.main.ai.AiScreen
import com.artleader.mvp.ui.screens.main.messenger.MessengerScreen
import com.artleader.mvp.ui.screens.main.profile.ProfileScreen
import com.artleader.mvp.ui.screens.main.tools.ToolsScreen
import com.artleader.mvp.viewmodel.MainViewModel
import com.artleader.mvp.viewmodel.MessengerViewModel

private val Bg         = Color(0xFF07080F)
private val Neon       = Color(0xFFFFE44D)
private val BlueTok    = Color(0xFF339CFF)
private val PinkTok    = Color(0xFFFF3D78)
private val CyanTok    = Color(0xFF4EF7FF)
private val TabAccents = listOf(Neon, CyanTok, PinkTok, BlueTok)

private data class NavTab(val icon: ImageVector, val label: String)

@Composable
fun MainShell(vm: MainViewModel, messengerViewModel: MessengerViewModel) {
    var idx by remember { mutableIntStateOf(0) }

    val tabs = remember {
        listOf(
            NavTab(Icons.Default.Home,        "Home"),
            NavTab(Icons.Default.Chat,        "Chats"),
            NavTab(Icons.Default.AutoAwesome, "AI"),
            NavTab(Icons.Default.Person,      "Profile"),
        )
    }

    Box(Modifier.fillMaxSize().background(Bg)) {

        // Screen area — fade-through cross-screen transition
        Box(Modifier.fillMaxSize().padding(bottom = 80.dp)) {
            AnimatedContent(
                targetState = idx,
                transitionSpec = {
                    (fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                            scaleIn(tween(220, easing = FastOutSlowInEasing), initialScale = 0.97f))
                        .togetherWith(
                            fadeOut(tween(110)) +
                                    scaleOut(tween(110), targetScale = 0.97f)
                        )
                },
                label = "main-tab"
            ) { selected ->
                when (selected) {
                    0    -> ToolsScreen(Modifier.fillMaxSize())
                    1    -> MessengerScreen(messengerViewModel, Modifier.fillMaxSize())
                    2    -> AiScreen(vm, Modifier.fillMaxSize())
                    else -> ProfileScreen(vm, Modifier.fillMaxSize())
                }
            }
        }

        // Floating glass bottom nav
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            // Dark glass pill background
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xCC0C0E1C))
            )
            // Active-tab top-edge glow
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            0f to TabAccents[idx].copy(0.25f),
                            0.08f to Color.Transparent
                        )
                    )
            )

            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { i, tab ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        NavItem(
                            tab        = tab,
                            selected   = idx == i,
                            accentColor = TabAccents[i],
                            onClick    = { idx = i }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: NavTab,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue      = if (selected) 1.13f else 1f,
        animationSpec    = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label            = "nav-scale"
    )
    val iconAlpha by animateFloatAsState(
        targetValue   = if (selected) 1f else 0.40f,
        animationSpec = tween(200),
        label         = "nav-alpha"
    )

    IconButton(
        onClick  = onClick,
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        // ColumnScope — both AnimatedVisibility overloads used here are the
        // generic (non-ColumnScope) ones, which compile in any scope.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Pill glow behind icon – uses Box scope, generic AnimatedVisibility
                androidx.compose.animation.AnimatedVisibility(
                    visible = selected,
                    enter   = fadeIn(tween(180)) + scaleIn(
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                    ),
                    exit    = fadeOut(tween(110)) + scaleOut(tween(110))
                ) {
                    Box(
                        Modifier
                            .size(width = 48.dp, height = 30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                    )
                }
                Icon(
                    imageVector     = tab.icon,
                    contentDescription = tab.label,
                    tint            = if (selected) accentColor else Color.White.copy(alpha = iconAlpha),
                    modifier        = Modifier.size(22.dp)
                )
            }
            // Label – ColumnScope.AnimatedVisibility is correct here (Column child)
            AnimatedVisibility(
                visible = selected,
                enter   = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit    = fadeOut(tween(120)) + shrinkVertically(tween(120))
            ) {
                Text(
                    text     = tab.label,
                    color    = accentColor,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}