package com.example.mtoproducto.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.mtoproducto.PreferenceHelper

@Composable
fun RGBColorPicker(context: Context, navController: NavController) {
    var red by remember { mutableFloatStateOf(255f) }
    var green by remember { mutableFloatStateOf(0f) }
    var blue by remember { mutableFloatStateOf(0f) }
    var pulsado by remember { mutableStateOf(false) }

    val color = Color(red / 255f, green / 255f, blue / 255f)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp))
    {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Botón de regreso (alineado a la izquierda)
            IconButton(
                onClick = {
                    if(!pulsado){
                        navController.popBackStack()
                        pulsado = true
                    }
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Regresar",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Título perfectamente centrado
            Text(
                text = "Preferencias",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )
        }

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
        Button(onClick = {
            saveColorPreference(color, context)
            (context as Activity).recreate()  // Reinicia la actividad
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

fun saveColorPreference(color: Color, context: Context) {
    val hexColor = color.toHex()
    val preferenceHelper = PreferenceHelper(context)
    preferenceHelper.guardarColorUsuario(hexColor)
    Toast.makeText(context, "Color guardado: $hexColor", Toast.LENGTH_SHORT).show()
}