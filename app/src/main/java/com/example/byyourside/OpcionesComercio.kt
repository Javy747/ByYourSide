package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OpcionesComercio : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_opciones_comercio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbarOpcionesComercio = findViewById<Toolbar>(R.id.toolbar_opciones_comercio)
        setSupportActionBar(toolbarOpcionesComercio)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnIrAgregarProducto = findViewById<Button>(R.id.btn_opc_agregar_producto)
        val btnIrConsultarInventario = findViewById<Button>(R.id.btn_opc_eliminar_producto)

        btnIrAgregarProducto.setOnClickListener {
            val intentAgregarProducto = Intent(this, AgregarProducto::class.java)
            startActivity(intentAgregarProducto)
        }

        btnIrConsultarInventario.setOnClickListener {
            val intentConsultarInventario = Intent(this, ConsultarInventario::class.java)
            startActivity(intentConsultarInventario)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_cerrar_sesion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cerrar_sesion -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun cerrarSesion() {
        // 1. Cerramos la sesión en Firebase (Cubre Email y Google)
        FirebaseAuth.getInstance().signOut()

        // 2. Limpiamos el estado de Credential Manager (Específico para Google/Passkeys)
        val credentialManager = CredentialManager.create(this)

        lifecycleScope.launch {
            try {
                // Esto obliga a que la próxima vez Google pida elegir cuenta
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("CierreSesion", "Error al limpiar credenciales: ${e.message}")
            } finally {
                // 3. Siempre redirigimos al inicio, falle o no la limpieza de credenciales
                val intent = Intent(this@OpcionesComercio, InicioApp::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

}