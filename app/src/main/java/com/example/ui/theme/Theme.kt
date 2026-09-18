package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GuerreirosColorScheme = darkColorScheme(
  primary = BlazeOrange,
  onPrimary = Color.White,
  primaryContainer = OrangeContainer,
  onPrimaryContainer = OnOrangeContainer,
  secondary = DivineGold,
  onSecondary = Color(0xFF1F1600),
  secondaryContainer = GoldContainer,
  onSecondaryContainer = OnGoldContainer,
  tertiary = AuraFlame,
  onTertiary = Color.White,
  background = DeepBlack,
  onBackground = TextPrimary,
  surface = CharcoalBlack,
  onSurface = TextPrimary,
  surfaceVariant = CardSurfaceDark,
  onSurfaceVariant = TextSecondary,
  outline = BorderDark,
  outlineVariant = CardSurfaceElevated
)

@Composable
fun GuerreirosTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = GuerreirosColorScheme,
    typography = Typography,
    content = content
  )
}

// Retained for backward compatibility
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  GuerreirosTheme(content = content)
}
