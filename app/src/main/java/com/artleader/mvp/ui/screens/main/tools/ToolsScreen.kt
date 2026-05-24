package com.artleader.mvp.ui.screens.main.tools

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Design tokens ────────────────────────────────────────────────────────────
private val BgDark  = Color(0xFF07080F)
private val Neon    = Color(0xFFFFE44D)
private val Pink    = Color(0xFFFF3D78)
private val Blue    = Color(0xFF339CFF)
private val Cyan    = Color(0xFF4EF7FF)
private val Card1   = Color(0xFF12152A)
private val Card2   = Color(0xFF0F1220)
private val TextDim = Color(0xFF8A92B2)

private data class HeroCard(val title: String, val sub: String, val brush: Brush)
private data class QuickAction(val icon: ImageVector, val label: String, val tint: Color)
private data class FeedItem(val title: String, val meta: String, val accent: Color)

@Composable
fun ToolsScreen(modifier: Modifier = Modifier) {
    val heroCards = remember {
        listOf(
            HeroCard(
                "Ваша волна", "Сегодняшний плейлист",
                Brush.linearGradient(listOf(Color(0xFF6B0F6B), Color(0xFF1A0533), Color(0xFF091025)))
            ),
            HeroCard(
                "Новые идеи", "Референсы и тренды",
                Brush.linearGradient(listOf(Color(0xFF0A2A4A), Color(0xFF051020), Color(0xFF091025)))
            ),
            HeroCard(
                "В ритме дня", "Инструменты и задачи",
                Brush.linearGradient(listOf(Color(0xFF1A2B0A), Color(0xFF0E1A05), Color(0xFF091025)))
            ),
        )
    }
    val quickActions = remember {
        listOf(
            QuickAction(Icons.Default.PlayArrow,  "Плейлист", Pink),
            QuickAction(Icons.Default.TrendingUp, "Тренды",   Blue),
            QuickAction(Icons.Default.Whatshot,   "Хайп",     Neon),
            QuickAction(Icons.Default.AutoAwesome,"AI микс",  Cyan),
        )
    }
    val feedItems = remember {
        listOf(
            FeedItem("Мастерство наружной рекламы", "Тренинг • 12 мин", Pink),
            FeedItem("Новые техники монтажа",        "Инструкция • 8 мин", Blue),
            FeedItem("Топ-5 материалов сезона",      "Обзор • 5 мин",     Neon),
            FeedItem("Командный брейнсторм",         "Сессия • 20 мин",   Cyan),
        )
    }

    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.22f, targetValue = 0.52f,
        animationSpec = infiniteRepeatable(
            tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "ga"
    )

    Box(modifier.fillMaxSize().background(BgDark)) {

        // Ambient blobs
        Box(
            Modifier
                .size(340.dp)
                .offset((-60).dp, (-40).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(listOf(Pink.copy(alpha = glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .offset(60.dp, 40.dp)
                .blur(70.dp)
                .background(
                    Brush.radialGradient(listOf(Blue.copy(alpha = glowAlpha * 0.8f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Header
            Column(Modifier.padding(start = 20.dp, top = 52.dp, end = 20.dp, bottom = 8.dp)) {
                Text(
                    "ART LEADER",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp, color = TextDim
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Добро пожаловать",
                    fontSize = 32.sp, fontWeight = FontWeight.Black,
                    color = Color.White, lineHeight = 36.sp
                )
                Text("Ваша вселенная сегодня", color = TextDim, fontSize = 14.sp)
            }

            // Hero carousel
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(heroCards) { card -> HeroCardItem(card) }
            }

            Spacer(Modifier.height(24.dp))

            // Quick actions
            SectionLabel("Быстрый старт")
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickActions) { a -> QuickActionChip(a) }
            }

            Spacer(Modifier.height(24.dp))

            // Feed
            SectionLabel("Рекомендации")
            Column(
                Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                feedItems.forEach { FeedCard(it) }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
        color    = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

@Composable
private fun HeroCardItem(card: HeroCard) {
    Box(
        Modifier
            .widthIn(min = 240.dp, max = 280.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(card.brush)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(Color.White.copy(0.12f), Color.Transparent)),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        // Subtle noise
        Canvas(Modifier.fillMaxSize()) {
            val dot = Color.White.copy(alpha = 0.04f)
            var x = 0f
            while (x <= size.width) {
                var y = 0f
                while (y <= size.height) {
                    drawCircle(dot, 1f, Offset(x, y))
                    y += 8f
                }
                x += 8f
            }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(card.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(card.sub,   color = Color.White.copy(0.6f), fontSize = 13.sp)
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuickActionChip(action: QuickAction) {
    Column(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Card1)
            .border(1.dp, action.tint.copy(0.2f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(action.tint.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, contentDescription = null,
                tint = action.tint, modifier = Modifier.size(22.dp))
        }
        Text(action.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FeedCard(item: FeedItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Card2)
            .border(1.dp, item.accent.copy(0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(item.accent, item.accent.copy(0.2f))))
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.meta, color = TextDim, fontSize = 12.sp)
        }
        Icon(Icons.Default.PlayArrow, contentDescription = null,
            tint = item.accent, modifier = Modifier.size(22.dp))
    }
}