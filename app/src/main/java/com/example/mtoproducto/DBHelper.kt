package com.example.mtoproducto

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class DBHelper(context: Context) : SQLiteOpenHelper(context, "mibase.db" , null, 1) {

    val cheetosBase64 = convertImageToBase64(context, R.drawable.cheetos)
    val doritosBase64 = convertImageToBase64(context, R.drawable.doritos)
    val rufflesBase64 = convertImageToBase64(context, R.drawable.ruffles)
    val cocaBase64 = convertImageToBase64(context, R.drawable.colaloca)
    val cacahuatesBase64 = convertImageToBase64(context, R.drawable.cacahuates)
    val spriteBase64 = convertImageToBase64(context, R.drawable.sprite)
    val gansitoBase64 = convertImageToBase64(context, R.drawable.gansito)
    val mariasBase64 = convertImageToBase64(context, R.drawable.maria)
    val kinderBase64 = convertImageToBase64(context, R.drawable.kinder)
    val arizonaBase64 = convertImageToBase64(context, R.drawable.arizona)

    override fun onCreate(db: SQLiteDatabase?) {
        val query1 = "CREATE TABLE producto(\n" +
                "id_producto INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "nombre TEXT NOT NULL,\n" +
                "precio DOUBLE NOT NULL,\n" +
                "descripcion TEXT,\n" +
                "imagen TEXT" +
                ");"
        val query2 = "INSERT INTO producto VALUES (" +
                "NULL, 'Cheetos', 17.5, NULL, '$cheetosBase64'),(" +
                "NULL, 'Doritos', 20, NULL, '$doritosBase64'),(" +
                "NULL, 'Ruffles', 17, NULL, '$rufflesBase64'),(" +
                "NULL, 'Coca-Cola 600 ml', 22, NULL, '$cocaBase64'),(" +
                "NULL, 'Cacahuates', 12, NULL, '$cacahuatesBase64'),(" +
                "NULL, 'Sprite', 17.5, NULL, '$spriteBase64'),(" +
                "NULL, 'Gansito', 20, NULL, '$gansitoBase64'),(" +
                "NULL, 'Galletas Maria', 28.54, NULL, '$mariasBase64'),(" +
                "NULL, 'Huevito Kinder', 55, NULL, '$kinderBase64'),(" +
                "NULL, 'Arizona', 18, NULL, '$arizonaBase64');"

        db?.execSQL(query1)
        db?.execSQL(query2)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Modificas cuando te toque actualizar tu app")
    }

    fun convertImageToBase64(context: Context, drawableId: Int): String {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)

        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}