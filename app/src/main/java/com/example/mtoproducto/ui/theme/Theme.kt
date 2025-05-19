package com.example.sharedpreferences.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.sharedpreferences.com.example.mtoproducto.PreferenceHelper

@Composable
fun SharedPreferencesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferenceHelper = PreferenceHelper(context)
    val hexColor = preferenceHelper.obtenerColorUsuario()
    val userColor = hexColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Purple40

    val colorScheme = if (darkTheme) {
        darkColorScheme(primary = userColor, secondary = PurpleGrey80, tertiary = Pink80)
    } else {
        lightColorScheme(primary = userColor, secondary = PurpleGrey40, tertiary = Pink40)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}