package com.artleader.mvp.ui.screens.main.messenger

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessengerScreen(vm: MessengerViewModel, modifier: Modifier = Modifier) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val chats by vm.chats.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.refreshDevices() }
    val enableBtLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { vm.refreshDevices() }

    LaunchedEffect(Unit) { vm.refreshDevices() }

    Scaffold(
        modifier = modifier.background(Brush.verticalGradient(listOf(Color(0xFF0A0E1A), Color(0xFF111326), Color.Black))),
        topBar = {
            TopAppBar(
                title = { Text(if (ui.selectedChatId == null) "Messenger" else "Диалог") },
                navigationIcon = {
                    if (ui.selectedChatId != null) IconButton(onClick = { vm.refreshDevices() }) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = { IconButton(onClick = { vm.refreshDevices() }) { Icon(Icons.Default.Bluetooth, null, tint = Color(0xFFFFE44D)) } }
            )
        },
        floatingActionButton = {
            if (ui.selectedChatId == null) {
                FloatingActionButton(onClick = { vm.waitForIncoming() }, containerColor = Color(0xFF2B2F57)) {
                    Icon(Icons.Default.Bluetooth, null)
                }
            }
        }
    ) { p ->
        Crossfade(targetState = ui.selectedChatId == null, modifier = Modifier.padding(p), label = "messenger-mode") { inChatList ->
            if (inChatList) {
                Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(ui.connectionState, color = Color(0xFF8FE3FF))
                    if (!ui.bluetoothEnabled) {
                        OutlinedButton(onClick = {
                            requestPermissions(permissionLauncher)
                            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }) { Text("Включить Bluetooth") }
                    }
                    Text("Nearby users", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    ui.devices.forEach { dev ->
                        Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(dev.name ?: "Unknown peer", color = Color.White)
                                    Text(dev.address, color = Color(0xFF9EA8C5), style = MaterialTheme.typography.bodySmall)
                                }
                                OutlinedButton(onClick = { vm.connect(dev) }) { Text("Connect") }
                            }
                        }
                    }
                    Text("Чаты", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chats) { chat ->
                            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(chat.title, color = Color.White)
                                        Text(if (chat.type == "group") "Групповой чат" else "Личный чат", color = Color(0xFFB8C1DE))
                                    }
                                    OutlinedButton(onClick = { vm.selectChat(chat) }) { Text("Open") }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(12.dp)) {
                    LazyColumn(Modifier.weight(1f), reverseLayout = true, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(messages.reversed()) { msg ->
                            AnimatedVisibility(true) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start) {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("${msg.encryptedPayload}  ${msg.timestamp.asTime()}") }
                                    )
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(text, { text = it }, modifier = Modifier.weight(1f), label = { Text("Сообщение") })
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { if (text.isNotBlank()) { vm.send(text); text = "" } }) { Icon(Icons.Default.Send, null) }
                    }
                    ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

private fun requestPermissions(launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    } else {
        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
    }
    launcher.launch(permissions)
}

private fun Long.asTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
