package com.example.mtoproducto.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import androidx.navigation.NavController
import com.example.mtoproducto.DBHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit

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
    var pulsado by remember { mutableStateOf(false) }

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
                descripcion = if (it.description.equals("sin descripcion", ignoreCase = true)) "" else it.description
                imagen = it.imagen
                if (it.imagen.isNotEmpty()) {
                    val bytes = Base64.decode(it.imagen, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?.asImageBitmap()?.let { bmp -> capturedImageBitmap = bmp }
                }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = {
                        if(!pulsado){
                            navController.popBackStack()
                            pulsado = true
                        } },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = if (id != null) "Editar Producto" else "Agregar Producto",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp
                )
            }
        }

        item {
            Surface(Modifier
                .fillMaxWidth()
                .padding(2.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!id.isNullOrEmpty()) {
                        Text("ID: $id", textAlign = TextAlign.Center, fontSize = 20.sp, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre*") }, modifier = Modifier.fillMaxWidth())

                    OutlinedTextField(
                        value = precio,
                        onValueChange = { nuevoValor ->
                            if (nuevoValor.matches(Regex("^\\d*\\.?\\d*$"))) {
                                precio = nuevoValor
                            }
                        },
                        label = { Text("Precio*") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )

                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

                    Text("Los campos con un \"*\" son obligatorios.",  textAlign = TextAlign.Center, fontSize = 24.sp)
                }
            }
        }

        item{
            Surface (
                Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.background
            ){
                Column (Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)){
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                        // Botón para galería
                        IconButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Abrir galería",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("Galería", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    imageFile = context.createImageFile()
                                    imageFile?.let { file ->
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )
                                        cameraLauncher.launch(uri)
                                    }
                                } else {
                                    cameraPermission.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Tomar foto",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("Tomar foto", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Botón para seleccionar carpeta (nuevo)
                        val folderLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.OpenDocumentTree()
                        ) { treeUri: Uri? ->
                            treeUri?.let {
                                context.contentResolver.takePersistableUriPermission(
                                    it,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                )
                                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    .edit { putString("save_folder_uri", it.toString()) }
                                Toast.makeText(context, "Carpeta seleccionada", Toast.LENGTH_SHORT).show()
                            }
                        }

                        IconButton(
                            onClick = { folderLauncher.launch(null) },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Folder,
                                    contentDescription = "Seleccionar carpeta",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("Elegir carpeta destino", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Text("Para guardar la imagen, haz \"click\" sobre ella.", textAlign = TextAlign.Center, fontSize = 24.sp)

                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(100.dp)
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    if (capturedImageBitmap != null) {
                                        Toast.makeText(context, "Imagen guardada", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No existe ninguna imagen para guardar", Toast.LENGTH_SHORT).show()
                                    }
                                }, Alignment.Center
                        ) {
                            capturedImageBitmap?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } ?: Icon(
                                Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                }
            }
        }

        item{
            Button(onClick = {
                if (nombre.isBlank() || precio.isBlank()) {
                    Toast.makeText(context, "Nombre y precio obligatorios", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val db = auxSQLite.writableDatabase
                val cleanDescription = if (descripcion.isBlank()) null else descripcion
                val success = if (id != null)
                    auxSQLite.updateProduct(db, id, nombre, precio, cleanDescription, imagen) > 0
                else
                    auxSQLite.addProduct(nombre, precio, cleanDescription, imagen)?.let { it != -1L } == true
                db.close()
                if (success) navController.popBackStack() else Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (id != null) "Actualizar" else "Agregar")
            }
        }

    }
}

fun Context.saveImageToSelectedFolder(bitmap: Bitmap, fileName: String): Boolean {
    return try {
        val file = File(getExternalFilesDir(null), "$fileName.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        MediaScannerConnection.scanFile(this, arrayOf(file.path), null, null)
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