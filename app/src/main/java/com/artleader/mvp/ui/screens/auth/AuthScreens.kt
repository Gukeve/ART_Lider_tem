package com.artleader.mvp.ui.screens.auth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.artleader.mvp.data.preferences.UserSession
import com.artleader.mvp.viewmodel.MainViewModel

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onNewEmployee: () -> Unit,
    session: UserSession,
    onBiometric: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF121830))))
    ) {
        Column(
            modifier             = Modifier.align(Alignment.Center),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "Art Leader",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Cyan
            )
            Text("Добро пожаловать в Art Leader", color = Color.White.copy(0.7f))
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onLogin,
                shape   = RoundedCornerShape(12.dp)
            ) {
                Text("Войти")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onNewEmployee) {
                Text("Новый сотрудник")
            }
            if (session.biometricEnabled && session.hasEncryptedSession) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBiometric) {
                    Text("Войти через биометрию")
                }
            }
        }
    }
}

@Composable
fun LoginScreen(vm: MainViewModel, onSuccess: () -> Unit) {
    // rememberSaveable so values survive config change
    var login     by rememberSaveable { mutableStateOf("") }
    var password  by rememberSaveable { mutableStateOf("") }
    var biometric by rememberSaveable { mutableStateOf(true) }
    var error     by remember { mutableStateOf(false) }
    // Guard against double-tap submitting twice
    var loading   by remember { mutableStateOf(false) }

    Column(
        modifier              = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF121830))))
            .padding(20.dp),
        verticalArrangement   = Arrangement.Center
    ) {
        Text(
            text  = "Вход",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value         = login,
            onValueChange = { login = it; error = false },
            label         = { Text("Логин") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = password,
            onValueChange = { password = it; error = false },
            label         = { Text("Пароль") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = biometric, onCheckedChange = { biometric = it })
            Text("Разрешить биометрию", color = Color.White.copy(0.8f))
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick  = {
                if (!loading) {
                    loading = true
                    error   = false
                    vm.login(login, password, biometric) { ok ->
                        loading = false
                        if (ok) {
                            onSuccess()
                        } else {
                            error = true
                        }
                    }
                }
            },
            enabled  = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Проверка…" else "Войти")
        }
        if (error) {
            Spacer(Modifier.height(8.dp))
            Text("Неверные данные", color = MaterialTheme.colorScheme.error)
        }
    }
}