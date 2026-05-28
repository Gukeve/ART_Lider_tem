package com.artleader.mvp.ui.screens.main.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// ── AI providers ──────────────────────────────────────────────────────────────
enum class AiProvider(val label: String, val defaultUrl: String, val defaultModel: String) {
    OPENAI("OpenAI",        "https://api.openai.com/v1",         "gpt-4.1-mini"),
    ANTHROPIC("Claude",     "https://api.anthropic.com/v1",      "claude-sonnet-4-20250514"),
    GEMINI("Gemini",        "https://generativelanguage.googleapis.com/v1beta", "gemini-2.0-flash"),
    OLLAMA("Ollama (local)","http://localhost:11434/v1",          "llama3"),
    CUSTOM("Custom",        "",                                   "")
}

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

data class ChatMessage(val role: String, val content: String)  // role: "user" | "assistant"

@Composable
fun AiScreen(
    @Suppress("UNUSED_PARAMETER") vm: MainViewModel,
    modifier: Modifier = Modifier
) {
    // ── Connection config (persisted through recomposition) ───────────────────
    var provider     by rememberSaveable { mutableStateOf(AiProvider.OPENAI.name) }
    var apiKey       by rememberSaveable { mutableStateOf("") }
    var baseUrl      by rememberSaveable { mutableStateOf(AiProvider.OPENAI.defaultUrl) }
    var model        by rememberSaveable { mutableStateOf(AiProvider.OPENAI.defaultModel) }
    var systemPrompt by rememberSaveable { mutableStateOf("You are Art Leader assistant. Be concise and helpful.") }
    var temperature  by remember { mutableFloatStateOf(0.7f) }

    // ── Connection state ──────────────────────────────────────────────────────
    var connState    by remember { mutableStateOf(ConnectionState.DISCONNECTED) }
    var errorMsg     by remember { mutableStateOf("") }
    var showConfig   by remember { mutableStateOf(true) }

    // ── Chat state ────────────────────────────────────────────────────────────
    val messages     = remember { mutableStateListOf<ChatMessage>() }
    var inputText    by remember { mutableStateOf("") }
    var isStreaming  by remember { mutableStateOf(false) }
    val listState    = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    // Orb pulse animation
    val orbPulse by rememberInfiniteTransition(label = "orb").animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(
            tween(if (connState == ConnectionState.CONNECTING) 500 else 1400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), "op"
    )
    val orbGlow by rememberInfiniteTransition(label = "og").animateFloat(
        0.3f,
        if (connState == ConnectionState.CONNECTED) 0.8f else 0.45f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "ogv"
    )

    val selectedProvider = AiProvider.entries.firstOrNull { it.name == provider } ?: AiProvider.OPENAI

    Column(
        modifier
            .fillMaxSize()
            .background(Color(0xFF060810))
            .navigationBarsPadding()
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("AI Ассистент", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(
                    when (connState) {
                        ConnectionState.DISCONNECTED -> "Не подключено"
                        ConnectionState.CONNECTING   -> "Подключение…"
                        ConnectionState.CONNECTED    -> "● ${selectedProvider.label} · $model"
                        ConnectionState.ERROR        -> "Ошибка: $errorMsg"
                    },
                    color = when (connState) {
                        ConnectionState.CONNECTED    -> Color(0xFF4EF7FF)
                        ConnectionState.ERROR        -> Color(0xFFFF6B6B)
                        ConnectionState.CONNECTING   -> Color(0xFFFFE44D)
                        else                         -> Color(0xFF8EA2C8)
                    },
                    fontSize = 12.sp
                )
            }
            // Toggle config panel
            Box(
                Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.06f))
                    .clickable { showConfig = !showConfig }.padding(8.dp)
            ) {
                Icon(if (showConfig) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
            }
        }

        // ── Collapsible config panel ──────────────────────────────────────────
        AnimatedVisibility(showConfig) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0D1022))
                    .border(1.dp, Color.White.copy(0.06f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Provider selector
                ProviderSelector(selectedProvider) { p ->
                    provider = p.name
                    baseUrl  = if (p != AiProvider.CUSTOM) p.defaultUrl else baseUrl
                    model    = if (p != AiProvider.CUSTOM) p.defaultModel else model
                }

                AiField("API ключ", apiKey, { apiKey = it })
                AiField("Base URL", baseUrl, { baseUrl = it })
                AiField("Модель",  model,    { model  = it })
                AiField("Системный промпт", systemPrompt, { systemPrompt = it })

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Температура: ${"%.2f".format(temperature)}", color = Color(0xFF9AB3D6), fontSize = 13.sp)
                }
                Slider(temperature, { temperature = it }, valueRange = 0f..1.2f)
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Orb ───────────────────────────────────────────────────────────────
        Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(130.dp).blur(28.dp)
                .background(Brush.radialGradient(listOf(
                    when (connState) {
                        ConnectionState.CONNECTED  -> Color(0xFF4EF7FF).copy(orbGlow * 0.6f)
                        ConnectionState.ERROR      -> Color(0xFFFF3D78).copy(0.4f)
                        else                       -> Color(0xFF5E45FF).copy(orbGlow * 0.45f)
                    },
                    Color.Transparent
                )), CircleShape))
            Box(
                Modifier.size((88 * orbPulse).dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(
                        when (connState) {
                            ConnectionState.CONNECTED  -> Color(0xFF1A4A5A)
                            ConnectionState.ERROR      -> Color(0xFF3A0A1A)
                            else                       -> Color(0xFF7B4EFF)
                        },
                        Color(0xFF1E2B73)
                    )))
                    .border(1.5.dp,
                        when (connState) {
                            ConnectionState.CONNECTED -> Color(0xFF4EF7FF).copy(0.8f)
                            ConnectionState.ERROR     -> Color(0xFFFF3D78).copy(0.7f)
                            else                      -> Color(0xFF4EF7FF).copy(0.5f)
                        },
                        CircleShape)
                    .clickable(enabled = connState != ConnectionState.CONNECTING) {
                        if (connState == ConnectionState.CONNECTED) {
                            connState = ConnectionState.DISCONNECTED
                        } else {
                            connState = ConnectionState.CONNECTING
                            errorMsg  = ""
                            // Simple connectivity test
                            testConnection(baseUrl, apiKey, model) { ok, err ->
                                connState = if (ok) ConnectionState.CONNECTED else ConnectionState.ERROR
                                if (!ok) errorMsg = err
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (connState == ConnectionState.CONNECTED) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF4EF7FF), modifier = Modifier.size(28.dp))
                } else {
                    Text(
                        when (connState) {
                            ConnectionState.CONNECTING   -> "…"
                            ConnectionState.ERROR        -> "Retry"
                            else                         -> "Connect"
                        },
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Chat area ─────────────────────────────────────────────────────────
        LazyColumn(
            state    = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (connState == ConnectionState.CONNECTED)
                                "Начните диалог…"
                            else
                                "Подключите AI провайдера чтобы начать",
                            color = Color(0xFF3A4A6A), fontSize = 14.sp
                        )
                    }
                }
            }
            items(messages) { msg -> AiMessageBubble(msg) }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Input bar ─────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF0A0C18))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = inputText,
                onValueChange = { inputText = it },
                modifier      = Modifier.weight(1f),
                placeholder   = { Text("Сообщение…", color = Color(0xFF3A4A6A)) },
                shape         = RoundedCornerShape(20.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color(0xFF4EF7FF).copy(0.5f),
                    unfocusedBorderColor = Color.White.copy(0.08f),
                    focusedTextColor     = Color.White,
                    unfocusedTextColor   = Color.White,
                    cursorColor          = Color(0xFF4EF7FF)
                ),
                maxLines = 4,
                enabled  = connState == ConnectionState.CONNECTED && !isStreaming
            )
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .background(
                        if (connState == ConnectionState.CONNECTED && inputText.isNotBlank())
                            Brush.linearGradient(listOf(Color(0xFF5E45FF), Color(0xFF2C9BFF)))
                        else
                            Brush.linearGradient(listOf(Color(0xFF1A2040), Color(0xFF1A2040)))
                    )
                    .clickable(enabled = connState == ConnectionState.CONNECTED && inputText.isNotBlank() && !isStreaming) {
                        val userMsg = inputText.trim()
                        if (userMsg.isBlank()) return@clickable
                        messages.add(ChatMessage("user", userMsg))
                        inputText  = ""
                        isStreaming = true

                        sendMessage(
                            baseUrl      = baseUrl,
                            apiKey       = apiKey,
                            model        = model,
                            systemPrompt = systemPrompt,
                            temperature  = temperature,
                            history      = messages.toList()
                        ) { chunk, done ->
                            if (messages.lastOrNull()?.role == "assistant") {
                                val last = messages.removeLast()
                                messages.add(last.copy(content = last.content + chunk))
                            } else {
                                messages.add(ChatMessage("assistant", chunk))
                            }
                            if (done) isStreaming = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("→", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Provider selector ─────────────────────────────────────────────────────────
@Composable
private fun ProviderSelector(selected: AiProvider, onSelect: (AiProvider) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(0.04f))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(selected.label, color = Color.White, fontSize = 14.sp)
            Icon(Icons.Default.ExpandMore, null, tint = Color.White.copy(0.5f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF0D1022))) {
            AiProvider.entries.forEach { p ->
                DropdownMenuItem(
                    text = { Text(p.label, color = if (p == selected) Color(0xFF4EF7FF) else Color.White) },
                    onClick = { onSelect(p); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun AiMessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
        Box(
            Modifier
                .clip(RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd   = if (isUser) 4.dp  else 16.dp
                ))
                .background(
                    if (isUser) Brush.linearGradient(listOf(Color(0xFF1E3A6E), Color(0xFF162B55)))
                    else        Brush.linearGradient(listOf(Color(0xFF151825), Color(0xFF0F1020)))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(msg.content, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun AiField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier  = Modifier.fillMaxWidth(),
        singleLine = label != "Системный промпт",
        maxLines  = if (label == "Системный промпт") 3 else 1,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color(0xFF4EF7FF).copy(0.5f),
            unfocusedBorderColor = Color.White.copy(0.1f),
            focusedTextColor     = Color.White,
            unfocusedTextColor   = Color.White,
            focusedLabelColor    = Color(0xFF4EF7FF),
            unfocusedLabelColor  = Color(0xFF6A7A9A),
            cursorColor          = Color(0xFF4EF7FF)
        )
    )
}

// ── Network helpers (OpenAI-compatible) ───────────────────────────────────────

private fun testConnection(
    baseUrl: String,
    apiKey: String,
    model: String,
    callback: (Boolean, String) -> Unit
) {
    Thread {
        try {
            val url  = URL("$baseUrl/models")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.connectTimeout = 5000
            conn.readTimeout    = 5000
            val code = conn.responseCode
            conn.disconnect()
            callback(code in 200..299, "HTTP $code")
        } catch (e: Exception) {
            callback(false, e.message?.take(40) ?: "Network error")
        }
    }.start()
}

private fun sendMessage(
    baseUrl: String,
    apiKey: String,
    model: String,
    systemPrompt: String,
    temperature: Float,
    history: List<ChatMessage>,
    onChunk: (String, Boolean) -> Unit
) {
    Thread {
        try {
            val msgs = buildString {
                append("[")
                append("""{"role":"system","content":${jsonString(systemPrompt)}}""")
                history.forEach { m ->
                    append(""",{"role":"${m.role}","content":${jsonString(m.content)}}""")
                }
                append("]")
            }
            val body = """{"model":${jsonString(model)},"messages":$msgs,"temperature":$temperature,"stream":false}"""
            val url  = URL("$baseUrl/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.connectTimeout = 30_000
            conn.readTimeout    = 60_000
            conn.outputStream.write(body.toByteArray())
            val resp = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            // Parse: extract choices[0].message.content
            val content = extractContent(resp)
            onChunk(content, true)
        } catch (e: Exception) {
            onChunk("[Ошибка: ${e.message?.take(60)}]", true)
        }
    }.start()
}

private fun jsonString(s: String) = "\"${s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""

private fun extractContent(json: String): String {
    return try {
        val marker  = "\"content\":"
        val start   = json.indexOf(marker) + marker.length
        val trimmed = json.substring(start).trimStart()
        if (trimmed.startsWith("\"")) {
            val end = trimmed.indexOf("\"", 1)
            trimmed.substring(1, end)
                .replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\")
        } else "…"
    } catch (_: Exception) { "[Ошибка разбора ответа]" }
}
