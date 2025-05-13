package com.example.mtoproducto

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

class DBHelper(context: Context) : SQLiteOpenHelper(context, "mibase.db" , null, 1) {

    val tableName = "producto"
    val columnId = "id_producto"
    val columnName = "nombre"
    val columnPrice = "precio"
    val columnDescription = "descripcion"
    val columnImage = "imagen"

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
        val query1 = "CREATE TABLE $tableName(\n" +
                "$columnId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "$columnName TEXT NOT NULL,\n" +
                "$columnPrice DOUBLE NOT NULL,\n" +
                "$columnDescription TEXT,\n" +
                "$columnImage TEXT" +
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
        db?.execSQL("DROP TABLE IF EXISTS $tableName")
        onCreate(db)
    }

    fun addProduct(nombre:String, precio:String, descripcion:String?, imagen:String?): Long?{
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(columnName,nombre)
            put(columnPrice,precio)
            put(columnDescription,descripcion)
            put(columnImage,imagen)
        }
        val result = db?.insert(tableName, null, cv)
        db?.close()
        return result
    }

    fun deleteProduct(id:String): Boolean{
        val db = this.writableDatabase
        return try {
            val filas = db.delete(tableName, "$columnId=?", arrayOf(id))
            filas > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun updateProduct(db: SQLiteDatabase, id: String, nombre: String, precio: String, descripcion: String?, imagen: String?): Int{
        val cv = ContentValues().apply {
            put(columnName,nombre)
            put(columnPrice,precio)
            put(columnDescription,descripcion)
            put(columnImage,imagen)
        }
        val result = db.update(tableName,cv,"$columnId=?",arrayOf(id))
        db.close()
        return result
    }

    fun getProductById(id:String):Producto?{
        val db = this.readableDatabase
        val cursor = db.query(tableName,arrayOf(columnId, columnName, columnPrice, columnDescription, columnImage),"$columnId=?",arrayOf(id),null,null,null)

        return if (cursor.moveToFirst()) {
            Producto(
                id = cursor.getString(0),
                name = cursor.getString(1),
                price = cursor.getString(2),
                description = cursor.getString(3) ?: "",
                imagen = cursor.getString(4) ?: ""
            )
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }



    fun convertImageToBase64(context: Context, drawableId: Int): String {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        val scaledBitmap =  Bitmap.createScaledBitmap(bitmap, 100, 100, true)

        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}