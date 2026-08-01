package com.projectdreams.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF7C4DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB89CFF),
    onPrimary = Color(0xFF2A0058),
    primaryContainer = Color(0xFF4A2A8E),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF141218),
    surface = Color(0xFF141218)
)

@Composable
fun ProjectDreamsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
