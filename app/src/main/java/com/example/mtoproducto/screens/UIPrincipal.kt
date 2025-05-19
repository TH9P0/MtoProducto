package com.example.mtoproducto.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mtoproducto.DBHelper
import com.example.mtoproducto.ProductCard
import com.example.mtoproducto.Producto

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
            .systemBarsPadding()
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(text="Productos Disponibles", fontSize=24.sp)
            IconButton(onClick = { navControlador.navigate("ProductoMto") }) {
                Icon(Icons.Filled.Add, contentDescription="Añadir")
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