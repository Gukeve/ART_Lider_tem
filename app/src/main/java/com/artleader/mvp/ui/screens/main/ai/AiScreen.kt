package com.artleader.mvp.ui.screens.main.ai

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artleader.mvp.viewmodel.MainViewModel

interface AiProvider

data class AiConnectionConfig(val apiKey: String, val baseUrl: String, val model: String, val temperature: Float, val systemPrompt: String, val memoryPreset: String)

@Composable
fun AiScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    var api by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("https://api.openai.com/v1") }
    var model by remember { mutableStateOf("gpt-4.1-mini") }
    var systemPrompt by remember { mutableStateOf("You are Art Leader assistant") }
    var memoryPreset by remember { mutableStateOf("Production") }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var connectedModel by remember { mutableStateOf<String?>(null) }
    var connecting by remember { mutableStateOf(false) }

    val orbPulse by rememberInfiniteTransition(label = "orb").animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "op")

    Column(modifier.fillMaxSize().background(Color(0xFF060810)).padding(16.dp).navigationBarsPadding()) {
        Text("AI", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(connectedModel?.let { "Connected: $it" } ?: "Not connected", color = if (connectedModel == null) Color(0xFF8EA2C8) else Color(0xFF4EF7FF))
        Spacer(Modifier.height(12.dp))
        AiField("API key", api) { api = it }
        AiField("Base URL", baseUrl) { baseUrl = it }
        AiField("Model", model) { model = it }
        AiField("System prompt", systemPrompt) { systemPrompt = it }
        AiField("Memory preset", memoryPreset) { memoryPreset = it }
        Text("Temperature: ${"%.2f".format(temperature)}", color = Color(0xFF9AB3D6))
        Slider(value = temperature, onValueChange = { temperature = it }, valueRange = 0f..1.2f)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(170.dp).blur(30.dp).background(Brush.radialGradient(listOf(Color(0xFF5E45FF).copy(if (connecting) 0.8f else 0.45f), Color.Transparent)), CircleShape))
            Box(Modifier.size((120 * orbPulse).dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF7B4EFF), Color(0xFF1E2B73))))
                .border(1.dp, Color(0xFF4EF7FF).copy(0.6f), CircleShape)
                .clickable {
                    connecting = true
                    val cfg = AiConnectionConfig(api, baseUrl, model, temperature, systemPrompt, memoryPreset)
                    if (cfg.apiKey.isNotBlank()) { connectedModel = cfg.model }
                    connecting = false
                }, contentAlignment = Alignment.Center) {
                Text(if (connecting) "Connecting" else "Connect", color = Color.White)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Chat history", color = Color.White, fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(16.dp)).background(Color(0xFF0D1324)).padding(12.dp)) {
            Text("Empty. Future: streaming, voice, image and attachments.", color = Color(0xFF8EA2C8))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Provider abstraction ready", color = Color(0xFF4EF7FF))
            Spacer(Modifier.width(8.dp))
            Text("OpenAI-compatible endpoint", color = Color(0xFF8EA2C8))
        }
    }
}

@Composable
private fun AiField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
}
