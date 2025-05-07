package com.example.mtoproducto

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.content.ContentValues

/**
 * Clase auxiliar para gestionar la base de datos SQLite de la aplicación.
 * Maneja la creación, actualización y operaciones básicas de la base de datos de productos.
 *
 * @param context Contexto de la aplicación necesario para operaciones de base de datos
 */
class DBHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Constantes de la base de datos
        private const val DATABASE_NAME = "productos.db"
        private const val DATABASE_VERSION = 1

        // Constantes de la tabla producto
        const val TABLE_PRODUCTO = "producto"
        const val COLUMN_ID = "id_producto"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_PRECIO = "precio"
        const val COLUMN_DESCRIPCION = "descripcion"
        const val COLUMN_IMAGEN = "imagen"

        // Tamaño de redimensionamiento de imágenes
        private const val IMAGE_SIZE = 100
    }

    // Imágenes codificadas en Base64 (cargadas solo una vez)
    private val productImages by lazy {
        mapOf(
            "cheetos" to convertImageToBase64(context, R.drawable.cheetos),
            "doritos" to convertImageToBase64(context, R.drawable.doritos),
            "ruffles" to convertImageToBase64(context, R.drawable.ruffles),
            "cocacola" to convertImageToBase64(context, R.drawable.colaloca),
            "cacahuates" to convertImageToBase64(context, R.drawable.cacahuates),
            "sprite" to convertImageToBase64(context, R.drawable.sprite),
            "gansito" to convertImageToBase64(context, R.drawable.gansito),
            "marias" to convertImageToBase64(context, R.drawable.maria),
            "kinder" to convertImageToBase64(context, R.drawable.kinder),
            "arizona" to convertImageToBase64(context, R.drawable.arizona)
        )
    }

    /**
     * Crea la base de datos e inserta datos iniciales
     */
    override fun onCreate(db: SQLiteDatabase?) {
        // Crear la tabla de productos
        val createTableQuery = """
            CREATE TABLE $TABLE_PRODUCTO (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_PRECIO REAL NOT NULL,
                $COLUMN_DESCRIPCION TEXT,
                $COLUMN_IMAGEN TEXT
            );
        """.trimIndent()

        db?.execSQL(createTableQuery)

        // Insertar datos iniciales
        insertInitialData(db)
    }

    /**
     * Inserta los datos iniciales en la tabla producto
     */
    private fun insertInitialData(db: SQLiteDatabase?) {
        // Lista de productos iniciales
        val initialProducts = arrayOf(
            arrayOf("Cheetos", 17.5, null, productImages["cheetos"]),
            arrayOf("Doritos", 20.0, null, productImages["doritos"]),
            arrayOf("Ruffles", 17.0, null, productImages["ruffles"]),
            arrayOf("Coca-Cola 600 ml", 22.0, null, productImages["cocacola"]),
            arrayOf("Cacahuates", 12.0, null, productImages["cacahuates"]),
            arrayOf("Sprite", 17.5, null, productImages["sprite"]),
            arrayOf("Gansito", 20.0, null, productImages["gansito"]),
            arrayOf("Galletas Maria", 28.54, null, productImages["marias"]),
            arrayOf("Huevito Kinder", 55.0, null, productImages["kinder"]),
            arrayOf("Arizona", 18.0, null, productImages["arizona"])
        )

        // Insertar cada producto usando ContentValues
        initialProducts.forEach { product ->
            val values = ContentValues().apply {
                put(COLUMN_NOMBRE, product[0] as String)
                put(COLUMN_PRECIO, product[1] as Double)
                product[2]?.let { put(COLUMN_DESCRIPCION, it as String) }
                put(COLUMN_IMAGEN, product[3] as String)
            }
            db?.insert(TABLE_PRODUCTO, null, values)
        }
    }

    /**
     * Agrega un nuevo producto a la base de datos
     *
     * @param nombre Nombre del producto
     * @param precio Precio del producto
     * @param descripcion Descripción opcional del producto
     * @param imagenBase64 Imagen del producto codificada en Base64 (opcional)
     * @param imagenBitmap Imagen del producto como Bitmap (opcional)
     * @return ID del nuevo producto insertado o -1 si hubo error
     */
    fun agregarProducto(
        nombre: String,
        precio: Double,
        descripcion: String? = null,
        imagenBase64: String? = null,
        imagenBitmap: Bitmap? = null
    ): Long {
        val finalImagenBase64 = imagenBase64 ?: imagenBitmap?.let { bitmap ->
            // Convertir el Bitmap a Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, nombre)
            put(COLUMN_PRECIO, precio)
            descripcion?.let { put(COLUMN_DESCRIPCION, it) }
            finalImagenBase64?.let { put(COLUMN_IMAGEN, it) }
        }

        return writableDatabase.use { db ->
            db.insert(TABLE_PRODUCTO, null, values)
        }
    }

    /**
     * Elimina un producto de la base de datos por su ID
     *
     * @param id ID del producto a eliminar
     * @return Número de filas afectadas
     */
    fun eliminarProducto(id: Long): Int {
        return writableDatabase.use { db ->
            db.delete(TABLE_PRODUCTO, "$COLUMN_ID = ?", arrayOf(id.toString()))
        }
    }

    /**
     * Elimina un producto de la base de datos por su nombre
     *
     * @param nombre Nombre del producto a eliminar
     * @return Número de filas afectadas
     */
    fun eliminarProductoPorNombre(nombre: String): Int {
        return writableDatabase.use { db ->
            db.delete(TABLE_PRODUCTO, "$COLUMN_NOMBRE = ?", arrayOf(nombre))
        }
    }

    /**
     * Actualiza un producto existente
     *
     * @param id ID del producto a actualizar
     * @param nombre Nuevo nombre
     * @param precio Nuevo precio
     * @param descripcion Nueva descripción
     * @param imagenBase64 Nueva imagen en Base64
     * @return Número de filas afectadas
     */
    fun actualizarProducto(id: Long, nombre: String, precio: Double, descripcion: String? = null, imagenBase64: String? = null): Int {
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, nombre)
            put(COLUMN_PRECIO, precio)
            descripcion?.let { put(COLUMN_DESCRIPCION, it) } ?: put(COLUMN_DESCRIPCION, null)
            imagenBase64?.let { put(COLUMN_IMAGEN, it) }
        }

        return writableDatabase.use { db ->
            db.update(TABLE_PRODUCTO, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        }
    }

    /**
     * Obtiene todos los productos de la base de datos
     *
     * @return Cursor con todos los productos
     */
    fun obtenerTodosLosProductos(): android.database.Cursor {
        return readableDatabase.rawQuery("SELECT * FROM $TABLE_PRODUCTO", null)
    }

    /**
     * Maneja las actualizaciones de la base de datos
     * cuando la versión cambia
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Implementación para futuros cambios de esquema
        if (oldVersion < 2) {
            // Código para actualizar de versión 1 a 2
            // Por ejemplo:
            // db?.execSQL("ALTER TABLE $TABLE_PRODUCTO ADD COLUMN nueva_columna TEXT;")
        }
    }

    /**
     * Convierte una imagen de recursos a cadena Base64
     *
     * @param context Contexto de la aplicación
     * @param drawableId ID del recurso drawable
     * @return Cadena Base64 de la imagen
     */
    fun convertImageToBase64(context: Context, drawableId: Int): String {
        // Decodificar el recurso en bitmap
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
        // Redimensionar para optimizar almacenamiento
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)

        // Convertir a array de bytes
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Codificar en Base64
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}