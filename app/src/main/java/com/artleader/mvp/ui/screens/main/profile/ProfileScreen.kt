package com.artleader.mvp.ui.screens.main.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.AdminPanelSettings
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
import com.artleader.mvp.data.local.entity.UserEntity
import com.artleader.mvp.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.cos
import kotlin.math.sin

// ── Design tokens ─────────────────────────────────────────────────────────────
private val BgTop = Color(0xFF0A0414)
private val BgBot = Color(0xFF060910)
private val Neon  = Color(0xFFFFE44D)
private val Pink  = Color(0xFFFF3D78)
private val Blue  = Color(0xFF339CFF)
private val Cyan  = Color(0xFF4EF7FF)
private val Dim   = Color(0xFF7A82A6)

private data class Badge(val icon: ImageVector, val label: String, val color: Color)

// Floating question tags that drift around avatar
private val FLOATING_QUESTIONS = listOf(
    "Проект?", "Дедлайн?", "Статус?", "Задача?",
    "Команда?", "Отчёт?", "Смена?", "Готово?"
)

@Composable
fun ProfileScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val user     by vm.user.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    val glowAlpha by rememberInfiniteTransition(label = "pg").animateFloat(
        0.35f, 0.80f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "pa"
    )

    // Derive display data from real user or defaults
    val displayName  = user?.displayName ?: "Гость"
    val position     = user?.position    ?: ""
    val initial      = displayName.take(1).uppercase()
    val rating       = user?.rating      ?: 0f
    val projects     = user?.projectCount ?: 0
    val production   = user?.productionPercent ?: 0
    val experience   = user?.let { calcExperience(it.employmentStart) } ?: "—"
    val isAdmin      = user?.isAdmin     ?: false

    val badges = remember(isAdmin, rating) {
        buildList {
            add(Badge(Icons.Default.Star,             "Мастер",       Neon))
            add(Badge(Icons.Default.Bolt,             "Топ-10",       Pink))
            add(Badge(Icons.Default.Verified,         "Верифицирован",Blue))
            if (isAdmin) add(Badge(Icons.Default.AdminPanelSettings, "Админ", Cyan))
            else         add(Badge(Icons.Default.WorkspacePremium,   "Прем", Cyan))
        }
    }

    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgTop, BgBot)))) {

        // Ambient glow blobs
        Box(Modifier.size(300.dp).offset((-80).dp, (-60).dp).blur(90.dp)
            .background(Brush.radialGradient(listOf(Pink.copy(glowAlpha * 0.35f), Color.Transparent)), CircleShape))
        Box(Modifier.size(220.dp).align(Alignment.BottomEnd).offset(60.dp, 60.dp).blur(70.dp)
            .background(Brush.radialGradient(listOf(Blue.copy(glowAlpha * 0.30f), Color.Transparent)), CircleShape))

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Hero ──────────────────────────────────────────────────────────
            Box(
                Modifier.fillMaxWidth().height(300.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF2A0B3A), Color(0xFF130A28), Color(0xFF060910))))
            ) {
                // Decorative ring
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

                // Floating question tags
                FloatingQuestions()

                // Avatar
                Box(
                    Modifier.size(96.dp).align(Alignment.Center).offset(y = (-24).dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Pink, Blue, Cyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, fontSize = 38.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                // Settings button
                IconButton(
                    onClick  = { showSettings = true },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        .clip(CircleShape).background(Color.White.copy(0.10f))
                ) {
                    Icon(Icons.Default.Settings, null, tint = Neon)
                }

                // Name + position
                Column(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(displayName, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    if (position.isNotBlank()) Text(position, color = Dim, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Badges ────────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                badges.forEach { b -> BadgeChip(b, Modifier.weight(1f)) }
            }

            Spacer(Modifier.height(20.dp))

            // ── Stats card ────────────────────────────────────────────────────
            GlassCard(Modifier.padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically) {
                    StatColumn(projects.toString(), "Проектов")
                    VerticalDivider(Modifier.height(48.dp), color = Color.White.copy(0.10f), thickness = 1.dp)
                    StatColumn("${"%.1f".format(rating)}", "Рейтинг")
                    VerticalDivider(Modifier.height(48.dp), color = Color.White.copy(0.10f), thickness = 1.dp)
                    StatColumn(experience, "Стаж")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Production percentage ─────────────────────────────────────────
            GlassCard(Modifier.padding(horizontal = 20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Выработка", color = Dim, fontSize = 13.sp)
                        Text("$production%", color = Neon, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    // Progress bar
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(0.08f))) {
                        Box(Modifier.fillMaxWidth(production / 100f).height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Brush.linearGradient(listOf(Cyan, Blue))))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Creator banner ─────────────────────────────────────────────────
            Box(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Color(0x88FF3D78), Color(0x66339CFF), Color(0x220D0F1E))))
                    .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Neon, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ART LEADER CREATOR", color = Neon, fontWeight = FontWeight.Black,
                            fontSize = 12.sp, letterSpacing = 2.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (projects > 0)
                            "Вы выполнили $projects проектов с рейтингом ${"%.1f".format(rating)}. " +
                                    "Выработка $production% от нормы."
                        else
                            "Добро пожаловать на платформу Art Leader. Начните первый проект.",
                        color = Color.White.copy(0.85f), fontSize = 14.sp, lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Birthday / achievements card ───────────────────────────────────
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

            // ── Edit button ────────────────────────────────────────────────────
            Button(
                onClick  = { /* TODO: edit profile sheet */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border   = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Pink, Blue)))
            ) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Редактировать профиль", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showSettings) SettingsDialog(vm) { showSettings = false }
}

// ── Floating question tags ────────────────────────────────────────────────────
@Composable
private fun FloatingQuestions() {
    FLOATING_QUESTIONS.forEachIndexed { i, text ->
        val speed = 2800 + i * 400
        val offset = i * (360f / FLOATING_QUESTIONS.size)
        val angle by rememberInfiniteTransition(label = "fq$i").animateFloat(
            offset, offset + 360f,
            infiniteRepeatable(tween(speed, easing = LinearEasing)), "fqa$i"
        )
        val alpha by rememberInfiniteTransition(label = "fa$i").animateFloat(
            0.0f, 0.55f,
            infiniteRepeatable(tween(speed / 2, i * 300, easing = FastOutSlowInEasing), RepeatMode.Reverse), "faa$i"
        )
        val radius = 115f
        val xOff   = (radius * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val yOff   = (radius * sin(Math.toRadians(angle.toDouble()))).toFloat()

        Box(
            Modifier
                .align(Alignment.Center)
                .offset(xOff.dp, (yOff - 24).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha * 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(text, color = Color.White.copy(alpha), fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────
@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
        .background(Color.White.copy(0.06f))
        .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
    ) { content() }
}

@Composable
private fun BadgeChip(badge: Badge, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(badge.color.copy(0.10f))
            .border(1.dp, badge.color.copy(0.25f), RoundedCornerShape(16.dp)).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(badge.icon, null, tint = badge.color, modifier = Modifier.size(20.dp))
        Text(badge.label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text(label,  color = Dim,         fontSize   = 12.sp)
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
                SettingRow("Анимации",       settings.animations, vm::setAnimations)
                SettingRow("День рождения",  settings.birthday,   vm::setBirthday)
                SettingRow("Тёмная тема",    settings.darkTheme,  vm::setTheme)
                Divider(color = Color.White.copy(0.08f))
                TextButton(onClick = { vm.clearCache(); close() }) {
                    Icon(Icons.Default.DeleteOutline, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Очистить кэш", color = Color(0xFFFF6B6B))
                }
                Divider(color = Color.White.copy(0.08f))
                TextButton(onClick = { vm.logout(); close() }) {
                    Text("Выйти из аккаунта", color = Color(0xFFFF6B6B))
                }
            }
        }
    )
}

@Composable
private fun SettingRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White)
        Switch(checked = value, onCheckedChange = onChange)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun calcExperience(startDate: String): String {
    return try {
        val fmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = LocalDate.parse(startDate, fmt)
        val now   = LocalDate.now()
        val years = ChronoUnit.YEARS.between(start, now)
        val months = ChronoUnit.MONTHS.between(start.plusYears(years), now)
        when {
            years > 0  -> "${years} г."
            months > 0 -> "${months} мес."
            else       -> "< 1 мес."
        }
    } catch (_: Exception) { "—" }
}
