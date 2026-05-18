package com.artleader.mvp.ui.screens.newemployee

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun NewEmployeeScreen() {
    val ctx = LocalContext.current
    val f = remember { mutableStateMapOf<String, String>() }
    var agree by remember { mutableStateOf(false) }
    val fields = listOf("Имя", "Фамилия", "Телефон", "Email", "Должность", "Краткий опыт", "Паспортные данные", "Адрес проживания")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        fields.forEach {
            OutlinedTextField(f[it].orEmpty(), { v -> f[it] = v }, label = { Text(it) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        Row { Checkbox(agree, { agree = it }); Text("Согласен на обработку данных") }
        Button(enabled = agree, onClick = {
            val body = fields.joinToString("%0A") { "$it: ${f[it].orEmpty()}" }
            val uri = Uri.parse("mailto:?subject=Новый сотрудник Art Leader&body=$body")
            ctx.startActivity(Intent(Intent.ACTION_SENDTO, uri))
        }) { Text("Отправить заявку") }
    }
}
