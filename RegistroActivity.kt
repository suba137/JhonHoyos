package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()

        val etCorreo = findViewById<EditText>(R.id.etCorreoRegistro)
        val etContrasena = findViewById<EditText>(R.id.etContrasenaRegistro)
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        val etDocumento = findViewById<EditText>(R.id.etDocumento)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val email = etCorreo.text.toString().trim()
            val password = etContrasena.text.toString().trim()
            val usuario = etUsuario.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val documento = etDocumento.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || usuario.isEmpty() || direccion.isEmpty() || documento.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔐 Crear usuario con Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            // 🔹 Enviar datos al backend (PostgreSQL)
                            enviarDatosAlServidor(userId, email, usuario, direccion, documento)
                        }
                    } else {
                        Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // --- 🔹 Enviar los datos del usuario a tu backend (que guarda en PostgreSQL) ---
    private fun enviarDatosAlServidor(uid: String, email: String, usuario: String, direccion: String, documento: String) {
        thread {
            try {
                val url = URL("https://tu-servidor.com/api/usuarios") // ✅ Cambia por tu endpoint real
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true

                // Construir el cuerpo JSON
                val json = JSONObject().apply {
                    put("uid", uid)
                    put("correo", email)
                    put("usuario", usuario)
                    put("direccion", direccion)
                    put("documento", documento)
                }

                // Enviar el JSON al servidor
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(json.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                connection.disconnect()

                runOnUiThread {
                    if (responseCode == 200 || responseCode == 201) {
                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("provider", "BASIC")
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error al guardar datos en el servidor (HTTP $responseCode)", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
