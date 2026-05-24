package com.artleader.mvp.ui.screens.main.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artleader.mvp.viewmodel.AttendanceViewModel

data class ToolCardUi(val title: String, val description: String, val icon: ImageVector)

@Composable
fun ToolsScreen(attendanceViewModel: AttendanceViewModel, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<ToolCardUi?>(null) }
    if (selected?.title == "Attendance / NFC") {
        AttendanceScreen(vm = attendanceViewModel, onBack = { selected = null })
        return
    }
    val tools = remember {
        listOf(
            ToolCardUi("Banner hole calculator", "Расчет периметра и точек крепежа", Icons.Default.Calculate),
            ToolCardUi("Attendance / NFC", "Отметки входа/выхода и история смен", Icons.Default.BluetoothSearching),
            ToolCardUi("Future tools", "Mesh sync, warehouse, QA modules", Icons.Default.Build)
        )
    }
    Column(modifier.fillMaxSize().background(Color(0xFF07080F)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tools", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("Futuristic control center", color = Color(0xFF8EA2C8))
        Spacer(Modifier.height(4.dp))
        tools.forEach { tool ->
            val isSelected = selected == tool
            val scale by animateFloatAsState(if (isSelected) 0.98f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "tool-scale")
            Box(
                Modifier.fillMaxWidth().scale(scale).clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF121A2E), Color(0xFF10142A))))
                    .border(1.dp, Color(0xFF4EF7FF).copy(if (isSelected) 0.65f else 0.3f), RoundedCornerShape(22.dp))
                    .clickable { selected = tool }
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(tool.icon, null, tint = Color(0xFF4EF7FF), modifier = Modifier.size(24.dp))
                    Text(tool.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(tool.description, color = Color(0xFF8EA2C8), fontSize = 13.sp)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF4EF7FF), modifier = Modifier.align(Alignment.CenterEnd))
            }
        }
        AnimatedVisibility(visible = selected?.title == "Banner hole calculator") {
            Text("Калькулятор будет подключен в следующем инкременте через отдельный ViewModel модуль.", color = Color(0xFF8EA2C8))
        }
    }
}
