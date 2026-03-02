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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroCliente : AppCompatActivity(), VerificacionCampos {

    private lateinit var cliente: Cliente
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val etNombreCliente = findViewById<EditText>(R.id.et_nombre_cliente_registro)
        val etApellidosCliente = findViewById<EditText>(R.id.et_apellido_cliente_registro)
        val etEmailCliente = findViewById<EditText>(R.id.et_email_cliente_registro)
        val etTelefonoCliente = findViewById<EditText>(R.id.et_telefono_cliente_registro)
        val etContrasenhaCliente = findViewById<EditText>(R.id.et_contrasenha_cliente_registro)
        val etConfirmarContrasenhaCliente = findViewById<EditText>(R.id.et_confirmar_contrasenha_cliente_registro)

        val btnRegistroCliente = findViewById<Button>(R.id.btn_registro_cliente)

        val btnVolverLoginCliente = findViewById<ImageView>(R.id.volver_login_cliente)

        btnRegistroCliente.setOnClickListener {
            val nombre = etNombreCliente.text.toString()
            val apellidos = etApellidosCliente.text.toString()
            val email = etEmailCliente.text.toString()
            val telefono = etTelefonoCliente.text.toString().trim()
            val contrasenha = etContrasenhaCliente.text.toString().trim()
            val confirmarContrasenha = etConfirmarContrasenhaCliente.text.toString().trim()

            // Validación de campos vacíos
            if(nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || telefono.isEmpty() || contrasenha.isEmpty()){
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val campos = listOf(
                Triple(etNombreCliente, ::validarNombreApellidosCliente, "Introduce un nombre válido"),
                Triple(etApellidosCliente, ::validarNombreApellidosCliente, "Introduce un apellido válido"),
                Triple(etEmailCliente, ::validarEmail, "El email debe tener el siguente formato: ejem27@example.com"),
                Triple(etTelefonoCliente, ::validarTelefono, "Introduce un telefono válido"),
                Triple(etContrasenhaCliente, ::validarContrasenha, "Introduce una contraseña de al menos 8 carácteres, 1 mayúscula, 1 minúscula y 1 número")
            )

            for((editText, validador, errorMessage) in campos){
                val texto = editText.text.toString()
                if(!validador(texto)){
                    editText.error = errorMessage
                    editText.requestFocus()
                    return@setOnClickListener
                }
            }

            if(contrasenha.isEmpty() || confirmarContrasenha.isEmpty()){
                Toast.makeText(this, "Debe ingresar y confirmar la contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(contrasenha != confirmarContrasenha){
                etConfirmarContrasenhaCliente.error = "Las contraseñas no coinciden"
                etConfirmarContrasenhaCliente.requestFocus()
                return@setOnClickListener
            }

            cliente = Cliente(nombre, apellidos, telefono, email)

            registrarCliente(email, contrasenha)
        }

        btnVolverLoginCliente.setOnClickListener{
            val volverAlLoginCliente = Intent(this, LoginCliente::class.java)
            startActivity(volverAlLoginCliente)
        }

    }

    private fun registrarCliente(email: String, contrasenha: String) {
        auth.createUserWithEmailAndPassword(email, contrasenha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        db.collection("clientes")
                            .document(user.uid)
                            .set(cliente)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Registro del cliente fue exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, BuscarProducto::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error al guardar datos del cliente", e)
                                Toast.makeText(
                                    this,
                                    "Error al guardar los datos del cliente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Error: No se pudieron obtener los datos del usuario.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Error: Usuario Existente", Toast.LENGTH_LONG).show()
                }
            }
    }

}