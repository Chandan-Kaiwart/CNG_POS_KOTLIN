package com.rsgl.cngpos


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E3A8A),
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFF5f259f),
    background = Color(0xFFF0F8FF),
    surface = Color.White
)

@Composable
fun POSAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}