package com.example.mtoproducto

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.sharedpreferences.com.example.mtoproducto.PreferenceHelper

@Composable
fun RGBColorPicker(context: Context) {
    var red by remember { mutableStateOf(255f) }
    var green by remember { mutableStateOf(0f) }
    var blue by remember { mutableStateOf(0f) }

    val color = Color(red / 255f, green / 255f, blue / 255f)

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {

        Text("Selecciona un color:", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        ColorSlider("Rojo", red) { red = it }
        ColorSlider("Verde", green) { green = it }
        ColorSlider("Azul", blue) { blue = it }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(color)
                .border(2.dp, Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "HEX: ${color.toHex()}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para guardar el color
        androidx.compose.material3.Button(onClick = {
            SaveColorPreference(color, context)
        }) {
            Text("Guardar Color")
        }
    }
}


@Composable
fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text("$label: ${value.toInt()}")
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = when (label) {
                    "Rojo" -> Color.Red
                    "Verde" -> Color.Green
                    "Azul" -> Color.Blue
                    else -> Color.Gray
                }
            )
        )
    }
}

// Convertir Color a HEX
fun Color.toHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02X%02X%02X", r, g, b)
}

fun SaveColorPreference(color: Color, context: Context) {
    val hexColor = color.toHex()
    val preferenceHelper = PreferenceHelper(context)
    preferenceHelper.guardarColorUsuario(hexColor)
}