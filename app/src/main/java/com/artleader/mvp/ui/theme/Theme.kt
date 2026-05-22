package com.artleader.mvp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonDark = darkColorScheme()

@Composable
fun ArtLeaderTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NeonDark, content = content)
}
