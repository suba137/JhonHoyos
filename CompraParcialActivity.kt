package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CompraParcialActivity : AppCompatActivity() {

    private lateinit var progresoDisponibilidad: ProgressBar
    private lateinit var tvDisponibilidad: TextView
    private lateinit var etPorcentaje: EditText
    private lateinit var btnComprar: Button

    private var inmueble: Inmueble? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compra_parcial)

        progresoDisponibilidad = findViewById(R.id.progresoDisponibilidad)
        tvDisponibilidad = findViewById(R.id.tvDisponibilidad)
        etPorcentaje = findViewById(R.id.etPorcentaje)
        btnComprar = findViewById(R.id.btnComprar)

        // Obtener el inmueble
        val idInmueble = intent.getStringExtra("idInmueble")
        inmueble = FortivaApp.listaInmuebles.find { it.id == idInmueble }

        inmueble?.let {
            tvDisponibilidad.text = "Disponible: ${it.disponible.toInt()}%"
            progresoDisponibilidad.progress = it.disponible.toInt()
        }

        // Detectar cambios en el campo para vista previa
        etPorcentaje.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                actualizarVistaPrevia()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnComprar.setOnClickListener {
            val valorTexto = etPorcentaje.text.toString()
            if (valorTexto.isEmpty()) {
                Toast.makeText(this, "Ingresa un porcentaje válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val porcentaje = valorTexto.toDoubleOrNull() ?: 0.0
            if (porcentaje <= 0) {
                Toast.makeText(this, "El porcentaje debe ser mayor que 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            realizarCompraRelativo(porcentaje)
        }
    }

    private fun actualizarVistaPrevia() {
        inmueble?.let {
            val porcentaje = etPorcentaje.text.toString().toDoubleOrNull() ?: 0.0
            val restante = it.disponible - porcentaje
            val nuevoPorcentaje = if (restante < 0) 0.0 else restante

            progresoDisponibilidad.progress = nuevoPorcentaje.toInt()
            tvDisponibilidad.text = "Disponible tras compra: ${nuevoPorcentaje.toInt()}%"
        }
    }

    private fun realizarCompraRelativo(porcentajeRel: Double) {
        inmueble?.let { inmuebleSeleccionado ->
            if (porcentajeRel > inmuebleSeleccionado.disponible) {
                Toast.makeText(this, "No hay tanta disponibilidad", Toast.LENGTH_SHORT).show()
                return
            }

            val agregado = FortivaApp.carritoViewModel.agregarCompraRelativo(
                inmuebleSeleccionado,
                porcentajeRel
            )

            if (agregado) {
                Toast.makeText(this, "Compra añadida al carrito", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CarritoActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "No se pudo agregar la compra", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
