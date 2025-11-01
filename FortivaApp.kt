package com.example.fortiva

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class FortivaApp : Application() {
    companion object {
        // 🛒 ViewModel global del carrito
        val carritoViewModel = CarritoInmueblesViewModel()

        // 📦 Lista de inmuebles compartida
        val listaInmuebles = mutableListOf(
            Inmueble("001", "Apartamento en Cali", 200000000.0, 100.0, ""),
            Inmueble("002", "Casa en Medellín", 300000000.0, 100.0, ""),
            Inmueble("003", "Lote en Bogotá", 150000000.0, 100.0, "")
        )
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
