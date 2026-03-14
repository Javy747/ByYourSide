package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioApp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 1. Comprobamos si hay un usuario logueado antes de cargar la vista
        if (auth.currentUser != null) {
            verificarRolYRedirigir(auth.currentUser!!.uid)
            // Hacemos return para que la pantalla se quede "en blanco" (o con tu splash screen)
            // mientras decide a dónde enviarlo, evitando que vea los botones.
            return
        }

        // Si no hay sesión, cargamos la pantalla normal
        cargarPantallaInicio()
    }

    private fun cargarPantallaInicio() {
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_app)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnComercio = findViewById<Button>(R.id.btn_comercio_cd_inicio_app)
        val btnCliente = findViewById<Button>(R.id.btn_cliente_cd_inicio_app)

        btnComercio.setOnClickListener{
            val mainToComercio = Intent(this, LoginComercio::class.java)
            startActivity(mainToComercio)
        }

        btnCliente.setOnClickListener {
            val mainToCliente = Intent(this, LoginCliente::class.java)
            startActivity(mainToCliente)
        }
    }

    private fun verificarRolYRedirigir(uid: String) {
        val db = FirebaseFirestore.getInstance()

        // 1. Primero buscamos si el UID existe en la colección de "comercios"
        db.collection("comercios").document(uid).get()
            .addOnSuccessListener { documentComercio ->
                if (documentComercio != null && documentComercio.exists()) {
                    Log.d("InicioApp", "Sesión activa detectada: Es un Comercio.")
                    val intent = Intent(this, OpcionesComercio::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // 2. Si no es comercio, buscamos en la colección de "clientes"
                    db.collection("clientes").document(uid).get()
                        .addOnSuccessListener { documentCliente ->
                            if (documentCliente != null && documentCliente.exists()) {
                                Log.d("InicioApp", "Sesión activa detectada: Es un Cliente.")
                                val intent = Intent(this, BuscarProducto::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                // 3. Si no está en ninguna colección, es un usuario inválido/huérfano
                                Log.w("InicioApp", "Usuario logueado pero no existe en bd. Cerrando sesión.")
                                auth.signOut()
                                cargarPantallaInicio()
                            }
                        }
                        .addOnFailureListener {
                            Log.e("InicioApp", "Error al verificar la colección de clientes.")
                            cargarPantallaInicio()
                        }
                }
            }
            .addOnFailureListener {
                Log.e("InicioApp", "Error al verificar la colección de comercios.")
                cargarPantallaInicio()
            }
    }
}