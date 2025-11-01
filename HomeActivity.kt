package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class HomeActivity : AppCompatActivity() {

    private lateinit var emailTextView: TextView
    private lateinit var providerTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var btnVerCatalogo: Button
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        emailTextView = findViewById(R.id.emailTextView)
        providerTextView = findViewById(R.id.providerTextView)
        logoutButton = findViewById(R.id.logOutButton)
        btnVerCatalogo = findViewById(R.id.btnVerCatalogo)
        executor = ContextCompat.getMainExecutor(this)

        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email == null || provider == null) {
            // No hay sesión guardada, volver al login
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        emailTextView.text = email
        providerTextView.text = provider

        // Si hay biometría disponible, ofrecer autenticació

        btnVerCatalogo.setOnClickListener {
            startActivity(Intent(this, CatalogoActivity::class.java))
        }

        logoutButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    cerrarSesion(provider)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }




    private fun cerrarSesion(provider: String?) {
        val prefsEdit = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefsEdit.clear()
        prefsEdit.apply()

        FirebaseAuth.getInstance().signOut()

        if (provider == "GOOGLE") {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
        }

        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
