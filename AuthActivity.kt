package com.example.fortiva

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.Executor

class AuthActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnContinuar: Button
    private lateinit var tvCrearCuenta: TextView
    private lateinit var tvOlvidarContrasena: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var googleSignInClient: GoogleSignInClient

    private var failedAttempts = 0
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    companion object {
        private const val GOOGLE_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // ✅ Forzar modo claro en toda la app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // --- Referencias de vistas ---
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnContinuar = findViewById(R.id.btnContinuar)
        tvCrearCuenta = findViewById(R.id.tvCrearCuenta)
        tvOlvidarContrasena = findViewById(R.id.tvOlvidarContrasena)

        // --- Inicializaciones ---
        auth = FirebaseAuth.getInstance()
        executor = ContextCompat.getMainExecutor(this)
        configurarBiometria()

        // --- Verificar sesión guardada ---
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)
        if (email != null && provider != null) {
            mostrarBiometria(email, provider)
        }

        // --- Configuración Google Sign-In ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- Iniciar sesión con correo ---
        btnContinuar.setOnClickListener {
            val emailInput = etCorreo.text.toString().trim()
            val passwordInput = etContrasena.text.toString().trim()

            if (emailInput.isNotEmpty() && passwordInput.isNotEmpty()) {
                auth.signInWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            mostrarBiometria(emailInput, "EMAIL")
                        } else {
                            mostrarDialogo("Error al iniciar sesión. Verifica tus credenciales.")
                        }
                    }
            } else {
                mostrarDialogo("Por favor completa todos los campos.")
            }
        }

        // --- Iniciar sesión con Google ---
        val btnGoogle: com.google.android.gms.common.SignInButton = findViewById(R.id.btnGoogle)
        btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }

        // --- Crear cuenta ---
        tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        // --- Recuperar contraseña ---
        tvOlvidarContrasena.setOnClickListener {
            startActivity(Intent(this, RecordarActivity::class.java))
        }
    }

    // --- CONFIGURAR BIOMETRÍA ---
    private fun configurarBiometria() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
                    val email = prefs.getString("email", null)
                    val provider = prefs.getString("provider", null)
                    if (email != null && provider != null) {
                        navegarHome(email, provider)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    failedAttempts++
                    if (failedAttempts >= 3) {
                        cerrarSesion()
                    } else {
                        mostrarDialogo("Huella incorrecta ($failedAttempts/3)")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    mostrarDialogo("Error: $errString")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verificación biométrica")
            .setSubtitle("Confirma tu identidad con tu huella digital")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    private fun mostrarBiometria(email: String, provider: String) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
        failedAttempts = 0
        biometricPrompt.authenticate(promptInfo)
    }

    private fun cerrarSesion() {
        auth.signOut()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit()
        prefs.clear().apply()
        mostrarDialogo("Demasiados intentos fallidos. Cerrando sesión...")
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun iniciarSesionGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthConGoogle(account)
            } catch (e: ApiException) {
                mostrarDialogo("Error al iniciar sesión con Google.")
            }
        }
    }

    private fun firebaseAuthConGoogle(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken == null) {
            mostrarDialogo("Error: no se obtuvo el ID Token de Google.")
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mostrarBiometria(account.email ?: "", "GOOGLE")
            } else {
                mostrarDialogo("Error de autenticación con Google.")
            }
        }
    }

    private fun navegarHome(email: String, provider: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider)
        }
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
