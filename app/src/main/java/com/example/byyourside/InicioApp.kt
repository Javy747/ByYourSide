package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class InicioApp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        // 1. Si hay un usuario logueado, verificamos su rol (comercio o cliente)
        if (auth.currentUser != null) {
            verificarSesionExistente(auth.currentUser!!.uid)
            return
        }

        // 2. Si no hay nadie logueado, cargamos la interfaz normal

        configurarUIInicio()
    }

    // Esta función configura los botones de "Soy Cliente" / "Soy Comercio"
    private fun configurarUIInicio() {
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

    private fun verificarSesionExistente(uid: String) {
        val db = FirebaseFirestore.getInstance()

        // Paso A: Buscamos primero en la colección de COMERCIOS
        db.collection("comercios").document(uid).get()
            .addOnSuccessListener { documentComercio ->
                if (documentComercio.exists()) {
                    // Es un COMERCIO
                    redirigir(OpcionesComercio::class.java)
                } else {
                    // Paso B: Si NO es comercio, buscamos en la colección de CLIENTES
                    // NOTA: Asegúrate de que tu colección se llame "clientes" (o cámbialo aquí)
                    db.collection("clientes").document(uid).get()
                        .addOnSuccessListener { documentCliente ->
                            if (documentCliente.exists()) {
                                // Es un CLIENTE
                                redirigir(BuscarProducto::class.java)
                            } else {
                                // No existe en ninguna base de datos (sesión huérfana o error)
                                FirebaseAuth.getInstance().signOut()
                                configurarUIInicio()
                            }
                        }
                        .addOnFailureListener {
                            // Fallo de red al buscar cliente
                            configurarUIInicio()
                        }
                }
            }
            .addOnFailureListener {
                // Fallo de red al buscar comercio
                configurarUIInicio()
            }
    }

    // Función auxiliar para no repetir el código de Intent y limpieza de pila (stack)
    private fun redirigir(destino: Class<*>) {
        val intent = Intent(this, destino)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}