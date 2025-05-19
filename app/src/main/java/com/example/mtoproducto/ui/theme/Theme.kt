package com.example.mtoproducto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.mtoproducto.PreferenceHelper
import com.example.sharedpreferences.ui.theme.Typography
import android.graphics.Color.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue

@Composable
fun MtoProductoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferenceHelper = remember { PreferenceHelper(context) }
    val hexColor by remember { mutableStateOf(preferenceHelper.leerColorUsuario()) }

    val colorUsuario = hexColor?.let {
        try { Color(parseColor(it)) }
        catch (e: Exception) { Purple40 }
    } ?: Purple40

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colorUsuario,
            background = colorUsuario.copy(alpha = 0.2f),
            surface = colorUsuario.copy(alpha = 0.1f)
        )
    } else {
        lightColorScheme(
            primary = colorUsuario,
            background = colorUsuario.copy(alpha = 0.1f),
            surface = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}