package com.example.sharedpreferences

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                UIPrincipal()
            }
        }
    }
}

@Composable
fun UIPrincipal() {
    val context: Context = LocalContext.current
    val prefs = remember { PreferenceHelper(context) }
    var colorFavorito by remember { mutableStateOf(prefs.leerColorUsuario()) }
    var colorSeleccionado by remember { mutableStateOf("") }

    /*Region PreferenciasUsuarioNombre
    var nombreUsuario by remember { mutableStateOf(prefs.leerNombreUsuario()) }
    var nombre by remember { mutableStateOf("") }

    Column (Modifier.padding(16.dp)) {
        Text("Nombre de usuario: $nombreUsuario")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Actualiza Nombre")}
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            prefs.guardarNombreUsuario(nombre)
            nombreUsuario=prefs.leerNombreUsuario()}
        ) { Text("Actualizar") }
    }Fin de la regiom*/

    val backgroundColor = remember(colorFavorito) {
        if (colorFavorito.isNotEmpty()) {
            Color(android.graphics.Color.parseColor(colorFavorito))
        } else {
            Color.White
        }
    }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()).background(backgroundColor)) {
        Text("Tu color favorito actual es $colorFavorito")
        colorSeleccionado = RGBColorPicker()
        Button(onClick = {
            prefs.guardarColorUsuario(colorSeleccionado)
            colorFavorito = prefs.leerColorUsuario()
        }
        ) {
            Text("Guardar color $colorSeleccionado")
        }
    }
}

@Composable
fun RGBColorPicker(): String {
    var red by remember { mutableStateOf(255f) }
    var blue by remember { mutableStateOf(0f) }
    var green by remember { mutableStateOf(0f) }

    val color = Color(red / 255f, green / 255f, blue / 255f)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Selecciona un color:", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        ColorSlider("Verde", green) { green = it }
        ColorSlider("Azul", blue) { blue = it }
        ColorSlider("Rojo", red) { red = it }
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(color)
                .border(2.dp, Color.Black)
        )
        Spacer(Modifier.height(16.dp))
        Text(text = "HEX: ${color.toHex()}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
    return color.toHex()
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
                thumbColor = Color.White, activeTrackColor =
                    when (label) {
                        "Rojo" -> Color.Red
                        "Verde" -> Color.Green
                        "Azul" -> Color.Blue
                        else -> Color.Gray
                    }
            )
        )
    }
}

fun Color.toHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("#%02x%02x%02x", r, g, b)
}

@Preview(showBackground = true)
@Composable
fun Previsualizacion() {
    UIPrincipal()
}

//Criterios para la evaluacion de una app by claudia albornoz