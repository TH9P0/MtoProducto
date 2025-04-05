package com.example.mtoproducto

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "mibase.db" , null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query1 = "CREATE TABLE producto(\n" +
                "id_producto INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "nombre TEXT NOT NULL,\n" +
                "precio DOUBLE NOT NULL,\n" +
                "descripcion TEXT\n" +
                ");"
        val query2 = "INSERT INTO producto VALUES (" +
                "NULL, 'Cheetos', 17.5, NULL),(" +
                "NULL, 'Doritos', 20, NULL),(" +
                "NULL, 'Ruffles', 17, NULL),(" +
                "NULL, 'Coca-Cola 600 ml', 22, NULL),(" +
                "NULL, 'Cacahuates', 12, NULL),(" +
                "NULL, 'Sprite', 17.5, NULL),(" +
                "NULL, 'Gansito', 20, NULL),(" +
                "NULL, 'Galletas Maria', 28.54, NULL),(" +
                "NULL, 'Huevito Kinder', 55, NULL),(" +
                "NULL, 'Arizona', 18, NULL);"

        db?.execSQL(query1)
        db?.execSQL(query2)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Modificas cuando te toque actualizar tu app")
    }
}