package com.artleader.mvp.ui.screens.main.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MainViewModel

// ─── Design tokens ────────────────────────────────────────────────────────────
private val BgTop  = Color(0xFF0A0414)
private val BgBot  = Color(0xFF060910)
private val Neon   = Color(0xFFFFE44D)
private val Pink   = Color(0xFFFF3D78)
private val Blue   = Color(0xFF339CFF)
private val Cyan   = Color(0xFF4EF7FF)
private val Dim    = Color(0xFF7A82A6)

private data class Badge(val icon: ImageVector, val label: String, val color: Color)

@Composable
fun ProfileScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    val glowAlpha by rememberInfiniteTransition(label = "pg").animateFloat(
        initialValue = 0.35f, targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "pa"
    )

    val badges = remember {
        listOf(
            Badge(Icons.Default.Star,             "Мастер",       Neon),
            Badge(Icons.Default.Bolt,             "Топ-10",       Pink),
            Badge(Icons.Default.Verified,         "Верифицирован",Blue),
            Badge(Icons.Default.WorkspacePremium, "Категория 2",  Cyan),
        )
    }

    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgTop, BgBot)))) {

        // Ambient glow blobs
        Box(
            Modifier.size(300.dp).offset((-80).dp, (-60).dp).blur(90.dp)
                .background(Brush.radialGradient(listOf(Pink.copy(glowAlpha * 0.35f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.size(220.dp).align(Alignment.BottomEnd).offset(60.dp, 60.dp).blur(70.dp)
                .background(Brush.radialGradient(listOf(Blue.copy(glowAlpha * 0.30f), Color.Transparent)), CircleShape)
        )

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Hero ──────────────────────────────────────────────────────────
            Box(
                Modifier.fillMaxWidth().height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A0B3A), Color(0xFF130A28), Color(0xFF060910))
                        )
                    )
            ) {
                // Decorative ring drawn with Canvas
                Canvas(Modifier.size(220.dp).align(Alignment.Center)) {
                    drawCircle(
                        brush  = Brush.linearGradient(listOf(Pink.copy(0.5f), Blue.copy(0.3f))),
                        radius = size.minDimension / 2f,
                        style  = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        brush  = Brush.linearGradient(listOf(Pink.copy(0.10f), Color.Transparent)),
                        radius = size.minDimension / 2f - 14f
                    )
                }
                // Avatar
                Box(
                    Modifier
                        .size(96.dp)
                        .align(Alignment.Center)
                        .offset(y = (-24).dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Pink, Blue, Cyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("В", fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                // Settings button top-right
                IconButton(
                    onClick  = { showSettings = true },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        .clip(CircleShape).background(Color.White.copy(0.10f))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Neon)
                }
                // Name
                Column(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Виталик", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Мастер наружной рекламы", color = Dim, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Badges ────────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                badges.forEach { b ->
                    BadgeChip(badge = b, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Stats glass card ──────────────────────────────────────────────
            GlassCard(Modifier.padding(horizontal = 20.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    StatColumn("128", "Проектов")
                    VerticalDivider(
                        modifier  = Modifier.height(48.dp),
                        color     = Color.White.copy(0.10f),
                        thickness = 1.dp
                    )
                    StatColumn("4.9", "Рейтинг")
                    VerticalDivider(
                        modifier  = Modifier.height(48.dp),
                        color     = Color.White.copy(0.10f),
                        thickness = 1.dp
                    )
                    StatColumn("3 г.", "Стаж")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Creator banner ────────────────────────────────────────────────
            Box(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0x88FF3D78), Color(0x66339CFF), Color(0x220D0F1E))
                        )
                    )
                    .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null,
                            tint = Neon, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "ART LEADER CREATOR",
                            color      = Neon,
                            fontWeight = FontWeight.Black,
                            fontSize   = 12.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Вы в топ-10% специалистов платформы. До следующего уровня — 12 проектов.",
                        color    = Color.White.copy(0.85f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Birthday card ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = settings.birthday,
                enter   = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit    = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                GlassCard(Modifier.padding(horizontal = 20.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Персональные достижения", color = Neon, fontWeight = FontWeight.Bold)
                            Text("Бейджи и награды скоро здесь", color = Dim, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Edit button ───────────────────────────────────────────────────
            Button(
                onClick  = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape  = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(listOf(Pink, Blue))
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Редактировать профиль", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showSettings) SettingsDialog(vm) { showSettings = false }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(0.06f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
    ) { content() }
}

@Composable
private fun BadgeChip(badge: Badge, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(badge.color.copy(0.10f))
            .border(1.dp, badge.color.copy(0.25f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(badge.icon, contentDescription = null,
            tint = badge.color, modifier = Modifier.size(20.dp))
        Text(
            badge.label,
            color      = Color.White,
            fontSize   = 9.sp,
            fontWeight = FontWeight.Medium,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text(label, color = Dim, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsDialog(vm: MainViewModel, close: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    AlertDialog(
        onDismissRequest = close,
        confirmButton    = { TextButton(onClick = close) { Text("Закрыть") } },
        title            = { Text("Настройки", color = Color.White) },
        containerColor   = Color(0xFF12152A),
        shape            = RoundedCornerShape(28.dp),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingRow("Анимации",    settings.animations, vm::setAnimations)
                SettingRow("День рождения", settings.birthday, vm::setBirthday)
                SettingRow("Тёмная тема", settings.darkTheme,  vm::setTheme)
                Divider(color = Color.White.copy(0.08f))
                TextButton(onClick = vm::clearCache) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Очистить кэш", color = Color(0xFFFF6B6B))
                }
            }
        }
    )
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White)
        Switch(checked = value, onCheckedChange = onChange)
    }
}