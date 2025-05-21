package com.example.mtoproducto.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mtoproducto.DBHelper
import com.example.mtoproducto.Producto
import com.example.mtoproducto.ui.theme.MtoProductoTheme

@Composable
fun UIPrincipal(navControlador: NavController){
    val context = LocalContext.current
    val auxSQLite = DBHelper(context)
    val productList = remember { mutableStateListOf<Producto>() }

    LaunchedEffect(Unit) {
        auxSQLite.readableDatabase.use { db ->
            db.rawQuery("SELECT * FROM producto;", null).use { cursor ->
                val tempList = mutableListOf<Producto>()
                while (cursor.moveToNext()) {
                    tempList.add(
                        Producto(
                            id = cursor.getString(0),
                            name = cursor.getString(1),
                            price = cursor.getString(2),
                            description = cursor.getString(3) ?: "Sin descripción",
                            imagen = cursor.getString(4) ?: ""
                        )
                    )
                }
                productList.clear()
                productList.addAll(tempList)
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(5.dp)
    ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(text="Productos Disponibles", fontSize=24.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { navControlador.navigate("ProductoMto") }) {
                    Icon(Icons.Filled.Add, contentDescription="Añadir")
                }

                IconButton(onClick = { navControlador.navigate("PantallaAyuda") }) {
                    Icon(Icons.Filled.Info, contentDescription="ayuda!")
                }

                IconButton(onClick = { navControlador.navigate("ColorPicker") }) {
                    Icon(Icons.Filled.Settings, contentDescription="Configuración")
                }
            }

        LazyColumn {
            items(productList) { product ->
                ProductCard(product, navControlador) { id ->
                    if (auxSQLite.deleteProduct(id)) productList.removeAll { it.id == id }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Producto, navController: NavController, onDelete: (String) -> Unit) {
    var show by rememberSaveable { mutableStateOf(false) }
    val imageBitmap = remember(product.imagen) {
        try {
            val bytes = Base64.decode(product.imagen, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) { null }
    }?.asImageBitmap()

    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha=0.2f)),
                contentAlignment = Alignment.Center
            ) {
                imageBitmap?.let {
                    Image(it, contentDescription=product.name, Modifier.fillMaxSize(), contentScale=ContentScale.Crop)
                } ?: Icon(Icons.Filled.Image, contentDescription=null, Modifier.size(48.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, maxLines=2, overflow=TextOverflow.Ellipsis)
                Text(
                    text = if (product.description.isEmpty()) "Sin descripción" else product.description,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("$${product.price}")
            }
            IconButton(onClick={ navController.navigate("ProductoMto?id=${product.id}") }) { Icon(Icons.Filled.Edit, contentDescription="Editar") }
            IconButton(onClick={ show=true }) { Icon(Icons.Filled.Delete, contentDescription="Eliminar") }
        }
        ButtonDelete(show, onDismiss={show=false}, onConfirm={ onDelete(product.id); show=false })
    }
}

@Composable
fun ButtonDelete(show:Boolean, onDismiss:() -> Unit, onConfirm:()-> Unit){
    if (show)
        AlertDialog(
            onDismissRequest = {onDismiss()},
            confirmButton = { TextButton(onClick = {onConfirm()}){Text("Continuar")} },
            dismissButton = { TextButton(onClick = {onDismiss()}){Text("Descartar")} },
            title = { Text("Eliminar producto?") },
            text = { Text("Esta acción no se puede deshacer") }
        )
}

@Preview(showBackground = true)
@Composable
fun PreviewProductCard() {
    MtoProductoTheme {
        ProductCard(Producto("","Ejemplo","0.00","Desc",""), rememberNavController()) {}
    }
}