package com.example.mtoproducto

import android.database.Cursor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

private const val TAG = "ProductosApp"

/**
 * Actividad principal de la aplicación que muestra la lista de productos.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar edge-to-edge para una mejor experiencia visual
        enableEdgeToEdge()
        setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme(colorScheme = AppTheme.blueColorScheme) {
                ProductosScreen()
            }
        }
    }
}

/**
 * Modelo de datos para representar un producto.
 *
 * @property id Identificador único del producto
 * @property name Nombre del producto
 * @property price Precio del producto (como String para facilitar la presentación)
 * @property description Descripción del producto
 * @property imagen Imagen del producto codificada en Base64
 */
data class Producto(
    val id: Long = 0,
    val name: String,
    val price: String,
    val description: String,
    val imagen: String
)

/**
 * Pantalla principal que muestra la lista de productos disponibles.
 */
@Composable
fun ProductosScreen() {
    // Contexto necesario para acceder a la base de datos
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }

    // Lista mutable para almacenar y observar cambios en los productos
    val productList = remember { mutableStateListOf<Producto>() }

    // Cargar productos desde la base de datos al iniciar
    LaunchedEffect(Unit) {
        try {
            dbHelper.obtenerTodosLosProductos().use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NOMBRE))
                    val price = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PRECIO)).toString()

                    // Manejo de columnas que podrían ser NULL
                    val descriptionIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPCION)
                    val description = if (cursor.isNull(descriptionIndex))
                        "Sin descripción"
                    else
                        cursor.getString(descriptionIndex)

                    val imagenIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IMAGEN)
                    val imagen = if (cursor.isNull(imagenIndex))
                        ""
                    else
                        cursor.getString(imagenIndex)

                    productList.add(
                        Producto(
                            id = id,
                            name = name,
                            price = price,
                            description = description,
                            imagen = imagen
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar productos", e)
        }
    }

    // UI principal con título y lista de productos
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(5.dp)
    ) {
        // Cabecera con título y botón de añadir
        Header(
            onAddClick = {
                // TODO: Implementar funcionalidad para añadir nuevo producto
                Log.d(TAG, "Añadir producto seleccionado")
            }
        )

        // Lista de productos
        LazyColumn {
            items(productList) { product ->
                ProductCard(
                    product = product,
                    onEdit = {
                        // TODO: Implementar funcionalidad para editar producto
                        Log.d(TAG, "Editar producto: ${product.name}")
                    },
                    onDelete = {
                        try {
                            // Eliminar producto de la base de datos
                            dbHelper.eliminarProductoPorNombre(product.name)

                            // Eliminar de la lista UI
                            productList.remove(product)

                            Log.d(TAG, "Producto eliminado: ${product.name}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al eliminar producto: ${product.name}", e)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Componente de cabecera con título y botón de añadir.
 *
 * @param onAddClick Acción a ejecutar cuando se pulsa el botón de añadir
 */
@Composable
fun Header(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Productos Disponibles",
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir Producto"
            )
        }
    }
}

/**
 * Componente de tarjeta para mostrar un producto individual.
 *
 * @param product Datos del producto a mostrar
 * @param onEdit Acción a ejecutar cuando se pulsa el botón de editar
 * @param onDelete Acción a ejecutar cuando se confirma eliminar
 */
@Composable
fun ProductCard(
    product: Producto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Estado para controlar el diálogo de confirmación de eliminación
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Convertir la imagen Base64 a ImageBitmap para mostrarla
    val imageBitmap = remember(product.imagen) {
        if (product.imagen.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(product.imagen, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                Log.e(TAG, "Error decodificando imagen: ${product.name}", e)
                null
            }
        } else {
            null
        }
    }

    // Tarjeta del producto
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
            // Contenedor de la imagen
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    // Mostrar la imagen del producto
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Imagen de ${product.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Mostrar un ícono de placeholder si no hay imagen
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Sin imagen",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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

            // Botones de acción (editar y eliminar)
            Column {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar ${product.name}"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar ${product.name}"
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar producto
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
}

/**
 * Diálogo de confirmación para eliminar un producto.
 *
 * @param onDismiss Acción a ejecutar cuando se cancela
 * @param onConfirm Acción a ejecutar cuando se confirma
 */
@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar producto?") },
        text = { Text("Esta acción no se puede deshacer") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Objeto que contiene definiciones relacionadas con el tema de la aplicación.
 */
object AppTheme {
    // Esquema de colores azul para la aplicación
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
}

/**
 * Vista previa de la pantalla principal.
 */
@Preview(showBackground = true)
@Composable
fun PreviewProductosScreen() {
    MaterialTheme(colorScheme = AppTheme.blueColorScheme) {
        val sampleProducts = listOf(
            Producto(1, "Cheetos", "17.50", "Botana de queso crujiente", ""),
            Producto(2, "Coca-Cola 600ml", "22.00", "Refresco de cola", "")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(5.dp)
        ) {
            Header(onAddClick = {})

            LazyColumn {
                items(sampleProducts) { product ->
                    ProductCard(
                        product = product,
                        onEdit = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}

/**
 * Vista previa de una tarjeta de producto individual.
 */
@Preview(showBackground = true)
@Composable
fun PreviewProductCard() {
    MaterialTheme(colorScheme = AppTheme.blueColorScheme) {
        ProductCard(
            product = Producto(
                id = 1,
                name = "Producto de Ejemplo",
                price = "123.45",
                description = "Descripción del producto de ejemplo",
                imagen = ""
            ),
            onEdit = {},
            onDelete = {}
        )
    }
}