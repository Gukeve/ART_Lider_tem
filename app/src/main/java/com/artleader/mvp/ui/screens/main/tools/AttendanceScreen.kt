package com.artleader.mvp.ui.screens.main.tools

import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.data.local.entity.AttendanceEventEntity
import com.artleader.mvp.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun AttendanceScreen(vm: AttendanceViewModel, onBack: () -> Unit) {
    val history by vm.history.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Detect NFC availability
    val hasNfc = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
    }

    // Pulse rings animation
    val pulse1 by rememberInfiniteTransition(label = "p1").animateFloat(0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), "p1v")
    val pulse2 by rememberInfiniteTransition(label = "p2").animateFloat(0f, 1f,
        infiniteRepeatable(tween(2000, 600, easing = LinearEasing), RepeatMode.Restart), "p2v")
    val pulse3 by rememberInfiniteTransition(label = "p3").animateFloat(0f, 1f,
        infiniteRepeatable(tween(2000, 1200, easing = LinearEasing), RepeatMode.Restart), "p3v")

    // Orb glow
    val orbGlow by rememberInfiniteTransition(label = "og").animateFloat(0.4f, 0.9f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), "ogv")

    // Compute shift status from history
    val lastEnter = history.lastOrNull { it.action == "ENTER" }?.timestamp
    val lastExit  = history.lastOrNull { it.action == "EXIT"  }?.timestamp
    val onShift   = lastEnter != null && (lastExit == null || lastEnter > lastExit)

    // Calculate session duration
    val sessionDuration = remember(history) { computeSessionDuration(history) }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF060D17)).padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("NFC Посещаемость", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Text("Отметка входа / выхода", color = Color(0xFF7E9BC2), fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        // ── Scan orb ────────────────────────────────────────────────────────
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Animated pulse rings
            for ((pulse, alpha) in listOf(pulse1 to 0.6f, pulse2 to 0.45f, pulse3 to 0.3f)) {
                Box(
                    Modifier
                        .size((140 + 80 * pulse).dp)
                        .scale(1f)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF4EF7FF).copy((1f - pulse) * alpha), CircleShape)
                )
            }
            // Glow blob
            Box(Modifier.size(180.dp).blur(40.dp)
                .background(Brush.radialGradient(listOf(Color(0xFF4EF7FF).copy(orbGlow * 0.4f), Color.Transparent)), CircleShape))
            // Main tap circle
            Box(
                Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFF0E2E4A), Color(0xFF061525)))
                    )
                    .border(2.dp,
                        Brush.linearGradient(listOf(Color(0xFF4EF7FF), Color(0xFF1A7AFF))),
                        CircleShape)
                    .clickable { vm.onNfcTagScanned("sim-tag-001", "worker", !onShift) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Nfc, null, tint = Color(0xFF4EF7FF), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(if (hasNfc) "Приложи метку" else "Симуляция", color = Color(0xFF4EF7FF), fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Status card ──────────────────────────────────────────────────────
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(
                    if (onShift) Color(0xFF4EF7FF).copy(0.08f) else Color.White.copy(0.04f)
                )
                .border(1.dp,
                    if (onShift) Color(0xFF4EF7FF).copy(0.3f) else Color.White.copy(0.06f),
                    RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(if (onShift) Icons.Default.Login else Icons.Default.Logout,
                    null, tint = if (onShift) Color(0xFF4EF7FF) else Color(0xFF8AA1C0),
                    modifier = Modifier.size(28.dp))
                Column {
                    Text(if (onShift) "На смене" else "Вне смены",
                        color = if (onShift) Color(0xFF4EF7FF) else Color(0xFF8AA1C0),
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (sessionDuration.isNotBlank()) {
                        Text("Сессия: $sessionDuration", color = Color.White.copy(0.5f), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Action buttons ───────────────────────────────────────────────────
        if (!hasNfc) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { vm.onNfcTagScanned("sim-tag", "worker", true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3E67))) {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Вход")
                }
                Button(onClick = { vm.onNfcTagScanned("sim-tag", "worker", false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E2C49))) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Выход")
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("NFC не обнаружен — симуляция активна", color = Color(0xFF4E5E7A), fontSize = 11.sp)
        }

        Spacer(Modifier.height(12.dp))

        // ── History ──────────────────────────────────────────────────────────
        Text("История", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (history.isEmpty()) {
                item {
                    Text("Записей нет", color = Color(0xFF4E5E7A), modifier = Modifier.padding(vertical = 12.dp))
                }
            }
            items(history) { event ->
                HistoryRow(event)
            }
        }
    }
}

@Composable
private fun HistoryRow(event: AttendanceEventEntity) {
    val ts = SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault()).format(Date(event.timestamp))
    val isEnter = event.action == "ENTER"
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF101A2B)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape)
            .background(if (isEnter) Color(0xFF4EF7FF) else Color(0xFFFF6B9D)))
        Column(Modifier.weight(1f)) {
            Text(if (isEnter) "Вход" else "Выход",
                color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(event.employeeLogin, color = Color(0xFF6B7FA3), fontSize = 11.sp)
        }
        Text(ts, color = Color(0xFF6B7FA3), fontSize = 11.sp)
    }
}

private fun computeSessionDuration(history: List<AttendanceEventEntity>): String {
    if (history.isEmpty()) return ""
    val lastEnter = history.lastOrNull { it.action == "ENTER" } ?: return ""
    val lastExit  = history.lastOrNull { it.action == "EXIT" }
    val end = if (lastExit != null && lastExit.timestamp > lastEnter.timestamp) lastExit.timestamp
    else System.currentTimeMillis()
    val diffMs = end - lastEnter.timestamp
    val hours   = TimeUnit.MILLISECONDS.toHours(diffMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs) % 60
    return if (hours > 0) "${hours}ч ${minutes}м" else "${minutes}м"
}
