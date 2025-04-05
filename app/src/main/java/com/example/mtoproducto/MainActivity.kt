package com.example.mtoproducto

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import androidx.compose.material3.lightColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setDecorFitsSystemWindows(window, false)  // Permite control manual
        setContent {
            MaterialTheme(colorScheme = blueColorScheme) {
                UIPrincipal()
            }
        }
    }
}

fun buttonEdit(){
    //TODO
}

@Composable
fun ButtonDelete(show:Boolean, onDismiss:() -> Unit, onConfirm:()-> Unit){
    //TODO: Show message for double check on deleting element
    if (show)
        AlertDialog(onDismissRequest = {onDismiss()}, confirmButton = { TextButton(onClick = {onConfirm()}){Text("Continuar")} }, dismissButton = {TextButton(onClick = {onDismiss()}){Text("Descartar")}}, title = {Text("Seguro que deseas eliminar este articulo?")}, text = {Text("Si aceptas eliminar este articulo ya no aparecera ni podras rehacer esta operacion, deseas continuar?")})

}

@Composable
fun UIPrincipal(){
    val auxSQLite = DBHelper(LocalContext.current)
    val base = auxSQLite.writableDatabase
    val cursor: Cursor = base.rawQuery("SELECT * FROM producto;", null)
    val productList = mutableListOf<Producto>()

    while(cursor.moveToNext()){
        val name = cursor.getString(1)
        val price = cursor.getString(2)
        val description = if (cursor.isNull(3)) "Sin descripción" else cursor.getString(3)

        productList.add(Producto(name, price, description))
    }
    cursor.close()
    base.close()

    Column(Modifier.fillMaxSize().systemBarsPadding().padding(5.dp)) {

        Row (Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
            Text(text="Productos Disponibles", fontSize = 24.sp, textAlign =  TextAlign.Center)
            Button(onClick = {}) {Text("Add New Item") }
        }

        LazyColumn {
            items(productList) { product ->
                ProductCard(
                    productName = product.name,
                    productPrice = product.price,
                    productDescription = product.description
                )
            }
        }
    }
}

data class Producto(
    val name: String,
    val price: String,
    val description: String
)

@Composable
fun ProductCard(productName:String,productPrice: String,productDescription:String) {
    var show by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Placeholder",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Textos
            Spacer(modifier = Modifier.height(12.dp))
            Text(productName, style = MaterialTheme.typography.titleMedium)
            Text(productDescription, style = MaterialTheme.typography.bodySmall)

            // Precio + Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(productPrice, style = MaterialTheme.typography.titleLarge)
                Row {
                    IconButton(onClick = { /* Editar */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { show = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }

    ButtonDelete(show, {show = false},{ Log.i("aris","click")})
}

@Preview(showBackground = true)
@Composable
fun PreviewUIPrincipal() {
    MaterialTheme(colorScheme = blueColorScheme) {
        val sampleProducts = listOf(
            Producto("Producto 1", "49.00", "Este producto está chido"),
            Producto("Otro producto", "9999.00", "Este también está chido")
        )
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(5.dp)) {

            Row (Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
                Text(text="Productos Disponibles", fontSize = 24.sp, textAlign =  TextAlign.Center)
                Button(onClick = {}) {Text("Add New Item") }
            }

            LazyColumn {
                items(sampleProducts) { product ->
                    ProductCard(
                        productName = product.name,
                        productPrice = product.price,
                        productDescription = product.description
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductCard() {
    MaterialTheme(colorScheme = blueColorScheme) {
        ProductCard(
            productName = "Producto de Ejemplo",
            productPrice = "123.45",
            productDescription = "Descripción del producto de ejemplo."
        )
    }
}

val blueColorScheme = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF1976D2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBDEFB),
    onSecondaryContainer = Color(0xFF1976D2),
    background = Color(0xFFE3F2FD),
    onBackground = Color(0xFF0D47A1),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0D47A1),
    error = Color(0xFFD32F2F),
    onError = Color.White
)