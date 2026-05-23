package com.artleader.mvp.ui.screens.main.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ToolsScreen(modifier: Modifier = Modifier) {
    val blocks = remember {
        listOf(
            "Прожить эту грусть" to "Личный плейлист и mood-подборка",
            "Тренироваться в ритме" to "Подбор инструментов на сегодня",
            "Незнакомое" to "Новые идеи и референсы"
        )
    }

    Column(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF2C0735), Color(0xFF10081B), Color.Black)))
            .padding(16.dp)
    ) {
        Text("ART LEADER", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text("Ваша волна", color = Color(0xFFFFE44D), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.background(Brush.horizontalGradient(listOf(Color(0x88FF216B), Color(0x883C8DFF), Color(0x88121426)))).padding(18.dp)) {
                Text("KENTUKKI", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text("Premium home hero для рекомендаций, профиля и быстрых действий.", color = Color(0xFFE1E8FF))
            }
        }

        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(blocks) { (title, subtitle) ->
                Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.background(Color(0x66242B47)).fillMaxWidth().padding(16.dp)) {
                        Column {
                            Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text(subtitle, color = Color(0xFFBFC8EA))
                        }
                    }
                }
            }
        }
    }
}
