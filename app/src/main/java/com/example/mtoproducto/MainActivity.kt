package com.example.mtoproducto

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UIPrincipal() }
    }
}


fun buttonEdit(){
    //TODO
}

@Composable
fun buttonDelete(show:Boolean, onDismiss:() -> Unit, onConfirm:()-> Unit){
    //TODO: Show message for double check on deleting element
    if (show)
        AlertDialog(onDismissRequest = {onDismiss()}, confirmButton = { TextButton(onClick = {onConfirm()}){Text("Continuar")} }, dismissButton = {TextButton(onClick = {onDismiss()}){Text("Descartar")}}, title = {Text("Seguro que deseas eliminar este articulo?")}, text = {Text("Si aceptas eliminar este articulo ya no aparecera ni podras rehacer esta operacion, deseas continuar?")})

}

@Composable
fun UIPrincipal(){
    val auxSQLite = DBHelper(LocalContext.current)
    val base = auxSQLite.writableDatabase
    val cursor: Cursor = base.rawQuery("SELECT * FROM producto;", null)
    val lista = mutableListOf<String>()
    while(cursor.moveToNext()){
        lista.add(cursor.getString(1))
    }
    cursor.close()
    base.close()

    Text("Productos Disponibles")

    LazyColumn {
        items(10){ index ->
            Text(text = "Item: $index")
            productCard()
        }
    }

    //Mostrar los datos de la base
    Column {
        Text(lista.get(0).toString())
    }

}

@Composable
fun productCard(){
    var show by rememberSaveable { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth()){
        Text("Product Image")
        Column(){
            Text("Product Name")
            Text("Product Price")
            Text("Product Description")
            Row{
                Button(onClick = {buttonEdit()}) {Text("Editar") }
                Button(onClick = {show = true}) {Text("Eliminar") }
            }
        }
    }

    buttonDelete(show, {show = false},{ Log.i("aris","click")})
}

@Preview(showBackground = true)
@Composable
fun Previsualizacion() {
    UIPrincipal()
}