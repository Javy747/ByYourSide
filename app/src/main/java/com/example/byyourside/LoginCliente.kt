package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.credentials.exceptions.GetCredentialCancellationException
import kotlinx.coroutines.launch


class LoginCliente : AppCompatActivity(), VerificacionCampos {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnIniciarSesionCliente: Button
    private lateinit var btnLoginEmailCliente: Button
    private val TAG = "LoginCliente"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null){
            verificaryRedirigirCliente(auth.currentUser!!.uid)
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etCorreoCliente = findViewById<EditText>(R.id.et_correo_cliente_login)
        val etContrasenhaCliente = findViewById<EditText>(R.id.et_contrasenha_cliente_login)

        btnIniciarSesionCliente = findViewById<Button>(R.id.btn_login_cliente)
        btnLoginEmailCliente = findViewById(R.id.btn_login_email_cliente)

        val btnCrearCuentaCliente = findViewById<Button>(R.id.btn_crear_cuenta_cliente)
        val btnVolverIncio = findViewById<ImageView>(R.id.volver_inicio_cliente)

        btnIniciarSesionCliente.setOnClickListener{

            val email = etCorreoCliente.text.toString()
            val contrasenha = etContrasenhaCliente.text.toString()

            val campos = listOf(
                Triple(etCorreoCliente, ::validarEmail, "El email debe tener el siguente formato: ejem27@example.com"),
                Triple(etContrasenhaCliente, ::validarContrasenha, "Introduce una contraseña válida")
            )

            // Itera sobre todos los campos para validar la entrada del usuario.
            for((editText, validador, errorMessage) in campos){
                val texto = editText.text.toString()
                if(!validador(texto)){
                    editText.error = errorMessage
                    editText.requestFocus()
                    return@setOnClickListener
                }
            }

            loginCliente(email, contrasenha)

        }

        btnLoginEmailCliente.setOnClickListener {

            val credentialManager = CredentialManager.create(this)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginCliente
                    )
                    handleSignIn(result.credential)
                } catch (e : GetCredentialCancellationException) {
                    Log.d(TAG, "El usuario canceló el inicio de sesión con Google.")
                } catch (e : GetCredentialException){
                    Log.e (TAG, "Error obteniendo credencial: ${e.message}")
                    Toast.makeText(this@LoginCliente, "Error obteniendo credencial", Toast.LENGTH_SHORT).show()
                } catch (e : Exception){
                    Log.e(TAG, "Error inesperado")
                }
            }

        }

        btnCrearCuentaCliente.setOnClickListener{
            val crearCuentaCliente = Intent(this, RegistroCliente::class.java)
            startActivity(crearCuentaCliente)
        }

        btnVolverIncio.setOnClickListener{
            val volverAlinicio = Intent(this, InicioApp::class.java)
            startActivity(volverAlinicio)
        }
    }

    private fun loginCliente(email: String, contrasenha: String) {
        btnIniciarSesionCliente.isEnabled = false

        auth.signInWithEmailAndPassword(email, contrasenha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!! // No será nulo si la tarea fue exitosa
                    val db = FirebaseFirestore.getInstance()

                    db.collection("clientes").document(user.uid).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                Log.d("LoginCliente", "Login exitoso. Usuario encontrado en la colección 'clientes'.")
                                val intent = Intent(this, BuscarProducto::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Log.w("LoginCliente", "Fallo de login. Usuario no encontrado en la colección 'clientes'.")
                                Toast.makeText(this, "El usuario no es un cliente", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                btnIniciarSesionCliente.isEnabled = true
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al verificar usuario en la colección 'clientes'", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            btnIniciarSesionCliente.isEnabled = true
                        }
                } else {
                    Toast.makeText(this, "Error de autenticación: El usuario o la contraseña no son correctos", Toast.LENGTH_LONG).show()
                    btnIniciarSesionCliente.isEnabled = true
                }
            }
    }

    private fun handleSignIn(credencial: Credential){
        if(credencial is CustomCredential && credencial.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            try{
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credencial.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e : Exception){
                Log.e(TAG, "Error procesando token de Google", e)
            }
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authenticación Fallida.", Toast.LENGTH_SHORT)
                    updateUI(null)

                }
            }
    }

    private fun updateUI(user : FirebaseUser?){
        if (user != null) {
            verificaryRedirigirCliente(user.uid)
        } else {
            btnIniciarSesionCliente.isEnabled = true
            btnLoginEmailCliente.isEnabled = true
        }
    }


    private fun verificaryRedirigirCliente(uid : String){

        val db = FirebaseFirestore.getInstance()

        db.collection("clientes").document(uid).get()
            .addOnSuccessListener{ document ->
                if(document != null && document.exists()){
                    Log.d("LoginCliente", "El usuario es un cliente válido.")
                    val intent = Intent(this, BuscarProducto::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("LoginComercio", "El usuario no es un cliente. Cerrando Sesión.")
                    Toast.makeText(this, "El usuario no es un cliente.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    btnIniciarSesionCliente.isEnabled = true
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "ERROR al verificar el usuario", Toast.LENGTH_LONG).show()
                auth.signOut()
                btnIniciarSesionCliente.isEnabled = true
            }
    }

}