package com.artleader.mvp.ui.screens.main.messenger

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artleader.mvp.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(vm: MessengerViewModel, modifier: Modifier = Modifier) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { vm.refreshDevices() }
    val enableBtLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { vm.refreshDevices() }

    LaunchedEffect(Unit) { vm.refreshDevices() }

    Scaffold(modifier = modifier.background(Brush.verticalGradient(listOf(Color(0xFF07090F), Color.Black))),
        topBar = {
            TopAppBar(title = { Text("Messenger") }, actions = {
                IconButton(onClick = { vm.refreshDevices() }) { Icon(Icons.Default.Bluetooth, null) }
            })
        }) { p ->
        Column(Modifier.padding(p).fillMaxSize().padding(12.dp)) {
            Text(ui.connectionState, color = Color.Cyan)
            if (!ui.bluetoothEnabled) {
                Button(onClick = {
                    requestPermissions(permissionLauncher)
                    enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }) { Text("Включить Bluetooth") }
            } else {
                Row {
                    Button(onClick = { requestPermissions(permissionLauncher); vm.refreshDevices() }) { Text("Поиск устройств") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { vm.waitForIncoming() }) { Text("Ожидание") }
                }
                Spacer(Modifier.height(8.dp))
                ui.devices.forEach { dev ->
                    OutlinedButton(onClick = { vm.connect(dev) }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(dev.name ?: dev.address)
                    }
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))
            LazyColumn(Modifier.weight(1f), reverseLayout = true) {
                items(messages.reversed()) { msg ->
                    AnimatedVisibility(true) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start) {
                            AssistChip(onClick = {}, label = { Text("${msg.text}  ${msg.timestamp.asTime()}") })
                        }
                    }
                }
            }
            Row {
                OutlinedTextField(text, { text = it }, modifier = Modifier.weight(1f), label = { Text("Сообщение") })
                IconButton(onClick = { if (text.isNotBlank()) { vm.send(text); text = "" } }) { Icon(Icons.Default.Send, null) }
            }
            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
