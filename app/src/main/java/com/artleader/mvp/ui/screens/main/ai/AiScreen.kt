package com.artleader.mvp.ui.screens.main.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MainViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─── Tokens ───────────────────────────────────────────────────────────────────
private val Bg       = Color(0xFF060810)
private val Neon     = Color(0xFFFFE44D)
private val Blue     = Color(0xFF339CFF)
private val Cyan     = Color(0xFF4EF7FF)
private val Purple   = Color(0xFF7B4EFF)
private val Dim      = Color(0xFF6A73A0)
private val BubbleAI = Color(0xFF10182E)
private val BubbleMe = Color(0xFF0D1F3E)

private data class Suggestion(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showKeyDialog by remember { mutableStateOf(false) }
    var key by remember(settings.apiKey) { mutableStateOf(settings.apiKey) }

    val demoMessages = remember {
        listOf(
            "me" to "Привет! Что ты умеешь?",
            "ai" to "Привет! Я AI-ассистент Art Leader. Могу помочь с проектами, идеями и анализом задач.",
            "me" to "Придумай слоган для команды",
            "ai" to "«Мы не просто создаём рекламу — мы оставляем след в городе.» Хочешь ещё варианты?",
        )
    }

    val suggestions = remember {
        listOf(
            Suggestion("Идея",         Icons.Default.AutoAwesome),
            Suggestion("Анализ",       Icons.Default.Analytics),
            Suggestion("Текст",        Icons.Default.Create),
            Suggestion("Брейнсторм",   Icons.Default.Bolt),
        )
    }

    // Orb rotation
    val orbPhase by rememberInfiniteTransition(label = "orb").animateFloat(
        initialValue = 0f,
        targetValue  = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label        = "op"
    )
    // Breathing glow
    val glowAlpha by rememberInfiniteTransition(label = "ga").animateFloat(
        initialValue = 0.28f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "gv"
    )

    Box(modifier.fillMaxSize().background(Bg)) {

        // Ambient blobs
        Box(
            Modifier.size(360.dp).align(Alignment.TopCenter).blur(100.dp)
                .background(Brush.radialGradient(listOf(Purple.copy(glowAlpha * 0.4f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.size(200.dp).align(Alignment.BottomStart).blur(70.dp)
                .background(Brush.radialGradient(listOf(Blue.copy(glowAlpha * 0.3f), Color.Transparent)), CircleShape)
        )

        Column(Modifier.fillMaxSize()) {

            // Top bar
            TopAppBar(
                title  = { Text("AI Ассистент", fontWeight = FontWeight.Black, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { showKeyDialog = true }) {
                        Icon(
                            Icons.Default.Key,
                            contentDescription = "API key",
                            tint = if (settings.apiKey.isBlank()) Dim else Neon
                        )
                    }
                }
            )

            // Orb hero
            Box(
                Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rotating ring via Canvas
                androidx.compose.foundation.Canvas(
                    Modifier.size(160.dp).graphicsLayer {
                        rotationZ = orbPhase * (180f / PI.toFloat())
                    }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r  = size.minDimension / 2f
                    drawCircle(
                        brush  = Brush.sweepGradient(
                            listOf(Purple.copy(0.85f), Cyan.copy(0.5f), Purple.copy(0f))
                        ),
                        radius = r,
                        style  = Stroke(width = 2.5f)
                    )
                    // Orbiting dot
                    drawCircle(
                        color  = Cyan,
                        radius = 6f,
                        center = Offset(
                            cx + (r - 2f) * cos(orbPhase),
                            cy + (r - 2f) * sin(orbPhase)
                        )
                    )
                }
                // Inner orb
                Box(
                    Modifier.size(80.dp).clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Purple.copy(0.9f), Blue.copy(0.6f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(34.dp))
                }
            }

            // Status line
            Text(
                text     = if (settings.apiKey.isBlank()) "Подключите API для полного доступа" else "Готов к работе",
                color    = if (settings.apiKey.isBlank()) Dim else Cyan,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp)
            )

            // Suggestion chips
            LazyRow(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { s ->
                    SuggestionChip(
                        label = { Text(s.label, fontSize = 12.sp) },
                        onClick = {},
                        icon  = { Icon(s.icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor  = Purple.copy(0.12f),
                            labelColor      = Color.White,
                            iconContentColor = Purple
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled     = true,
                            borderColor = Purple.copy(0.3f),
                            borderWidth = 1.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color.White.copy(0.05f), modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            // Demo chat feed
            LazyColumn(
                modifier            = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(demoMessages) { (role, text) ->
                    AiChatBubble(text = text, isAi = role == "ai")
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0E1C))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = "",
                    onValueChange = {},
                    modifier      = Modifier.weight(1f),
                    placeholder   = {
                        Text(
                            if (settings.apiKey.isBlank()) "Добавьте API ключ…" else "Спросите что-нибудь…",
                            color = Dim, fontSize = 14.sp
                        )
                    },
                    shape   = RoundedCornerShape(24.dp),
                    colors  = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Purple.copy(0.6f),
                        unfocusedBorderColor = Color.White.copy(0.08f),
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White
                    ),
                    enabled = settings.apiKey.isNotBlank()
                )
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(
                            if (settings.apiKey.isNotBlank())
                                Brush.linearGradient(listOf(Purple, Blue))
                            else
                                Brush.linearGradient(listOf(Color(0xFF1A1D2E), Color(0xFF1A1D2E)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint     = if (settings.apiKey.isNotBlank()) Color.White else Dim,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // API key dialog
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            containerColor   = Color(0xFF10152A),
            shape            = RoundedCornerShape(28.dp),
            icon             = { Icon(Icons.Default.Key, contentDescription = null, tint = Neon) },
            title            = { Text("API ключ", color = Color.White) },
            text = {
                OutlinedTextField(
                    value         = key,
                    onValueChange = { key = it },
                    label         = { Text("Anthropic API key") },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Purple,
                        focusedLabelColor    = Purple,
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = { vm.setApiKey(key); showKeyDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = Purple)
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showKeyDialog = false }) {
                    Text("Отмена", color = Dim)
                }
            }
        )
    }
}

@Composable
private fun AiChatBubble(text: String, isAi: Boolean) {
    val cornerShape = RoundedCornerShape(
        topStart    = 18.dp, topEnd    = 18.dp,
        bottomStart = if (isAi) 4.dp else 18.dp,
        bottomEnd   = if (isAi) 18.dp else 4.dp
    )
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
    ) {
        if (isAi) {
            Box(
                Modifier.size(32.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Purple, Blue))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null,
                    tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        }
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .clip(cornerShape)
                .background(if (isAi) BubbleAI else BubbleMe)
                .border(
                    1.dp,
                    if (isAi) Purple.copy(0.2f) else Blue.copy(0.2f),
                    cornerShape
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}