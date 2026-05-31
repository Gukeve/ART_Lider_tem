package com.artleader.mvp.ui.screens.main.messenger

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.bluetooth.mesh.NearbyPeer
import com.artleader.mvp.data.local.entity.ConversationEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.viewmodel.MessengerUiState
import com.artleader.mvp.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Design tokens ────────────────────────────────────────────────────────────
private val Bg         = Color(0xFF07090F)
private val BgCard     = Color(0xFF0E1020)
private val Neon       = Color(0xFFFFE44D)
private val Blue       = Color(0xFF339CFF)
private val Cyan       = Color(0xFF4EF7FF)
private val Dim        = Color(0xFF717DA0)
private val MeBubble   = Brush.linearGradient(listOf(Color(0xFF1E3A6E), Color(0xFF162B55)))
private val PeerBubble = Color(0xFF151825)
private val AvatarPalette = listOf(
    listOf(Color(0xFFFF3D78), Color(0xFF7B2FBE)),
    listOf(Color(0xFF339CFF), Color(0xFF1A2B8C)),
    listOf(Color(0xFFFFE44D), Color(0xFFFF7A00)),
    listOf(Color(0xFF4EF7FF), Color(0xFF1A7AFF)),
)

// ── Root ─────────────────────────────────────────────────────────────────────
@Composable
fun MessengerScreen(vm: MessengerViewModel, modifier: Modifier = Modifier) {
    val context      = LocalContext.current
    val ui           by vm.ui.collectAsStateWithLifecycle()
    val chats        by vm.chats.collectAsStateWithLifecycle()
    val nearbyPeers  by vm.nearbyPeers.collectAsStateWithLifecycle()
    val messages     by vm.messages.collectAsStateWithLifecycle()
    var text         by rememberSaveable { mutableStateOf("") }

    // Permission launcher — request BLE permissions then bind service
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            vm.bindService(context)
        }
    }

    // Bind/unbind service with this composable's lifecycle
    DisposableEffect(Unit) {
        val perms = if (Build.VERSION.SDK_INT >= 31) arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        ) else arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        permLauncher.launch(perms)
        onDispose { vm.unbindService(context) }
    }

    // Ambient glow
    val glowAlpha by rememberInfiniteTransition(label = "mg").animateFloat(
        0.08f, 0.22f,
        infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "ma"
    )

    Box(modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.size(280.dp).blur(90.dp)
            .background(Brush.radialGradient(listOf(Blue.copy(glowAlpha), Color.Transparent)), CircleShape))

        Crossfade(targetState = ui.selectedChatId == null, label = "msg-mode") { isList ->
            if (isList) {
                ChatListPane(ui, chats, nearbyPeers,
                    onSelectChat  = { vm.selectChat(it) },
                    onOpenPrivate = { vm.openPrivateChat(it) }
                )
            } else {
                ChatPane(
                    messages     = messages,
                    text         = text,
                    onTextChange = { text = it },
                    onSend       = { vm.send(text); text = "" },
                    onBack       = { vm.onBackFromChat() },
                    error        = ui.error
                )
            }
        }
    }
}

// ── Chat list ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListPane(
    ui: MessengerUiState,
    chats: List<ConversationEntity>,
    nearby: List<NearbyPeer>,
    onSelectChat: (ConversationEntity) -> Unit,
    onOpenPrivate: (NearbyPeer) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Сообщения", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    // Mesh status indicator
                    Box(
                        Modifier.padding(end = 16.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (ui.meshActive) Cyan.copy(0.15f) else Color.White.copy(0.05f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(6.dp).clip(CircleShape)
                                .background(if (ui.meshActive) Cyan else Color(0xFF444B6B)))
                            Text(ui.statusText, color = if (ui.meshActive) Cyan else Dim, fontSize = 11.sp)
                        }
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Nearby peers strip
            if (nearby.isNotEmpty()) {
                item {
                    Text("Рядом", color = Dim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(nearby) { peer ->
                            NearbyBubble(peer = peer, onClick = { onOpenPrivate(peer) })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Section header for chats
            if (chats.isNotEmpty()) {
                item { Text("Чаты", color = Dim, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp)) }
                items(chats) { chat ->
                    ChatRow(chat, chats.indexOf(chat), onClick = { onSelectChat(chat) })
                }
            } else {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = Dim, modifier = Modifier.size(48.dp))
                        Text("Нет чатов", color = Dim)
                        Text(
                            if (nearby.isEmpty()) "Ищем устройства рядом…"
                            else "Нажмите на аватар рядом, чтобы начать чат",
                            color = Dim.copy(0.6f), fontSize = 12.sp
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun NearbyBubble(peer: NearbyPeer, onClick: () -> Unit) {
    val idx = (peer.peerId.hashCode() and 0xFF) % AvatarPalette.size
    Column(
        Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box {
            Box(
                Modifier.size(52.dp).clip(CircleShape)
                    .background(Brush.linearGradient(AvatarPalette[idx])),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    peer.displayName.firstOrNull()?.toString() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            if (peer.isOnline) {
                Box(Modifier.size(14.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Bg).padding(2.dp)) {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF3DFF8F)))
                }
            }
        }
        Text(peer.displayName.take(10), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChatRow(chat: ConversationEntity, index: Int, onClick: () -> Unit) {
    val colorIdx = index % AvatarPalette.size
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(50.dp).clip(CircleShape).background(Brush.linearGradient(AvatarPalette[colorIdx])),
            contentAlignment = Alignment.Center) {
            Icon(if (chat.type == "group") Icons.Default.Group else Icons.Default.Person,
                null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(chat.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(chat.lastMessage.ifBlank { "Нет сообщений" }, color = Dim, fontSize = 13.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (chat.lastTimestamp > 0) Text(chat.lastTimestamp.asTime(), color = Dim, fontSize = 11.sp)
            if (chat.unreadCount > 0) {
                Box(Modifier.size(20.dp).clip(CircleShape).background(Blue), contentAlignment = Alignment.Center) {
                    Text(chat.unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Chat pane ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatPane(
    messages: List<MessageEntity>,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    error: String?
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Диалог", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0x880D0F1E))
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF0D0F1E))
                    .padding(horizontal = 12.dp, vertical = 10.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text, onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Сообщение…", color = Dim) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Blue.copy(0.6f),
                        unfocusedBorderColor = Color.White.copy(0.10f),
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        cursorColor          = Blue
                    ),
                    maxLines = 4
                )
                AnimatedVisibility(text.isNotBlank(),
                    enter = scaleIn(tween(180)) + fadeIn(tween(180)),
                    exit  = scaleOut(tween(120)) + fadeOut(tween(120))
                ) {
                    IconButton(onClick = onSend,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Blue, Color(0xFF1A4D9C))))) {
                        Icon(Icons.Default.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(messages) { msg ->
                    MessageBubble(msg.encryptedPayload, msg.timestamp.asTime(), msg.isMine, msg.senderName)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
            error?.let {
                Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFF3A0A0A)) {
                    Text(it, color = Color(0xFFFF6B6B))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(text: String, time: String, isMine: Boolean, sender: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        if (!isMine) {
            Text(sender, color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp))
        }
        Box(
            Modifier.widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 18.dp, topEnd = 18.dp,
                    bottomStart = if (isMine) 18.dp else 4.dp,
                    bottomEnd   = if (isMine) 4.dp  else 18.dp
                ))
                .then(if (isMine) Modifier.background(MeBubble) else Modifier.background(PeerBubble))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(text, color = Color.White, fontSize = 15.sp)
                Text(time, color = Color.White.copy(0.4f), fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
            }
        }
    }
}

private fun Long.asTime() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
