package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = ThemeOnSurface,
    surface = ThemeOnSurface,
    onBackground = ThemeBackground,
    onSurface = ThemeBackground
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,
    secondary = PurplePrimary,
    secondaryContainer = ThemeSurfaceVariant,
    onSecondaryContainer = ThemeOnSurface,
    tertiary = RoseTertiary,
    onTertiary = OnRoseTertiary,
    background = ThemeBackground,
    surface = ThemeSurface,
    surfaceVariant = ThemeSurfaceVariant,
    outline = ThemeOutline,
    outlineVariant = ThemeOutline,
    onBackground = ThemeOnSurface,
    onSurface = ThemeOnSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor = false so our curated expressively styled theme shows on all devices
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
