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

private val ZombieDarkColorScheme =
  darkColorScheme(
    primary = ToxicGreen,
    onPrimary = Color.Black,
    secondary = BloodRed,
    onSecondary = Color.White,
    tertiary = CautionAmber,
    onTertiary = Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceElevated,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = TextMuted,
    outline = MetalGray
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ZombieDarkColorScheme,
    typography = Typography,
    content = content
  )
}
