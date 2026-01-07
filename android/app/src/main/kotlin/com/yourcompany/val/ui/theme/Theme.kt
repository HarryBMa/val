package com.yourcompany.val.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Calming Medical Assistant Theme - Matching Figma Design
private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7c9cb5),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF5d7a8f),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFFFFFFF),

    secondary = androidx.compose.ui.graphics.Color(0xFFa8c5d9),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF4a5568),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF8b98a5),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFFFFFFF),

    tertiary = androidx.compose.ui.graphics.Color(0xFFb8d8d8),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF4a5568),

    error = androidx.compose.ui.graphics.Color(0xFFef4444),
    onError = androidx.compose.ui.graphics.Color(0xFFFFFFFF),

    background = androidx.compose.ui.graphics.Color(0xFF1a1f2e),
    onBackground = androidx.compose.ui.graphics.Color(0xFFe5e7eb),

    surface = androidx.compose.ui.graphics.Color(0xFF242936),
    onSurface = androidx.compose.ui.graphics.Color(0xFFe5e7eb),
    surfaceVariant = androidx.compose.ui.graphics.Color(0x99ffffff),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF8b98a5),

    outline = androidx.compose.ui.graphics.Color(0x26ffffff),
    outlineVariant = androidx.compose.ui.graphics.Color(0x66ffffff)
)

private val LightColorScheme = lightColorScheme(
    // Primary colors - Ocean blue theme
    primary = androidx.compose.ui.graphics.Color(0xFF7c9cb5),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFd4e8ed),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF4a5568),

    // Secondary colors
    secondary = androidx.compose.ui.graphics.Color(0xFFa8c5d9),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF4a5568),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFe6f2f5),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF4a5568),

    // Tertiary colors - Seafoam accents
    tertiary = androidx.compose.ui.graphics.Color(0xFFb8d8d8),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF4a5568),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFf0f4f7),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF4a5568),

    // Error colors
    error = androidx.compose.ui.graphics.Color(0xFFd4183d),
    onError = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    errorContainer = androidx.compose.ui.graphics.Color(0xFFfecdd3),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFd4183d),

    // Background colors - Soft mist
    background = androidx.compose.ui.graphics.Color(0xFFf8fafb),
    onBackground = androidx.compose.ui.graphics.Color(0xFF4a5568),

    // Surface colors - Glass morphism support
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF4a5568),
    surfaceVariant = androidx.compose.ui.graphics.Color(0x99ffffff), // White 60%
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF8b98a5),
    surfaceTint = androidx.compose.ui.graphics.Color(0xFF7c9cb5),

    // Outline colors
    outline = androidx.compose.ui.graphics.Color(0x26ffffff), // White 15%
    outlineVariant = androidx.compose.ui.graphics.Color(0x66ffffff), // White 40%

    // Inverse colors
    inverseSurface = androidx.compose.ui.graphics.Color(0xFF4a5568),
    inverseOnSurface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    inversePrimary = androidx.compose.ui.graphics.Color(0xFFa8c5d9)
)

@Composable
fun ValTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to maintain calming medical theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use background color for status bar to match calming design
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
