package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = TataPunchJade,
    onPrimary = TataEvBlack,
    primaryContainer = TataEvCardDark,
    onPrimaryContainer = TataPunchMint,
    secondary = TataPunchMint,
    onSecondary = TataEvBlack,
    secondaryContainer = TataEvCardMid,
    onSecondaryContainer = TataPunchLightGreen,
    tertiary = AmberGold,
    onTertiary = TataEvBlack,
    background = TataEvBlack,
    onBackground = TataEvTextLight,
    surface = TataEvSurface,
    onSurface = TataEvTextLight,
    surfaceVariant = TataEvCardDark,
    onSurfaceVariant = TataEvTextMuted,
    error = CrimsonRed,
    onError = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NeonCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF006064),
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    tertiary = AmberGold,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Slate700,
    error = CrimsonRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Kiosk systems default to sleek dark mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
