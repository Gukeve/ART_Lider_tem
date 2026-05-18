package com.artleader.mvp.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.artleader.mvp.viewmodel.MainViewModel

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onNewEmployee: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF121830))))) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Art Leader", style = MaterialTheme.typography.headlineLarge, color = Color.Cyan)
            Text("Добро пожаловать в Art Leader")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onLogin, shape = RoundedCornerShape(12.dp)) { Text("Войти") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onNewEmployee) { Text("Новый сотрудник") }
        }
    }
}

@Composable
fun LoginScreen(vm: MainViewModel, onSuccess: () -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
        OutlinedTextField(login, { login = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.login(login, password) { ok -> if (ok) onSuccess() else error = true } }, modifier = Modifier.fillMaxWidth()) { Text("Войти") }
        if (error) Text("Неверные данные", color = MaterialTheme.colorScheme.error)
    }
}
