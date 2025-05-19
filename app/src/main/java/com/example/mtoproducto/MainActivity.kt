package com.example.mtoproducto

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.scale
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import androidx.core.graphics.createBitmap
import androidx.navigation.NavHostController
import com.example.mtoproducto.screens.ProductoMto
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
            RGBColorPicker(context)
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
        ?.let { it.toUri() }

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

fun ImageBitmap.asAndroidBitmap(): Bitmap {
    val config = if (this.config == ImageBitmapConfig.Argb8888) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    val bmp = createBitmap(width, height, config)
    val buffer = IntArray(width * height)
    readPixels(buffer, 0, width, 0, 0, width, height)
    bmp.setPixels(buffer, 0, width, 0, 0, width, height)
    return bmp
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
    MtoProductoTheme { UIPrincipal(rememberNavController()) }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductCard() {
    MtoProductoTheme {
        ProductCard(Producto("","Ejemplo","0.00","Desc",""), rememberNavController()) {}
    }
}

@Composable
fun MtoProductoTheme(content: @Composable () -> Unit) {

}