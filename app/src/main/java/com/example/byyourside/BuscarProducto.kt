package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class BuscarProducto : AppCompatActivity(), VerificacionCampos {


    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            val intent = Intent(this, InicioApp::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_buscar_producto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbarBuscarProducto = findViewById<Toolbar>(R.id.toolbar_buscar_producto)
        setSupportActionBar(toolbarBuscarProducto)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val spinner = findViewById<Spinner>(R.id.spr_pais_buscar_producto)
        val etMarcaProducto = findViewById<EditText>(R.id.et_marca_producto_busqueda)
        val etNombreProducto = findViewById<EditText>(R.id.et_nombre_producto_busqueda)
        val btnBuscarProducto = findViewById<Button>(R.id.btn_buscar_producto)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.paises,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(1)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val textView = view as? TextView

                if (position == 0 || position == 1) {
                    textView?.setTextColor(getColor(R.color.gray))
                } else {
                    textView?.setTextColor(getColor(R.color.black))
                }

                (view as? TextView)?.textSize = 25f
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        btnBuscarProducto.setOnClickListener {
            val paisOrigenProductoPosicion = spinner.selectedItemPosition
            val pais = spinner.selectedItem.toString()
            val marca = etMarcaProducto.text.toString().trim()
            val nombre = etNombreProducto.text.toString().trim()

            val nombreVacio = nombre.isBlank()
            val marcaVacia = marca.isBlank()
            val paisNoSeleccionado = paisOrigenProductoPosicion <= 1

            if (nombreVacio && marcaVacia && paisNoSeleccionado) {
                Toast.makeText(this, "Complete al menos un campo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val resultados =
                        buscarProductos(pais = if (paisNoSeleccionado) null else pais, nombre = nombre, marca = marca)

                    val dtoList = resultados.map { (producto, comercio) ->
                        InfoProductoComercioDTO(
                            nombreComercio = comercio.nombre,
                            nombreProducto = producto.nombre,
                            marcaProducto = producto.marca,
                            paisOrigenProducto = producto.pais,
                            precioProducto = producto.precio,
                            nombreCalle = comercio.calle,
                            numeroCalleComercio = comercio.numero_local,
                            codigoPostalComercio = comercio.codigo_postal,
                            municipioComercio = comercio.municipio,
                            provinciaComercio = comercio.provincia,
                            webComercio = comercio.web
                        )
                    }

                    val intent = Intent(this@BuscarProducto, ResultadoBusqueda::class.java)
                    intent.putParcelableArrayListExtra("resultado_dto", ArrayList(dtoList))
                    startActivity(intent)

                } catch (e: Exception) {
                    Toast.makeText(this@BuscarProducto, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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

    suspend fun buscarProductos(
        pais: String?,
        nombre: String?,
        marca: String?
    ): List<Pair<Producto, Comercio>> = coroutineScope {
        val db = FirebaseFirestore.getInstance()

        var query: Query = db.collection("inventario")

        val paisNormalizado = pais?.trim()
        val nombreProducto = nombre?.trim()?.lowercase()
        val marcaProducto = marca?.trim()?.lowercase()

        val esPaisPlaceholder = paisNormalizado == "Selecciona un país" || paisNormalizado == "País de Origen del Producto"
        val hayPaisValido = !paisNormalizado.isNullOrBlank() && !esPaisPlaceholder
        val hayNombre = !nombreProducto.isNullOrBlank()
        val hayMarca = !marcaProducto.isNullOrBlank()

        if (hayPaisValido) {
            query = query.whereEqualTo("paisProducto", paisNormalizado)
        }

        if (hayNombre) {
            query = query.orderBy("nombreProducto")
                .startAt(nombreProducto)
                .endAt(nombreProducto + '\uf8ff')
        } else if (hayMarca) {
            query = query.orderBy("marcaProducto")
                .startAt(marcaProducto)
                .endAt(marcaProducto + '\uf8ff')
        }

        try {
            val snapshot = query.get().await()
            return@coroutineScope procesarInventarioConDetalles(snapshot.documents)
        } catch (e: Exception) {
            Log.e("FirestoreDebug", "Error al ejecutar la consulta en Firestore", e)
            throw e
        }
    }

    suspend fun procesarInventarioConDetalles(
        docsInventario: List<DocumentSnapshot>
    ): List<Pair<Producto, Comercio>> = coroutineScope {
        val db = FirebaseFirestore.getInstance()

        val tareas = docsInventario.map { inventarioDoc ->
            async {
                val idProducto = inventarioDoc.getString("idMaestroProducto")
                val idComercio = inventarioDoc.getString("idComercio")

                // Si faltan IDs, no se puede procesar
                if (idProducto == null || idComercio == null) return@async null

                val productoDoc = db.collection("productos")
                    .document(idProducto)
                    .get()
                    .await()
                val producto = productoDoc.toObject(Producto::class.java)

                val comercioDoc = db.collection("comercios")
                    .document(idComercio)
                    .get()
                    .await()
                val comercio = comercioDoc.toObject(Comercio::class.java)

                if (producto != null && comercio != null) Pair(producto, comercio) else null
            }
        }

        return@coroutineScope tareas.awaitAll().filterNotNull()
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
                val intent = Intent(this@BuscarProducto, InicioApp::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

}