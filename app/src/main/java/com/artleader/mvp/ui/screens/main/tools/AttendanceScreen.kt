package com.artleader.mvp.ui.screens.main.tools

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(vm: AttendanceViewModel, onBack: () -> Unit) {
    val history by vm.history.collectAsStateWithLifecycle()
    val pulse by rememberInfiniteTransition(label = "nfc").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "p"
    )

    val openEnter = history.lastOrNull { it.action == "ENTER" }?.timestamp
    val lastExit = history.lastOrNull { it.action == "EXIT" }?.timestamp
    val onShift = openEnter != null && (lastExit == null || openEnter > lastExit)

    Column(Modifier.fillMaxSize().background(Color(0xFF060D17)).padding(16.dp)) {
        Text("Attendance / NFC", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Text("Industrial dashboard", color = Color(0xFF7E9BC2))
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(220.dp).blur(36.dp).background(Brush.radialGradient(listOf(Color(0xFF4EF7FF).copy(pulse), Color.Transparent)), CircleShape))
            Box(Modifier.size(160.dp).clip(CircleShape).border(2.dp, Color(0xFF4EF7FF), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.BluetoothSearching, null, tint = Color(0xFF4EF7FF), modifier = Modifier.size(64.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(if (onShift) "Статус: На смене" else "Статус: Вне смены", color = if (onShift) Color(0xFF4EF7FF) else Color(0xFF8AA1C0))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { vm.onNfcTagScanned("sim-tag", "worker", true) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3E67))) { Text("Scan NFC tag (Enter)") }
            Button(onClick = { vm.onNfcTagScanned("sim-tag", "worker", false) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E2C49))) { Text("Exit") }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131A2C))) { Text("Back") }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(history) { event ->
                val ts = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(event.timestamp))
                Text("${event.action} • $ts • ${event.employeeLogin}", color = Color(0xFFB8D8FF), modifier = Modifier
                    .fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF101A2B)).padding(12.dp))
            }
        }
    }
}
