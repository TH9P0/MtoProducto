package com.example.mtoproducto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val blueColorScheme = lightColorScheme(
    primary = Color(0xFF134074),       // Azul oscuro principal
    onPrimary = Color(0xFFEEF4ED),     // Blanco/verde claro para texto sobre primary
    primaryContainer = Color(0xFFD4E3F5), // Versión más clara de primary
    onPrimaryContainer = Color(0xFF0B2545), // Azul muy oscuro para texto sobre primaryContainer
    secondary = Color(0xFF0B2545),     // Azul muy oscuro como secundario
    onSecondary = Color(0xFFEEF4ED),   // Blanco/verde claro para texto sobre secondary
    secondaryContainer = Color(0xFFC3D0E0), // Versión más clara de secondary
    onSecondaryContainer = Color(0xFF0B2545), // Azul muy oscuro para texto sobre secondaryContainer
    background = Color(0xFFEEF4ED),    // Fondo claro
    onBackground = Color(0xFF0B2545),  // Azul muy oscuro para texto sobre fondo
    surface = Color(0xFFFFFFFF),       // Superficie blanca
    onSurface = Color(0xFF0B2545),     // Azul muy oscuro para texto sobre superficie
    error = Color(0xFFD32F2F),        // Rojo de error
    onError = Color(0xFFFFFFFF)       // Blanco para texto sobre error
)

// Opcional: Versión oscura del esquema de colores
val blueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB6F9),
    onPrimary = Color(0xFF0B2545),
    primaryContainer = Color(0xFF1E4B8B),
    onPrimaryContainer = Color(0xFFD4E3F5),
    secondary = Color(0xFFB6CCE8),
    onSecondary = Color(0xFF0B2545),
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFC3D0E0),
    background = Color(0xFF0B2545),
    onBackground = Color(0xFFEEF4ED),
    surface = Color(0xFF1E3A5F),
    onSurface = Color(0xFFEEF4ED),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MtoProductoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if(darkTheme) blueDarkColorScheme else blueColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}