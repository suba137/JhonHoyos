package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executor

class CarritoActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnFinalizar: Button
    private lateinit var adapter: CarritoAdapter
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        recycler = findViewById(R.id.recyclerCarrito)
        tvTotal = findViewById(R.id.tvTotal)
        btnFinalizar = findViewById(R.id.btnFinalizarCompra)
        executor = ContextCompat.getMainExecutor(this)

        recycler.layoutManager = LinearLayoutManager(this)
        val viewModel = FortivaApp.carritoViewModel

        adapter = CarritoAdapter(viewModel.carrito.value ?: mutableListOf()) { compra ->
            viewModel.eliminarCompra(compra)
        }
        recycler.adapter = adapter

        // Observa cambios en el carrito
        viewModel.carrito.observe(this) {
            adapter.notifyDataSetChanged()
        }

        // Observa el total
        viewModel.totalCompra.observe(this) { total ->
            val formato = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            tvTotal.text = "Total: ${formato.format(total)}"
        }

        // Botón de finalizar
        btnFinalizar.text = "Finalizar compra"
        btnFinalizar.setOnClickListener {
            val total = viewModel.totalCompra.value ?: 0.0
            if (total > 0) {
                mostrarConfirmacionCompra(total)
            } else {
                irAlCatalogo()
            }
        }
    }

    private fun mostrarConfirmacionCompra(total: Double) {
        val formato = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val mensaje = "¿Deseas confirmar tu compra por ${formato.format(total)}?"

        AlertDialog.Builder(this)
            .setTitle("Confirmar compra")
            .setMessage(mensaje)
            .setPositiveButton("Confirmar") { _, _ ->
                mostrarBiometria()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarBiometria() {
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    finalizarCompra()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    mostrarDialogo("Autenticación cancelada o fallida.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirmar compra")
            .setSubtitle("Usa tu huella o autenticación biométrica")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun finalizarCompra() {
        FortivaApp.carritoViewModel.finalizarCompra()
        mostrarDialogo("Compra completada exitosamente.")
        irAlCatalogo()
    }

    private fun irAlCatalogo() {
        val intent = Intent(this, CatalogoActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarDialogo(mensaje: String) {
        AlertDialog.Builder(this)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}
