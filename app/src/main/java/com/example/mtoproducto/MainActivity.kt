package com.example.mtoproducto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mtoproducto.screens.PantallaAyuda
import com.example.mtoproducto.screens.ProductoMto
import com.example.mtoproducto.screens.RGBColorPicker
import com.example.mtoproducto.screens.UIPrincipal
import com.example.mtoproducto.ui.theme.MtoProductoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MtoProductoTheme {
                Navegacion()
            }
        }
    }
}

@Composable
fun Navegacion(){
    val context = LocalContext.current
    val navController = rememberNavController()
    NavHost(navController, startDestination = "PantallaPrincipal"){
        composable("PantallaPrincipal"){
            UIPrincipal(navController)
        }
        composable("ColorPicker") {
            RGBColorPicker(context,navController)
        }
        composable("PantallaAyuda"){
            PantallaAyuda(navController)
        }
        composable(
            "ProductoMto?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })

        ){ backStackEntry ->
            ProductoMto(
                id = backStackEntry.arguments?.getString("id"),
                navController = navController
            )
        }
    }
}

data class Producto(
    val id: String,
    val name: String,
    val price: String,
    val description: String,
    val imagen: String
)

@Preview(showBackground = true)
@Composable
fun PreviewUIPrincipal() {
    MtoProductoTheme { UIPrincipal(rememberNavController()) }
}