package com.example.mtoproducto

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mtoproducto.ui.theme.MtoProductoTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val navController = rememberNavController()
    NavHost(navController, startDestination = "PantallaPrincipal"){
        composable("PantallaPrincipal"){
            PantallaPrincipal(navController)
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

@Composable
fun ProductoMto(id:String?, navController: NavController){
    val context = LocalContext.current
    val auxSQLite = DBHelper(context)

    var nombre by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var imagen by rememberSaveable { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var capturedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }


    // 2) Procesar imagen capturada o subida
    fun processImage(uri: Uri?) {
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val thumbnailBitmap = originalBitmap?.resizeToThumbnail()

                // Guardar en carpeta seleccionada
                thumbnailBitmap?.let { bmp ->
                    val fileName = generateImageFileName()
                    context.saveImageToSelectedFolder(bmp, fileName)
                }

                // Convertir a Base64
                val baos = ByteArrayOutputStream()
                thumbnailBitmap?.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                imagen = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                // Mostrar en UI
                capturedImageBitmap = thumbnailBitmap?.asImageBitmap()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al procesar imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        processImage(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile!!
            )
            processImage(imageUri)
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            imageFile = context.createImageFile()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile!!
            )
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara requerido", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(id) {
        if (!id.isNullOrEmpty()) {
            auxSQLite.getProductById(id)?.let {
                nombre = it.name
                precio = it.price
                descripcion = it.description
                imagen = it.imagen
                if (it.imagen.isNotEmpty()) {
                    val bytes = Base64.decode(it.imagen, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?.asImageBitmap()?.let { bmp -> capturedImageBitmap = bmp }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        //boton regreso a pantalla principal
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
        }
        Text(
            text = if (id != null) "Editar Producto" else "Agregar Producto",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        if (!id.isNullOrEmpty()) {
            Text("ID: $id", textAlign = TextAlign.Center, fontSize = 20.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre*") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio*") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    imageFile = context.createImageFile()
                    imageFile?.let { file ->
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        cameraLauncher.launch(uri)
                    }
                } else cameraPermission.launch(Manifest.permission.CAMERA)
            }) { Text("Tomar foto") }
            Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Galería") }
        }
        //  Botón para seleccionar carpeta destino
        Modifier.padding(12.dp)
        SelectSaveFolderButton()

        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    Toast.makeText(context, "Imagen guardada", Toast.LENGTH_SHORT).show()
                }
        ) {
            capturedImageBitmap?.let {
                Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } ?: Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (nombre.isBlank() || precio.isBlank()) {
                Toast.makeText(context, "Nombre y precio obligatorios", Toast.LENGTH_SHORT).show()
                return@Button
            }
            val db = auxSQLite.writableDatabase
            val success = if (id != null)
                auxSQLite.updateProduct(db, id, nombre, precio, descripcion, imagen) > 0
            else
                auxSQLite.addProduct(nombre, precio, descripcion, imagen)?.let { it != -1L } == true
            db.close()
            if (success) navController.popBackStack() else Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(if (id != null) "Actualizar" else "Agregar")
        }
    }
}

@Composable
fun SelectSaveFolderButton(
    modifier: Modifier = Modifier,
    onFolderSelected: () -> Unit = {}
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        treeUri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit().putString("save_folder_uri", it.toString()).apply()
            Toast.makeText(context, "Carpeta seleccionada", Toast.LENGTH_SHORT).show()
            onFolderSelected()
        }
    }

    Button(
        onClick = { launcher.launch(null) },
        modifier = modifier
    ) {
        Text("Elegir carpeta destino")
    }
}

private fun Context.getSaveFolderUri(): Uri? =
    getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getString("save_folder_uri", null)
        ?.let { Uri.parse(it) }

fun Context.saveImageToSelectedFolder(bitmap: Bitmap, fileName: String): Boolean {
    val treeUri = getSaveFolderUri() ?: run {
        Toast.makeText(this, "Selecciona carpeta primero", Toast.LENGTH_SHORT).show()
        return false
    }
    return try {
        require(!bitmap.isRecycled)
        val docFolder = DocumentFile.fromTreeUri(this, treeUri)
            ?: throw IllegalStateException("No acceso al directorio")
        val newFile = docFolder.createFile("image/jpeg", "$fileName.jpg")
            ?: throw IllegalStateException("No se creó el archivo")
        contentResolver.openOutputStream(newFile.uri).use { out ->
            requireNotNull(out)
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)) {
                throw IllegalStateException("Compresión fallida")
            }
        }
        MediaScannerConnection.scanFile(this, arrayOf(newFile.uri.path), null, null)
        true
    } catch (e: Exception) {
        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
        false
    }
}

fun Bitmap.resizeToThumbnail(): Bitmap = this.scale(100, 100)

fun Context.createImageFile(fileName: String = generateImageFileName()): File {
    val storageDir = File(filesDir, "product_images").apply { if (!exists()) mkdirs() }
    return File(storageDir, "${fileName}.jpg").also { if (!it.exists()) it.createNewFile() }
}

fun generateImageFileName(): String =
    "PRODUCT_IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"

fun ImageBitmap.asAndroidBitmap(): Bitmap {
    val config = if (this.config == ImageBitmapConfig.Argb8888) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    val bmp = Bitmap.createBitmap(width, height, config)
    val buffer = IntArray(width * height)
    readPixels(buffer, 0, width, 0, 0, width, height)
    bmp.setPixels(buffer, 0, width, 0, 0, width, height)
    return bmp
}

@Composable
fun PantallaPrincipal(navControlador: NavController){
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
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(text="Productos Disponibles", fontSize=24.sp)
            IconButton(onClick = { navControlador.navigate("ProductoMto") }) {
                Icon(Icons.Filled.Add, contentDescription="Añadir")
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
                } ?: Icon(Icons.Filled.Warning, contentDescription=null, Modifier.size(48.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, maxLines=2, overflow=TextOverflow.Ellipsis)
                Text(product.description, maxLines=1, overflow=TextOverflow.Ellipsis)
                Text("$${product.price}")
            }
            IconButton(onClick={ navController.navigate("ProductoMto?id=${product.id}") }) { Icon(Icons.Filled.Edit, contentDescription="Editar") }
            IconButton(onClick={ show=true }) { Icon(Icons.Filled.Delete, contentDescription="Eliminar") }
        }
        ButtonDelete(show, onDismiss={show=false}, onConfirm={ onDelete(product.id); show=false })
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
    MtoProductoTheme { PantallaPrincipal(rememberNavController()) }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductCard() {
    MtoProductoTheme {
        ProductCard(Producto("","Ejemplo","0.00","Desc",""), rememberNavController()) {}
    }
}
