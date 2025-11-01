package com.example.fortiva

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RecordarActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var btnRecuperar: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordar)

        // Referencias
        etCorreo = findViewById(R.id.etCorreo)
        btnRecuperar = findViewById(R.id.btnRecuperar)
        auth = FirebaseAuth.getInstance()

        btnRecuperar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()

            if (correo.isNotEmpty()) {
                auth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Se ha enviado un enlace de recuperación a tu correo.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "No se pudo enviar el correo. Verifica el email.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor ingresa tu correo.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
