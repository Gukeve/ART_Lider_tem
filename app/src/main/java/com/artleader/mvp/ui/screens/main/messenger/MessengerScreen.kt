package com.artleader.mvp.ui.screens.main.messenger

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MessengerViewModel

@Composable
fun MessengerScreen(vm: MessengerViewModel, modifier: Modifier = Modifier) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val chats by vm.chats.collectAsStateWithLifecycle()
    val users by vm.users.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    val devices by vm.nearbyDevices.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.refreshDevices() }

    LaunchedEffect(Unit) { requestPermissions(permissionLauncher); vm.refreshDevices() }

    Scaffold(modifier = modifier.background(Brush.verticalGradient(listOf(Color(0xFF080A14), Color.Black))), topBar = {
        TopAppBar(title = { Text("Messenger") }, actions = {
            TextButton(onClick = { vm.hostSession() }) { Text("Host") }
            TextButton(onClick = { vm.refreshDevices() }) { Text("Scan") }
        })
    }) { p ->
        Column(Modifier.padding(p).fillMaxSize().padding(12.dp)) {
            Text(ui.connectionState, color = Color.Cyan)
            Crossfade(targetState = ui.selectedChatId == null, label = "chat_state") { showList ->
                if (showList) {
                    LazyColumn(Modifier.weight(1f)) {
                        item { Text("Личные чаты") }
                        items(users) { user ->
                            OutlinedButton(onClick = { vm.openPrivateChat(user) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("${user.username} • ${if (user.online) "online" else "offline"}")
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)); Text("Группы") }
                        items(chats.filter { it.type == "group" }) { chat ->
                            ListItem(headlineContent = { Text(chat.title) }, supportingContent = { Text(chat.lastMessage) }, modifier = Modifier.fillMaxWidth())
                            Divider()
                        }
                        item {
                            OutlinedTextField(groupName, { groupName = it }, label = { Text("Новая группа") }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = { if (groupName.isNotBlank()) vm.createGroup(groupName, users.take(3)) }) { Text("Создать группу") }
                        }
                        item { Spacer(Modifier.height(8.dp)); Text("Nearby devices") }
                        items(devices) { dev ->
                            TextButton(onClick = { vm.connectAsClient(dev) }) { Text(dev.name ?: dev.address) }
                        }
                    }
                } else {
                    Column(Modifier.weight(1f)) {
                        LazyColumn(Modifier.weight(1f)) {
                            items(messages) { msg ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start) {
                                    AnimatedVisibility(true) { AssistChip(onClick = {}, label = { Text(msg.encryptedPayload) }) }
                                }
                            }
                        }
                        Row {
                            OutlinedTextField(text, { text = it }, modifier = Modifier.weight(1f), label = { Text("Сообщение") })
                            Button(onClick = { if (text.isNotBlank()) { vm.send(text); text = "" } }) { Text("Send") }
                        }
                    }
                }
            }
        }
    }
}

private fun requestPermissions(launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
    else arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
    launcher.launch(permissions)
}
