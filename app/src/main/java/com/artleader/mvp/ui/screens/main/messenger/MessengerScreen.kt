package com.artleader.mvp.ui.screens.main.messenger

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.data.local.entity.ConversationEntity
import com.artleader.mvp.data.local.entity.MessageEntity
import com.artleader.mvp.data.local.entity.PeerEntity
import com.artleader.mvp.viewmodel.MessengerUiState
import com.artleader.mvp.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Design tokens ────────────────────────────────────────────────────────────
private val Bg         = Color(0xFF07090F)
private val BgCard     = Color(0xFF0E1020)
private val Neon       = Color(0xFFFFE44D)
private val Blue       = Color(0xFF339CFF)
private val Pink       = Color(0xFFFF3D78)
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

// ─── Root ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(vm: MessengerViewModel, modifier: Modifier = Modifier) {
    val ui       by vm.ui.collectAsStateWithLifecycle()
    val chats    by vm.chats.collectAsStateWithLifecycle()
    val users    by vm.users.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    var text     by remember { mutableStateOf("") }
    var showDiscovery by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.refreshDevices() }
    val btLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { vm.refreshDevices() }

    LaunchedEffect(Unit) { vm.refreshDevices() }

    // Ambient glow
    val glowAlpha by rememberInfiniteTransition(label = "mg").animateFloat(
        initialValue = 0.10f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ma"
    )

    Box(modifier.fillMaxSize().background(Bg)) {

        Box(
            Modifier
                .size(280.dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(listOf(Blue.copy(glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )

        Crossfade(targetState = ui.selectedChatId == null, label = "msg-mode") { isList ->
            if (isList) {
                ChatListPane(
                    ui           = ui,
                    chats        = chats,
                    users        = users,
                    onSelectChat = { vm.selectChat(it) },
                    onOpenPrivate = { vm.openPrivateChat(it) },
                    onNewMessage = { vm.createNewMessage() },
                    onDiscovery  = { showDiscovery = true },
                    onEnableBt   = {
                        requestBtPermissions(permLauncher)
                        btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
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

    if (showDiscovery) {
        ModalBottomSheet(
            onDismissRequest = { showDiscovery = false },
            containerColor   = Color(0xFF0F1222),
            shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            DiscoverySheet(
                enabled   = ui.bluetoothEnabled,
                status    = ui.connectionState,
                devices   = ui.devices,
                onConnect = { vm.connect(it); showDiscovery = false },
                onHost    = { vm.waitForIncoming(); showDiscovery = false }
            )
        }
    }
}

// ─── Chat list pane ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListPane(
    ui: MessengerUiState,
    chats: List<ConversationEntity>,
    users: List<PeerEntity>,
    onSelectChat: (ConversationEntity) -> Unit,
    onOpenPrivate: (PeerEntity) -> Unit,
    onDiscovery: () -> Unit,
    onEnableBt: () -> Unit,
    onNewMessage: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text("Сообщения", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onDiscovery) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Discover", tint = Neon)
                    }
                    IconButton(onClick = onNewMessage) {
                        Icon(Icons.Default.Edit, contentDescription = "New chat", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Bluetooth status row
            item {
                BtStatusRow(
                    enabled    = ui.bluetoothEnabled,
                    status     = ui.connectionState,
                    onEnable   = onEnableBt
                )
                Spacer(Modifier.height(12.dp))
            }

            // Nearby users strip
            if (users.isNotEmpty()) {
                item {
                    Text(
                        text     = "Nearby",
                        color    = Dim,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // LazyRow inside a LazyColumn item — correct usage
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding        = PaddingValues(end = 8.dp)
                    ) {
                        items(users) { u ->
                            NearbyBubble(user = u, onClick = { onOpenPrivate(u) })
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text     = "Чаты",
                        color    = Dim,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Empty state
            if (chats.isEmpty()) {
                item {
                    Column(
                        modifier              = Modifier.fillMaxWidth().padding(top = 60.dp),
                        horizontalAlignment   = Alignment.CenterHorizontally,
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint               = Dim,
                            modifier           = Modifier.size(48.dp)
                        )
                        Text("Нет чатов", color = Dim)
                        Text(
                            text     = "Нажмите Bluetooth-иконку, чтобы найти собеседников",
                            color    = Dim.copy(0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(chats) { chat ->
                    ChatRow(
                        title   = chat.title,
                        sub     = if (chat.type == "group") "Группа"
                        else chat.lastMessage.ifBlank { "Нет сообщений" },
                        isGroup = chat.type == "group",
                        unread  = chat.unreadCount,
                        time    = if (chat.lastTimestamp > 0) chat.lastTimestamp.asTime() else "",
                        index   = chats.indexOf(chat),
                        onClick = { onSelectChat(chat) }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun BtStatusRow(enabled: Boolean, status: String, onEnable: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Blue.copy(0.10f) else Color.White.copy(0.05f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment         = Alignment.CenterVertically,
        horizontalArrangement     = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (enabled) Cyan else Color(0xFF444B6B))
            )
            Text(
                text  = status,
                color = if (enabled) Cyan else Dim,
                fontSize = 13.sp
            )
        }
        if (!enabled) {
            TextButton(
                onClick          = onEnable,
                contentPadding   = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Включить", color = Blue, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun NearbyBubble(user: PeerEntity, onClick: () -> Unit) {
    val idx = (user.peerId.hashCode() and 0xFF) % AvatarPalette.size
    Column(
        modifier              = Modifier.clickable(onClick = onClick),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(6.dp)
    ) {
        Box {
            Box(
                modifier            = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(AvatarPalette[idx])),
                contentAlignment    = Alignment.Center
            ) {
                Text(
                    text       = user.username.take(1).uppercase(),
                    color      = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize   = 20.sp
                )
            }
            if (user.online) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Bg)
                        .padding(2.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF3DFF8F))
                    )
                }
            }
        }
        Text(user.username, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChatRow(
    title: String, sub: String, isGroup: Boolean,
    unread: Int, time: String, index: Int, onClick: () -> Unit
) {
    val colorIdx = index % AvatarPalette.size
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(AvatarPalette[colorIdx])),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = if (isGroup) Icons.Default.Group else Icons.Default.Person,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(24.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(sub,   color = Dim,         fontSize = 13.sp, maxLines = 1)
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (time.isNotBlank()) Text(time, color = Dim, fontSize = 11.sp)
            if (unread > 0) {
                Box(
                    modifier         = Modifier.size(20.dp).clip(CircleShape).background(Blue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(unread.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Chat pane ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatPane(
    messages: List<MessageEntity>,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    error: String?,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Диалог", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0x880D0F1E))
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0F1E))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = text,
                    onValueChange = onTextChange,
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text("Сообщение…", color = Dim) },
                    shape         = RoundedCornerShape(24.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Blue.copy(0.6f),
                        unfocusedBorderColor = Color.White.copy(0.10f),
                        focusedTextColor     = Color.White,
                        unfocusedTextColor   = Color.White,
                        cursorColor          = Blue
                    ),
                    maxLines = 4
                )
                AnimatedVisibility(
                    visible = text.isNotBlank(),
                    enter   = scaleIn(tween(180)) + fadeIn(tween(180)),
                    exit    = scaleOut(tween(120)) + fadeOut(tween(120))
                ) {
                    IconButton(
                        onClick  = onSend,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Blue, Color(0xFF1A4D9C))))
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Send,
                            contentDescription = "Send",
                            tint               = Color.White,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                state                 = listState,
                modifier              = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        text    = msg.encryptedPayload,
                        time    = msg.timestamp.asTime(),
                        isMine  = msg.isMine,
                        sender  = msg.senderName
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            error?.let {
                Snackbar(
                    modifier         = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor   = Color(0xFF3A0A0A)
                ) { Text(it, color = Color(0xFFFF6B6B)) }
            }
        }
    }
}

@Composable
private fun MessageBubble(text: String, time: String, isMine: Boolean, sender: String) {
    val cornerShape = RoundedCornerShape(
        topStart    = 18.dp, topEnd    = 18.dp,
        bottomStart = if (isMine) 18.dp else 4.dp,
        bottomEnd   = if (isMine) 4.dp  else 18.dp
    )
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        if (!isMine) {
            Text(
                text     = sender,
                color    = Blue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(cornerShape)
                .then(
                    if (isMine) Modifier.background(MeBubble)
                    else        Modifier.background(PeerBubble)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(text, color = Color.White, fontSize = 15.sp)
                Text(
                    text     = time,
                    color    = Color.White.copy(0.4f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ─── BT Discovery sheet ───────────────────────────────────────────────────────

@Composable
private fun DiscoverySheet(
    enabled: Boolean,
    status: String,
    devices: List<BluetoothDevice>,
    onConnect: (BluetoothDevice) -> Unit,
    onHost: () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Найти устройства", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(status, color = Cyan, fontSize = 13.sp)

        OutlinedButton(
            onClick = onHost,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(14.dp),
            border   = BorderStroke(1.dp, Blue.copy(0.5f))
        ) {
            Icon(Icons.Default.BluetoothSearching, contentDescription = null, tint = Blue)
            Spacer(Modifier.width(8.dp))
            Text("Ждать входящих", color = Blue)
        }

        if (devices.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет сопряжённых устройств", color = Dim)
            }
        } else {
            devices.forEach { dev ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgCard)
                        .padding(14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        @Suppress("MissingPermission")
                        Text(dev.name ?: "Unknown", color = Color.White, fontWeight = FontWeight.Medium)
                        Text(dev.address, color = Dim, fontSize = 12.sp)
                    }
                    TextButton(onClick = { onConnect(dev) }) {
                        Text("Подключить", color = Neon)
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun requestBtPermissions(launcher: ActivityResultLauncher<Array<String>>) {
    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
    }
    launcher.launch(perms)
}

private fun Long.asTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))