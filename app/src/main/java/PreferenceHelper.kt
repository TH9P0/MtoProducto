package com.example.sharedpreferences

import android.content.Context

class PreferenceHelper(context:Context) {
    private val prefs = context.getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE)

    /* Codigo de Preferencias de nombre
    fun guardarNombreUsuario(nombreUsuario:String){
        prefs.edit().putString("nombre_usuario",nombreUsuario).apply()
    }

    fun leerNombreUsuario(): String{
        return prefs.getString("nombre_usuario","") ?: ""
    }*/
    
    fun guardarColorUsuario(colorFavorito: String){
        prefs.edit().putString("color_favorito",colorFavorito).apply()
    }

    fun leerColorUsuario(): String{
        return prefs.getString("color_favorito","") ?: ""
    }
}