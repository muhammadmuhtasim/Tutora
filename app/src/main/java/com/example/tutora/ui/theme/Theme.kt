package com.example.tutora.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = VibrantBlue,
    onPrimary = PrimaryWhite,
    primaryContainer = SoftBlueContainer,
    onPrimaryContainer = OnSoftBlueContainer,
    secondary = VibrantTeal,
    onSecondary = PrimaryWhite,
    secondaryContainer = SoftTealContainer,
    onSecondaryContainer = OnSoftTealContainer,
    tertiary = VibrantAmber,
    onTertiary = PrimaryWhite,
    tertiaryContainer = SoftAmberContainer,
    onTertiaryContainer = OnSoftAmberContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Neutral900,
    onSurface = Neutral900,
    onSurfaceVariant = Neutral700,
    error = ErrorRed,
    outline = Neutral300,
    outlineVariant = Neutral100,
    surfaceTint = VibrantBlue
)

private val DarkColorScheme = darkColorScheme(
    primary = VibrantBlueLight,
    onPrimary = PrimaryWhite,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = SoftBlueContainer,
    secondary = VibrantTealLight,
    onSecondary = PrimaryWhite,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = SoftTealContainer,
    tertiary = VibrantViolet,
    onTertiary = PrimaryWhite,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = VibrantViolet,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = PrimaryWhite,
    onSurface = PrimaryWhite,
    onSurfaceVariant = Neutral300,
    error = ErrorRed,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    surfaceTint = VibrantBlueLight
)

val PreciseShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun TutoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PreciseShapes,
        content = content
    )
}
