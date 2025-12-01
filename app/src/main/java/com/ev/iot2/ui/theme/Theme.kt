package com.ev.iot2.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val IoTempColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryRedLight,
    onPrimaryContainer = OnBackgroundBlack,
    secondary = PrimaryRedDark,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = PrimaryRedLight,
    onSecondaryContainer = OnBackgroundBlack,
    tertiary = PrimaryRed,
    onTertiary = OnPrimaryWhite,
    background = BackgroundWhite,
    onBackground = OnBackgroundBlack,
    surface = SurfaceWhite,
    onSurface = OnSurfaceBlack,
    surfaceVariant = GrayLight,
    onSurfaceVariant = GrayDark,
    error = ErrorRed,
    onError = OnPrimaryWhite
)

@Composable
fun IOT2Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = IoTempColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PrimaryRed.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}