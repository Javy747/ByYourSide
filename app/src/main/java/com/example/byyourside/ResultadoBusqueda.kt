package com.example.byyourside

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ResultadoBusqueda : AppCompatActivity() {

    private var auth = FirebaseAuth.getInstance()

    private var direccionPendiente : String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            direccionPendiente?.let { abrirGoogleMaps(it) }
        } else {
            // Si el permiso es denegado, muestra un mensaje informativo al usuario.
            Toast.makeText(
                this,
                "Permiso de ubicación denegado. No se puede mostrar la ruta.",
                Toast.LENGTH_LONG
            ).show()
        }
        direccionPendiente = null
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(auth.currentUser == null){
            // Si no hay usuario, redirige a la pantalla de LoginComercio.
            val intent = Intent(this, InicioApp::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_resultado_busqueda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbarResultadoBusqueda = findViewById<Toolbar>(R.id.toolbar_resultado_buqueda)
        setSupportActionBar(toolbarResultadoBusqueda)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnVolverBuscarProducto = findViewById<ImageView>(R.id.rb_volver_buscar_producto)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_resultados_busqueda)
        val tvNoResultadosRb = findViewById<TextView>(R.id.tvNoResultadosRb)

        try {
            val resultados: List<InfoProductoComercioDTO> = intent.getParcelableArrayListExtra(
                "resultado_dto",
                InfoProductoComercioDTO::class.java
            ) ?: emptyList()

            if(resultados.isEmpty()){
                tvNoResultadosRb.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvNoResultadosRb.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ResultadoBusquedaAdapter(resultados) { direccion ->
                    solicitarPermisoYComoLlegar(direccion)
                }
            }

        }catch (e: Exception){
            Log.d("ResultadoBusqueda", "Error al deserializar los datos", e)
        }

        btnVolverBuscarProducto.setOnClickListener {
            val intent = Intent(this, BuscarProducto::class.java)
            startActivity(intent)
        }
    }

    private fun solicitarPermisoYComoLlegar(direccion: String) {
        when {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Si el permiso está concedido, abre Google Maps directamente.
                abrirGoogleMaps(direccion)
            }
            else -> {
                direccionPendiente = direccion
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun abrirGoogleMaps(direccion: String) {
        val intentUri = Uri.parse("https://maps.google.com/maps?daddr=${Uri.encode(direccion)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, intentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if(mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
        else {
            Toast.makeText(this, "Google Maps no instalado, abriendo en navegador.", Toast.LENGTH_SHORT).show()
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(direccion)}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
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

        val intent = Intent(this@ResultadoBusqueda, InicioApp::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()

    }

}