package com.example.mtoproducto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mtoproducto.ui.theme.MtoProductoTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

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
        composable("ProductoMto?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ){
            backStackEntry ->
                ProductoMto(backStackEntry.arguments?.getString("id"))
        }
    }
}

@Composable
fun ButtonDelete(show:Boolean, onDismiss:() -> Unit, onConfirm:()-> Unit){
    if (show)
        AlertDialog(onDismissRequest = {onDismiss()}, confirmButton = { TextButton(onClick = {onConfirm()}){Text("Continuar")} }, dismissButton = {TextButton(onClick = {onDismiss()}){Text("Descartar")}}, title = {Text("Eliminar producto?")}, text = {Text("Esta accion no se puede deshacer")})

}

@Composable
fun ProductoMto(id:String?){
    val context = LocalContext.current
    val auxSQLite = DBHelper(context)
    val navController = rememberNavController()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagen by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var capturedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var savedImageFile by remember {mutableStateOf<File?>(null)}

    // Función para procesar la imagen seleccionada/capturada
    fun processImage(uri: Uri?) {
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                // Redimensionar a 100x100
                val thumbnailBitmap = originalBitmap?.resizeToThumbnail()

                // Guardar en almacenamiento interno
                val fileName = generateImageFileName()
                val savedFile = thumbnailBitmap?.let { bitmap ->
                    context.saveImageToPublicFolder(bitmap, fileName)
                }

                // Convertir a Base64 para la base de datos
                val byteArrayOutputStream = ByteArrayOutputStream()
                thumbnailBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

                // Actualizar estados
                imagen = base64Image
                savedImageFile = savedFile
                capturedImageBitmap = thumbnailBitmap?.asImageBitmap()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        processImage(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile!!)
            processImage(imageUri)
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if(granted){
            Toast.makeText(context, "Permiso de camara otorgado", Toast.LENGTH_SHORT).show()
            imageFile = context.createImageFile()
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider",imageFile!!)
            cameraLauncher.launch(uri)
        } else{
            Toast.makeText(context, "Esta funcion necesita permisos de camara", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(id) {
        if (!id.isNullOrEmpty()) {
            val producto = auxSQLite.getProductById(id)
            producto?.let {
                nombre = it.name
                precio = it.price
                descripcion = it.description
                imagen = it.imagen

                if (it.imagen.isNotEmpty()) {
                    try {
                        val imageBytes = Base64.decode(it.imagen, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        capturedImageBitmap = bitmap?.asImageBitmap()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column (Modifier.fillMaxSize().padding(16.dp)){
        Text(text=if (id != null) "Editar Producto" else "Añadir Producto", fontSize = 24.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        if(!id.isNullOrEmpty()){
            Text("ID: $id", textAlign = TextAlign.Center, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(value = nombre, onValueChange = {nombre = it}, label = {Text("Nombre*")}, modifier =  Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = precio, onValueChange = {precio = it}, label = {Text("Precio*")}, modifier =  Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = {descripcion = it}, label = {Text("Descripcion")}, modifier =  Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    val hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

                    if (hasCameraPermission) {
                        imageFile = context.createImageFile()
                        imageFile?.let { file ->
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            cameraLauncher.launch(uri)
                        }
                    } else {
                        cameraPermission.launch(Manifest.permission.CAMERA)
                    }
                }
            ) { Text("Tomar foto") }
            Button(onClick = {galleryLauncher.launch("image/*")}) { Text("Subir desde la galeria") }
        }
        Spacer(Modifier.height(8.dp))

        // Visualización de imagen
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    capturedImageBitmap?.let { imageBitmap ->
                        try {
                            // Crea una copia nueva del bitmap
                            val bitmap = imageBitmap.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)

                            if (bitmap != null && !bitmap.isRecycled) {
                                val file = context.saveImageToPublicFolder(bitmap, generateImageFileName())
                                file?.let {
                                    Toast.makeText(
                                        context,
                                        "Imagen guardada en: ${it.absolutePath}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } ?: run {
                                    Toast.makeText(
                                        context,
                                        "Error: Archivo no creado",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error: Bitmap no válido",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error crítico: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        ) {
            if (capturedImageBitmap != null) {
                Image(
                    bitmap = capturedImageBitmap!!,
                    contentDescription = "Imagen del producto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Seleccionar imagen",
                    modifier = Modifier.size(48.dp).align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (nombre.isBlank() || precio.isBlank()) {
                    Toast.makeText(context, "Nombre y precio son obligatorios", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val db = auxSQLite.writableDatabase
                val success = if (id != null) {
                    // Actualizar producto existente
                    val rows = auxSQLite.updateProduct(db, id, nombre, precio, descripcion, imagen)
                    rows > 0
                } else {
                    // Crear nuevo producto
                    val newId = auxSQLite.addProduct(nombre, precio, descripcion, imagen)
                    newId != null && newId != -1L
                }
                db.close()

                if (success) {
                    Toast.makeText(
                        context,
                        if (id != null) "Producto actualizado" else "Producto añadido",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(
                        context,
                        "Error al guardar el producto",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (id != null) "Actualizar Producto" else "Añadir Producto")
        }
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
                    id = cursor.getString(0),
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
                    navController = navControlador,
                    onDelete = {
                        val eliminado = auxSQLite.deleteProduct(product.id)

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
    val id: String,
    val name: String,
    val price: String,
    val description: String,
    val imagen: String
)

@Composable
fun ProductCard(product: Producto, navController: NavController, onDelete: ()->Unit) {

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
                    onClick = { navController.navigate("ProductoMto?id=${product.id}") },
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
            Producto("","Producto 1", "49.00", "Este producto está chido",""),
            Producto("","Otro producto", "9999.00", "Este también está chido","")
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
                            "", // Cadena vacia de ID
                            "Producto de Ejemplo",
                            "123.45",
                            "Descripción",
                            "" // Cadena Base64 vacía para la preview
                        ),
                        navController = rememberNavController(),
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
                "",
                "Producto de Ejemplo",
                "123.45",
                "Descripción",
                "" // Cadena Base64 vacía para la preview
            ),
            navController = rememberNavController(),
            onDelete = {}
        )
    }
}

fun Bitmap.resizeToThumbnail(): Bitmap {
    return this.scale(100, 100)
}

// Función para guardar imagen en la carpeta de la aplicación
fun Context.saveImageToPublicFolder(bitmap: Bitmap, fileName: String): File? {
    return try {
        // Verifica si el bitmap es válido
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
            Toast.makeText(this, "Bitmap inválido", Toast.LENGTH_SHORT).show()
            return null
        }

        val appName = getString(R.string.app_name)
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            appName
        ).apply {
            if (!exists()) mkdirs()
        }

        val file = File(directory, "$fileName.jpg")
        FileOutputStream(file).use { fos ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)) {
                Toast.makeText(this, "Error al comprimir", Toast.LENGTH_SHORT).show()
                return null
            }
            fos.flush()
        }

        // Notifica al sistema sobre el nuevo archivo
        MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null, null)

        file
    } catch (e: Exception) {
        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}

// Función para crear nombre de archivo único
fun generateImageFileName(): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "PRODUCT_IMG_$timeStamp"
}

fun Context.createImageFile(fileName: String = generateImageFileName()): File {
    val storageDir = File(filesDir, "product_images").apply {
        if (!exists()) mkdirs()
    }
    return File(storageDir, "$fileName.jpg").also { file ->
        file.createNewFile() // Crea el archivo explícitamente
    }
}

fun ImageBitmap.asAndroidBitmap(): Bitmap {
    val config = if (this.config == ImageBitmapConfig.Argb8888) {
        Bitmap.Config.ARGB_8888
    } else {
        Bitmap.Config.RGB_565
    }

    val bitmap = Bitmap.createBitmap(this.width, this.height, config)
    val buffer = IntArray(this.width * this.height)

    this.readPixels(
        buffer,
        0,
        this.width,
        0,
        0,
        this.width,
        this.height
    )
    bitmap.setPixels(buffer, 0, this.width, 0, 0, this.width, this.height)

    return bitmap
}