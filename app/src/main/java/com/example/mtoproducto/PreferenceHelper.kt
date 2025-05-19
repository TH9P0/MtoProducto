package com.example.mtoproducto

import android.content.Context

class PreferenceHelper(context: Context) {
    private val prefs = context.getSharedPreferences("mis_preferencias", Context.MODE_PRIVATE)

    fun guardarColorUsuario(colorFavorito: String){
        prefs.edit().putString("color_favorito",colorFavorito).apply()
    }

    fun leerColorUsuario(): String?{
        return prefs.getString("color_favorito",null)
    }
}