package com.example.mtoproducto.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.mtoproducto.DBHelper
import com.example.mtoproducto.SelectSaveFolderButton
import com.example.mtoproducto.createImageFile
import com.example.mtoproducto.generateImageFileName
import com.example.mtoproducto.resizeToThumbnail
import com.example.mtoproducto.saveImageToSelectedFolder
import java.io.ByteArrayOutputStream
import java.io.File

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