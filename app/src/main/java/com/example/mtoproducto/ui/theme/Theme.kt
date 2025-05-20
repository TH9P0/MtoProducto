package com.example.mtoproducto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.mtoproducto.PreferenceHelper
import com.example.sharedpreferences.ui.theme.Typography
import androidx.core.graphics.toColorInt

@Composable
fun MtoProductoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferenceHelper = remember { PreferenceHelper(context) }
    val hexColor by remember { mutableStateOf(preferenceHelper.leerColorUsuario()) }

    val colorUsuario = hexColor?.let {
        try { Color(it.toColorInt()) }
        catch (e: Exception) { generateSeedColor() }
    } ?: generateSeedColor()

    // Creamos una versión clara del color (30% de opacidad)
    val lightBackgroundColor = colorUsuario.copy(alpha = 0.3f)

    val colorScheme = lightColorScheme(
        primary = colorUsuario,  // Color original para botones
        onPrimary = getContrastColor(colorUsuario),

        // Fondo con tono claro
        background = lightBackgroundColor,
        surface = Color.White.copy(alpha = 0.9f),  // Superficie semi-transparente

        // Resto de la configuración
        onBackground = Color.Black.copy(alpha = 0.87f),
        onSurface = Color.Black.copy(alpha = 0.87f),
        error = Color(0xFFBA1A1A),
        onError = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun generateSeedColor(): Color {
    // Color por defecto que cumple WCAG
    return Color(0xFF6750A4) // Morado Material Design que funciona bien en ambos modos
}

// Función para garantizar contraste WCAG
private fun getContrastColor(color: Color): Color {
    val luminance = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    return if (luminance > 0.4f) Color.Black else Color.White
}

// Ajusta el color base para crear colores secundarios/terciarios
private fun adjustColorForContrast(baseColor: Color, factor: Float): Color {
    return Color(
        red = (baseColor.red + factor).coerceIn(0f, 1f),
        green = (baseColor.green + factor).coerceIn(0f, 1f),
        blue = (baseColor.blue + factor).coerceIn(0f, 1f),
        alpha = baseColor.alpha
    )
}