package com.example.mtoproducto

import android.database.Cursor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val navControlador = rememberNavController()

    NavHost(navControlador, startDestination = "PantallaPrincipal"){
        composable("PantallaPrincipal"){
            PantallaPrincipal(navControlador)
        }
        composable("ProductoMto?nombre={nombre}?",
            arguments = listOf(navArgument("nombre") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ){
            backStackEntry ->
                ProductoMto(backStackEntry.arguments?.getString("nombre"))
        }
    }
}

fun buttonEdit(){
    //TODO
}

@Composable
fun ButtonDelete(show:Boolean, onDismiss:() -> Unit, onConfirm:()-> Unit){
    if (show)
        AlertDialog(onDismissRequest = {onDismiss()}, confirmButton = { TextButton(onClick = {onConfirm()}){Text("Continuar")} }, dismissButton = {TextButton(onClick = {onDismiss()}){Text("Descartar")}}, title = {Text("Eliminar producto?")}, text = {Text("Esta accion no se puede deshacer")})

}

@Composable
fun ProductoMto(nombre:String?){
    val context = LocalContext.current
    val auxSQLite = DBHelper(context)
    val base = auxSQLite.writableDatabase

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagen by remember { mutableStateOf("") }

    Column (Modifier.fillMaxSize().padding(16.dp)){
        Text("Aniadir producto", fontSize = 24.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = nombre, onValueChange = {nombre = it}, label = {Text("Nombre*")}, modifier =  Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = precio, onValueChange = {precio = it}, label = {Text("Precio*")}, modifier =  Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = {descripcion = it}, label = {Text("Descripcion")}, modifier =  Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
            Button(onClick = {/*TODO: Elaborar logica para tomar foto*/}) { Text("Tomar foto") }
            Button(onClick = {/*TODO: Elaborar logica para tomar foto*/}) { Text("Subir desde la galeria") }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {/* TODO: Complementar logica para aniadir el producto */}, modifier = Modifier.fillMaxWidth()) { Text("Aniadir Producto") }
    }

}

@Composable
fun PantallaPrincipal(navControlador: NavController){
    val context = LocalContext.current
    val auxSQLite = DBHelper(context)
    val base = auxSQLite.writableDatabase

    //La lista de Productos en mutableListOf para que sea observable
    val productList = remember { mutableStateListOf<Producto>() }

    LaunchedEffect(Unit) {
        val db = auxSQLite.readableDatabase
        val cursor: Cursor = base.rawQuery("SELECT * FROM producto;", null)

        val tempList = mutableListOf<Producto>()
        while(cursor.moveToNext()){
            tempList.add(
                Producto(
                    name = cursor.getString(1),
                    price = cursor.getString(2),
                    description = if (cursor.isNull(3)) "Sin descripción" else cursor.getString(3),
                    imagen = if (cursor.isNull(4)) "" else cursor.getString(4)
                )
            )
        }
        cursor.close()
        db.close()

        productList.clear()
        productList.addAll(tempList)
    }

    Column(Modifier
        .fillMaxSize()
        .systemBarsPadding()
        .padding(5.dp)) {

        Row (Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
            Text(text="Productos Disponibles", fontSize = 24.sp, textAlign =  TextAlign.Center)
            IconButton(onClick = { navControlador.navigate("ProductoMto") }) {
                Icon(Icons.Default.Add, contentDescription = "Aniadir")
            }
        }

        LazyColumn {
            items(productList) { product ->
                ProductCard(
                    product = product,
                    onDelete = {
                        val eliminado = auxSQLite.deleteProduct(product.name)

                        if(eliminado){
                            productList.remove(product)
                            Toast.makeText(context,"Producto eliminado exitosamente",Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(context,"Fallo al eliminar el producto",Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }
}

data class Producto(
    val name: String,
    val price: String,
    val description: String,
    val imagen: String
)

@Composable
fun ProductCard(product: Producto,  onDelete: ()->Unit) {

    var show by rememberSaveable { mutableStateOf(false) }


    // Convertir Base64 a ImageBitmap
    val imageBitmap = remember(product.imagen) {
        if (product.imagen.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(product.imagen, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen (100x100px)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Placeholder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Columna para texto y precio
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, // Limita a 2 líneas
                    overflow = TextOverflow.Ellipsis // Puntos suspensivos si es muy largo
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // Botones de acción
            Column {
                IconButton(
                    onClick = { /* Editar */ },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = { show = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }

    ButtonDelete(show, onDismiss = {show = false}, onConfirm = { onDelete()
        show = false})
}

@Preview(showBackground = true)
@Composable
fun PreviewUIPrincipal() {
    MtoProductoTheme {
        val sampleProducts = listOf(
            Producto("Producto 1", "49.00", "Este producto está chido",""),
            Producto("Otro producto", "9999.00", "Este también está chido","")
        )
        Column(Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(5.dp)) {

            Row (Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically){
                Text(text="Productos Disponibles", fontSize = 24.sp, textAlign =  TextAlign.Center)
                Button(onClick = {}) {Text("Add New Item") }
            }

            LazyColumn {
                items(sampleProducts) { product ->
                    ProductCard(
                        product = Producto(
                            "Producto de Ejemplo",
                            "123.45",
                            "Descripción",
                            "" // Cadena Base64 vacía para la preview
                        ),
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductCard() {
    MtoProductoTheme {
        ProductCard(
            product = Producto(
                "Producto de Ejemplo",
                "123.45",
                "Descripción",
                "" // Cadena Base64 vacía para la preview
            ),
            onDelete = {}
        )
    }
}