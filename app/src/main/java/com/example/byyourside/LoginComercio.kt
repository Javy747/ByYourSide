package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window


class LoginComercio : AppCompatActivity(), VerificacionCampos {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnIniciarSesionComercio: Button
//  private lateinit var btnLoginEmailComercio : Button
    private val TAG = "LoginComercio"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null){
            verificaryRedirigirComercio(auth.currentUser!!.uid)
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login_comercio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etCorreoComercio = findViewById<EditText>(R.id.et_correo_comercio_login)
        val etContrasenhaComercio = findViewById<EditText>(R.id.et_contrasenha_comercio_login)

        btnIniciarSesionComercio = findViewById(R.id.btn_login_comercio)
    //  btnLoginEmailComercio = findViewById(R.id.btn_login_email_comercio)

        val tvRecuperarContrasenha =  findViewById<TextView>(R.id.tv_recuperar_contrasenha_comercio)

        val btnCrearCuentaComercio = findViewById<Button>(R.id.btn_crear_cuenta_comercio)
        val btnVolverInicioComercio = findViewById<ImageView>(R.id.volver_inicio_comercio)

        btnIniciarSesionComercio.setOnClickListener{

            val email = etCorreoComercio.text.toString()
            val contrasenha = etContrasenhaComercio.text.toString()

            // Lista de campos a validar con su función validadora y mensaje de error.
            val campos = listOf(
                Triple(etCorreoComercio, ::validarEmail, "El email debe tener el siguente formato: ejemplo27@example.com"),
                Triple(etContrasenhaComercio, ::validarContrasenha, "Introduce una contraseña válida")
            )

            // Itera sobre todo los campos para validar la entrada del usuario.
            for((editText, validador, errorMessage) in campos){
                val texto = editText.text.toString()
                if(!validador(texto)){
                    editText.error = errorMessage
                    editText.requestFocus()
                    return@setOnClickListener
                }
            }

            loginComercio(email, contrasenha)

        }
//
//
//        btnLoginEmailComercio.setOnClickListener{
//
//            val credentialManager = CredentialManager.create(this)
//
//            val googleIdOption = GetGoogleIdOption.Builder()
//                .setFilterByAuthorizedAccounts(false)
//                .setServerClientId(getString(R.string.default_web_client_id))
//                .setAutoSelectEnabled(true)
//                .build()
//
//            val request = GetCredentialRequest.Builder()
//                .addCredentialOption(googleIdOption)
//                .build()
//
//            lifecycleScope.launch {
//                try {
//                    val result = credentialManager.getCredential(
//                        request = request,
//                        context = this@LoginComercio
//                    )
//                    handleSignIn(result.credential)
//                } catch ( e : GetCredentialCancellationException){
//                    Log.d (TAG, "El usuario canceló el inicio de sesión con Google.")
//                } catch (e : GetCredentialException){
//                    Log.e (TAG, "Error obteniendo credencial: ${e.message}")
//                    Toast.makeText(this@LoginComercio, "Error obteniendo credencial", Toast.LENGTH_SHORT).show()
//                } catch (e : Exception){
//                    Log.e(TAG, "Error inesperado")
//                }
//            }
//
//        }

        tvRecuperarContrasenha.setOnClickListener {
            mostrarDialogoRecuperacion()
        }

        btnCrearCuentaComercio.setOnClickListener{
            val btnRegistrarComercio = Intent(this, RegistroComercio::class.java)
            startActivity(btnRegistrarComercio)
        }

        btnVolverInicioComercio.setOnClickListener{
            val volverAlInicio = Intent(this, InicioApp::class.java)
            startActivity(volverAlInicio)
        }

    }

    private fun loginComercio(email: String, contrasenha: String) {
        btnIniciarSesionComercio.isEnabled = false

        auth.signInWithEmailAndPassword(email, contrasenha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    val db = FirebaseFirestore.getInstance()

                    db.collection("comercios").document(user.uid).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                Log.d("LoginComercio", "Login exitoso. Usuario encontrado en 'comercios'.")
                                val intent = Intent(this, OpcionesComercio::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Log.w("LoginComercio", "Fallo de login. Usuario no encontrado en 'comercios'.")
                                Toast.makeText(this, "El usuario no es un comercio.", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                btnIniciarSesionComercio.isEnabled = true
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al verificar usuario en la colección 'comercios'", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            btnIniciarSesionComercio.isEnabled = true
                        }
                } else {
                    Toast.makeText(this, "Error de autenticación: El usuario o la contraseña no son correctos", Toast.LENGTH_LONG).show()
                    btnIniciarSesionComercio.isEnabled = true
                }
            }
    }

    private fun handleSignIn(credencial: Credential){
        if(credencial is CustomCredential && credencial.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credencial.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e : Exception) {
                Log.e(TAG, "Error procesando token de Google", e)
            }
        } else{
            Log.w(TAG,  "Credential is not of type Google ID!")
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credencial)
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

    private fun mostrarDialogoRecuperacion(){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_recuperar_contrasenha)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etCorreo = dialog.findViewById<EditText>(R.id.et_correo_recuperacion)
        val btnEnviar = dialog.findViewById<Button>(R.id.btn_enviar_recuperacion)
        val tvCancelar = dialog.findViewById<TextView>(R.id.tv_cancelar_recuperacion)

        btnEnviar.setOnClickListener {
            val email = etCorreo.text.toString().trim()

            if(email.isEmpty()){
                etCorreo.error = "Debes ingresar el correo del con el que registraste el comercio"
                etCorreo.requestFocus()
            } else if(!validarEmail(email)){
                etCorreo.error = "El email debe tener el siguente formato: ejemplo27@example.com"
                etCorreo.requestFocus()
            } else {
                enviarCorreoRecuperacion(email)
                dialog.dismiss()
            }
        }

        tvCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun enviarCorreoRecuperacion(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "Correo enviado un correo de recuperación a dirección indicada", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error al enviar el correo de recuperación", Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun updateUI(user : FirebaseUser?){
        if (user != null) {
            verificaryRedirigirComercio(user.uid)
        } else {
            btnIniciarSesionComercio.isEnabled = true
//          btnLoginEmailComercio.isEnabled = true
        }
    }

    private fun verificaryRedirigirComercio(uid : String){

        val db = FirebaseFirestore.getInstance()

        db.collection("comercios").document(uid).get()
            .addOnSuccessListener{ document ->
                if(document != null && document.exists()){
                    Log.d("LoginComercio", "El usuario es un comercio válido.")
                    val intent = Intent(this, OpcionesComercio::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("LoginComercio", "El usuario no es un comercio. Cerrando Sesión.")
                    Toast.makeText(this, "El usuario no es un comercio.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    btnIniciarSesionComercio.isEnabled = true
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "ERROR al verificar el usuario", Toast.LENGTH_LONG).show()
                auth.signOut()
                btnIniciarSesionComercio.isEnabled = true
            }
    }

}