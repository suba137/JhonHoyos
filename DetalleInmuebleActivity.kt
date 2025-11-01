package com.example.fortiva

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

data class Inmueble(
    val id: String,
    val nombre: String,
    val valorTotal: Double,
    var disponible: Double,
    val imagenUrl: String
)

data class CompraParcial(
    val inmueble: Inmueble,
    var porcentajeCompra: Double     // porcentaje absoluto sobre 100 del valor total
) {
    val valorCompra: Double
        get() = inmueble.valorTotal * (porcentajeCompra / 100.0)

    val valorRestante: Double
        get() = inmueble.valorTotal * (inmueble.disponible / 100.0)
}


class CarritoInmueblesViewModel : ViewModel() {

    private val _carrito = MutableLiveData<MutableList<CompraParcial>>(mutableListOf())
    val carrito: LiveData<MutableList<CompraParcial>> = _carrito

    val totalCompra: LiveData<Double> = carrito.map { lista ->
        lista.sumOf { it.valorCompra }
    }

    fun agregarCompra(inmueble: Inmueble, porcentaje: Double): Boolean {
        if (porcentaje <= 0.0) return false
        if (porcentaje > inmueble.disponible) return false

        val lista = _carrito.value ?: mutableListOf()
        val existente = lista.find { it.inmueble.id == inmueble.id }

        if (existente != null) {
            val nuevo = existente.porcentajeCompra + porcentaje
            if (nuevo > 100.0) return false
            existente.porcentajeCompra = nuevo
        } else {
            lista.add(CompraParcial(inmueble, porcentaje))
        }

        inmueble.disponible -= porcentaje
        if (inmueble.disponible < 0.0) inmueble.disponible = 0.0

        _carrito.value = lista
        return true
    }

    fun agregarCompraRelativo(inmueble: Inmueble, porcentajeRel: Double): Boolean {
        if (porcentajeRel <= 0.0) return false
        if (inmueble.disponible <= 0.0) return false
        if (porcentajeRel > 100.0) return false

        // porcentaje absoluto del total que se va a comprar
        val porcentajeAbs = inmueble.disponible * (porcentajeRel / 100.0)

        // sanity check
        if (porcentajeAbs <= 0.0) return false
        if (porcentajeAbs > inmueble.disponible) return false

        val lista = _carrito.value ?: mutableListOf()
        val existente = lista.find { it.inmueble.id == inmueble.id }

        if (existente != null) {
            val nuevo = existente.porcentajeCompra + porcentajeAbs
            if (nuevo > 100.0) return false
            existente.porcentajeCompra = nuevo
        } else {
            lista.add(CompraParcial(inmueble, porcentajeAbs))
        }

        // Reducir disponibilidad (en porcentajes absolutos)
        inmueble.disponible -= porcentajeAbs
        if (inmueble.disponible < 0.0) inmueble.disponible = 0.0

        _carrito.value = lista
        return true
    }



    fun eliminarCompra(compra: CompraParcial) {
        val lista = _carrito.value ?: mutableListOf()
        compra.inmueble.disponible += compra.porcentajeCompra
        if (compra.inmueble.disponible > 100.0) compra.inmueble.disponible = 100.0
        lista.removeAll { it.inmueble.id == compra.inmueble.id }
        _carrito.value = lista
    }

    fun finalizarCompra() {
        _carrito.value = mutableListOf()
    }

    fun limpiarCarrito() {
        val lista = _carrito.value ?: mutableListOf()
        lista.forEach { it.inmueble.disponible += it.porcentajeCompra }
        _carrito.value = mutableListOf()
    }
}
